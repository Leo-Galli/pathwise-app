package it.leogalli.pathwise.util

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Length
import dagger.hilt.android.qualifiers.ApplicationContext
import it.leogalli.pathwise.domain.model.Track
import it.leogalli.pathwise.domain.repository.FitnessSyncer
import java.time.Instant
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Integrazione nativa con Health Connect API (client 1.1.0).
 *
 * LETTURA   → peso e frequenza cardiaca (dashboard e calcolo calorico)
 * SCRITTURA → al termine della registrazione: sessione di trekking,
 *             calorie bruciate, passi e distanza nello stesso intervallo.
 *
 * Le firme usate qui sono state VERIFICATE contro il sorgente ufficiale
 * (connect-client-1.1.0-sources.jar):
 *  - permessi      → PermissionController.createRequestPermissionResultContract()
 *  - TimeRangeFilter → androidx.health.connect.client.time
 *  - Metadata      → factory pubblica Metadata.activelyRecorded(Device)
 *  - tipo esercizio → ExerciseSessionRecord.EXERCISE_TYPE_HIKING (37)
 *  - i record usano Instant + ZoneOffset + unità (Energy/Length), count: Long
 *
 * Implementa [FitnessSyncer] (porta del dominio): la sync è best-effort
 * e non blocca mai il flusso principale.
 */
@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : FitnessSyncer {

    // Lazy: getOrCreate lancia se Health Connect non è installato sul device.
    private val client: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    /** Permessi richiesti dall'app (dichiarati anche nel Manifest). */
    val requiredPermissions: Set<String> = setOf(
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class),
        HealthPermission.getWritePermission(DistanceRecord::class),
    )

    /**
     * Contratto per la richiesta permessi (1.1.0).
     * Da usare con rememberLauncherForActivityResult(contract) { granted -> ... }.
     */
    fun permissionContract(): ActivityResultContract<Set<String>, Set<String>> =
        PermissionController.createRequestPermissionResultContract()

    override suspend fun arePermissionsGranted(): Boolean =
        runCatching {
            client.permissionController.getGrantedPermissions().containsAll(requiredPermissions)
        }.getOrDefault(false)

    // ── LETTURA ──────────────────────────────────────────────────────────────

    /** Ultimo peso registrato in kg (ultimi 12 mesi), o null. */
    suspend fun readLatestWeightKg(): Double? {
        val request = ReadRecordsRequest(
            recordType = WeightRecord::class,
            timeRangeFilter = TimeRangeFilter.between(
                Instant.now().minusSeconds(31_536_000L), // 365 giorni
                Instant.now(),
            ),
            ascendingOrder = false,
        )
        return runCatching {
            client.readRecords(request).records.firstOrNull()?.weight?.inKilograms
        }.getOrNull()
    }

    /** Ultima frequenza cardiaca in BPM (ultime 24 ore), o null. */
    suspend fun readLatestHeartRateBpm(): Long? {
        val request = ReadRecordsRequest(
            recordType = HeartRateRecord::class,
            timeRangeFilter = TimeRangeFilter.between(
                Instant.now().minusSeconds(86_400L), // 24 ore
                Instant.now(),
            ),
            ascendingOrder = false,
        )
        return runCatching {
            client.readRecords(request).records.firstOrNull()?.samples?.lastOrNull()?.beatsPerMinute
        }.getOrNull()
    }

    // ── SCRITTURA (sessione completata) ─────────────────────────────────────

    /**
     * Scrive la sessione di trekking completata con tutti i dettagli:
     * esercizio, calorie, passi e distanza nello stesso intervallo temporale.
     */
    override suspend fun writeCompletedTrek(track: Track) {
        val start = Instant.ofEpochMilli(track.startedAtEpochMillis)
        val end = start.plusSeconds(track.durationMinutes * 60L)
        val utc = ZoneOffset.UTC
        // Metadata: il costruttore primario è internal, si usa la factory pubblica.
        // Device richiede il tipo (obbligatorio, senza default): qui uno smartphone.
        val metadata = Metadata.activelyRecorded(Device(Device.TYPE_PHONE))

        val session = ExerciseSessionRecord(
            startTime = start,
            startZoneOffset = utc,
            endTime = end,
            endZoneOffset = utc,
            metadata = metadata,
            exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_HIKING,
            title = "PathWise Trekking",
            notes = "Percorso: ${track.name} · D+ ${track.ascentM.toInt()} m",
            // exerciseRoute forza la risoluzione sul costruttore SECONDARIO pubblico
            // (il primario con gli stessi parametri è internal).
            exerciseRoute = null,
        )
        val calories = TotalCaloriesBurnedRecord(
            startTime = start,
            startZoneOffset = utc,
            endTime = end,
            endZoneOffset = utc,
            energy = Energy.kilocalories(track.caloriesKcal.toDouble()),
            metadata = metadata,
        )
        val steps = StepsRecord(
            startTime = start,
            startZoneOffset = utc,
            endTime = end,
            endZoneOffset = utc,
            count = track.steps.toLong(),
            metadata = metadata,
        )
        val distance = DistanceRecord(
            startTime = start,
            startZoneOffset = utc,
            endTime = end,
            endZoneOffset = utc,
            distance = Length.meters(track.distanceMeters),
            metadata = metadata,
        )

        client.insertRecords(listOf(session, calories, steps, distance))
    }

    /**
     * Stima del peso da usare nel motore: preferisce il peso reale
     * letto da Health Connect, altrimenti il default del profilo.
     */
    suspend fun bestEffortBodyWeightKg(fallbackKg: Double): Double =
        readLatestWeightKg() ?: fallbackKg
}
