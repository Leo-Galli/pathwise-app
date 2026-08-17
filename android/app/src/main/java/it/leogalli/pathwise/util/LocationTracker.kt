package it.leogalli.pathwise.util

import android.content.Context
import android.location.Location
import android.os.PowerManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import it.leogalli.pathwise.domain.model.TrackPoint
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.tasks.await

/**
 * Tracker GPS con CAMPIONAMENTO DINAMICO per il risparmio energetico
 * durante le camminate lunghe:
 *
 *  - velocità > 6 km/h  → 1 fix ogni 5 s  (salita veloce / discesa)
 *  - velocità > 2 km/h  → 1 fix ogni 10 s
 *  - fermo o lento       → 1 fix ogni 20 s
 *  - risparmio batteria  → 1 fix ogni 60 s (soglia alta anche in movimento)
 *
 * Il cambio di intervallo avviene in modo adattivo: quando la velocità
 * media cambia fascia, la richiesta viene ricalibrata (debounce di 15 s).
 */
@Singleton
class LocationTracker @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val fused: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _locations = MutableSharedFlow<TrackPoint>(extraBufferCapacity = 128)
    val locations = _locations.asSharedFlow()

    @Volatile
    var isTracking: Boolean = false
        private set

    private var lastIntervalMs = 0L
    private var lastAdaptationMs = 0L

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            val now = System.currentTimeMillis()
            // Debounce di 15 s tra un ricalcolo dell'intervallo e l'altro
            if (now - lastAdaptationMs > 15_000) {
                adaptSamplingInterval(location)
                lastAdaptationMs = now
            }
            _locations.tryEmit(
                TrackPoint(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitudeM = if (location.hasAltitude()) location.altitude else 0.0,
                    timestampEpochMillis = now,
                )
            )
        }
    }

    fun startTracking() {
        if (isTracking) return
        isTracking = true
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, DEFAULT_INTERVAL_MS)
            .setMinUpdateIntervalMillis(2_000)
            .build()
        fused.requestLocationUpdates(request, callback, null)
    }

    fun stopTracking() {
        if (!isTracking) return
        isTracking = false
        fused.removeLocationUpdates(callback)
    }

    /** Ultima posizione nota (per SOS e per centrare la mappa). */
    suspend fun getCurrentLocation(): Location? =
        runCatching { fused.lastLocation.await() }.getOrNull()

    /**
     * Ricalibra l'intervallo di campionamento in base a velocità e batteria.
     */
    private fun adaptSamplingInterval(location: Location) {
        val speedKmh = if (location.hasSpeed()) location.speed * 3.6 else 0.0
        val batterySaver = isPowerSaveMode()
        val intervalMs = computeInterval(speedKmh, batterySaver)
        if (intervalMs != lastIntervalMs) {
            lastIntervalMs = intervalMs
            // In risparmio energetico la precisione scende a balanced
            val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, intervalMs)
                .setMinUpdateIntervalMillis(intervalMs / 2)
                .build()
            fused.requestLocationUpdates(request, callback, null)
        }
    }

    internal fun computeInterval(speedKmh: Double, batterySaver: Boolean): Long = when {
        batterySaver -> SAVE_POWER_INTERVAL_MS
        speedKmh > 6.0 -> FAST_INTERVAL_MS
        speedKmh > 2.0 -> NORMAL_INTERVAL_MS
        else -> SLOW_INTERVAL_MS
    }

    private fun isPowerSaveMode(): Boolean =
        context.getSystemService(Context.POWER_SERVICE)?.let {
            (it as PowerManager).isPowerSaveMode
        } ?: false

    companion object {
        const val FAST_INTERVAL_MS = 5_000L
        const val NORMAL_INTERVAL_MS = 10_000L
        const val SLOW_INTERVAL_MS = 20_000L
        const val SAVE_POWER_INTERVAL_MS = 60_000L
        const val DEFAULT_INTERVAL_MS = 5_000L
    }
}
