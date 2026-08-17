package it.leogalli.pathwise.ui.map

import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.leogalli.pathwise.domain.engine.PathEvaluator
import it.leogalli.pathwise.domain.model.Poi
import it.leogalli.pathwise.domain.model.PoiType
import it.leogalli.pathwise.domain.model.Track
import it.leogalli.pathwise.domain.model.TrackPoint
import it.leogalli.pathwise.domain.repository.PoiRepository
import it.leogalli.pathwise.domain.repository.SettingsRepository
import it.leogalli.pathwise.domain.usecase.SaveCompletedTrackUseCase
import it.leogalli.pathwise.util.HealthConnectManager
import it.leogalli.pathwise.util.LocationTracker
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Stato UI della schermata mappa. */
data class MapUiState(
    val pois: List<Poi> = emptyList(),
    val activeLayers: Set<PoiType> = setOf(PoiType.RIFUGIO, PoiType.BIVACCO, PoiType.PANORAMA),
    val isRecording: Boolean = false,
    val currentLatitude: Double = DEFAULT_LAT,
    val currentLongitude: Double = DEFAULT_LNG,
    val currentAltitudeM: Double = 0.0,
    val distanceMeters: Double = 0.0,
    val durationSeconds: Long = 0L,
    val liveCaloriesKcal: Int = 0,
    val steps: Int = 0,
    val ascentM: Double = 0.0,
    val descentM: Double = 0.0,
    val sosRecipients: List<String> = listOf("112"),
    /** True dopo la concessione dei permessi Health Connect. */
    val healthConnected: Boolean = false,
    /** True dopo il salvataggio di una traccia (banner monouso). */
    val trackSaved: Boolean = false,
) {
    companion object {
        // Centratura di default: Alpi Occidentali (mostra subito qualcosa di utile)
        const val DEFAULT_LAT = 45.8341
        const val DEFAULT_LNG = 6.8653
    }
}

/**
 * ViewModel della mappa: gestisce layer POI, registrazione con statistiche
 * live (distanza, altitudine, calorie in tempo reale con Pandolf) e SOS.
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val poiRepository: PoiRepository,
    private val settingsRepository: SettingsRepository,
    private val locationTracker: LocationTracker,
    private val healthConnectManager: HealthConnectManager,
    private val saveCompletedTrackUseCase: SaveCompletedTrackUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val recordingPoints = mutableListOf<TrackPoint>()
    private var lastPointForCalories: TrackPoint? = null
    private var startEpochMillis = 0L
    private var bodyWeightKg = 70.0

    val visiblePois: StateFlow<List<Poi>> = combine(
        _uiState,
        poiRepository.observePois(),
    ) { state, pois ->
        pois.filter { it.type in state.activeLayers }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            poiRepository.seedIfEmpty()
            settingsRepository.sosRecipients.collect { recipients ->
                _uiState.update { it.copy(sosRecipients = recipients) }
            }
        }
    }

    // ── Health Connect ─────────────────────────────────────────

    /** Contratto per la richiesta permessi (usato dalla UI con rememberLauncherForActivityResult). */
    val healthPermissionContract: ActivityResultContract<Set<String>, Set<String>>
        get() = healthConnectManager.permissionContract()

    /** Permessi reali da richiedere al primo collegamento. */
    val requiredHealthPermissions: Set<String>
        get() = healthConnectManager.requiredPermissions

    /** Da chiamare quando la UI riceve i permessi concessi. */
    fun onHealthPermissionsGranted() {
        _uiState.update { it.copy(healthConnected = true) }
    }

    // ── Layer ──────────────────────────────────────────────────

    fun toggleLayer(type: PoiType) {
        _uiState.update { state ->
            val layers = state.activeLayers.toMutableSet()
            if (!layers.add(type)) layers.remove(type)
            state.copy(activeLayers = layers)
        }
    }

    // ── Registrazione ──────────────────────────────────────────

    fun startRecording() {
        if (_uiState.value.isRecording) return
        recordingPoints.clear()
        lastPointForCalories = null
        startEpochMillis = System.currentTimeMillis()
        _uiState.update { it.copy(isRecording = true) }
        viewModelScope.launch {
            bodyWeightKg = healthConnectManager.bestEffortBodyWeightKg(70.0)
        }
        locationTracker.startTracking()
        viewModelScope.launch {
            locationTracker.locations.collect { point ->
                onTrackPoint(point)
            }
        }
    }

    fun stopRecording() {
        if (!_uiState.value.isRecording) return
        locationTracker.stopTracking()
        val end = System.currentTimeMillis()
        val state = _uiState.value
        val durationSeconds = (end - startEpochMillis) / 1000
        val track = Track(
            name = "Trekking ${java.time.LocalDate.now()}",
            startedAtEpochMillis = startEpochMillis,
            durationMinutes = (durationSeconds / 60).toInt().coerceAtLeast(1),
            distanceMeters = state.distanceMeters,
            ascentM = state.ascentM,
            descentM = state.descentM,
            maxAltitudeM = state.pointsMaxAltitude(),
            caloriesKcal = state.liveCaloriesKcal,
            steps = state.steps,
            points = recordingPoints.toList(),
        )
        viewModelScope.launch {
            saveCompletedTrackUseCase(track)
            _uiState.update { it.copy(isRecording = false, trackSaved = true) }
        }
    }

    /** Chiude il banner "traccia salvata" (chiamato dalla UI dopo un delay). */
    fun clearTrackSaved() {
        _uiState.update { it.copy(trackSaved = false) }
    }

    private fun MapUiState.pointsMaxAltitude(): Double =
        recordingPoints.maxOfOrNull { it.altitudeM } ?: currentAltitudeM

    /** Aggiorna le statistiche live a ogni nuovo punto GPS. */
    private fun onTrackPoint(point: TrackPoint) {
        val previous = recordingPoints.lastOrNull()
        recordingPoints.add(point)

        var distance = 0.0
        var ascent = 0.0
        var descent = 0.0
        var caloriesIncrement = 0.0

        if (previous != null) {
            val segmentMeters = previous.distanceToMeters(point)
            distance = segmentMeters
            val deltaAlt = point.altitudeM - previous.altitudeM
            if (deltaAlt > 0) ascent = deltaAlt else descent = -deltaAlt

            // Calorie live con Pandolf: usa velocità e pendenza reali del tratto
            val dtHours = (point.timestampEpochMillis - previous.timestampEpochMillis) / 3_600_000.0
            if (dtHours > 0) {
                val speedKmh = (segmentMeters / 1000.0) / dtHours
                val grade = if (segmentMeters > 0) (deltaAlt / segmentMeters) * 100.0 else 0.0
                val watts = PathEvaluator.pandolfWatts(
                    bodyKg = bodyWeightKg,
                    loadKg = 8.0,
                    speedMs = speedKmh / 3.6,
                    gradePct = grade.coerceAtLeast(0.0),
                    eta = 1.1,
                )
                caloriesIncrement = watts * 0.01433 * (dtHours * 60.0)
            }
        }

        // Passi stimati: ~0.70 m/passo in piano, meno in salita
        val stepIncrement = (distance / 0.7).roundToInt()

        _uiState.update { state ->
            state.copy(
                currentLatitude = point.latitude,
                currentLongitude = point.longitude,
                currentAltitudeM = point.altitudeM,
                distanceMeters = state.distanceMeters + distance,
                ascentM = state.ascentM + ascent,
                descentM = state.descentM + descent,
                liveCaloriesKcal = state.liveCaloriesKcal + caloriesIncrement.toInt(),
                steps = state.steps + stepIncrement,
                durationSeconds = (System.currentTimeMillis() - startEpochMillis) / 1000,
            )
        }
    }

    /** Messaggio per la dashboard: statistiche della registrazione. */
    fun durationLabel(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        return if (h > 0) "%dh %02dm".format(h, m) else "%02dm".format(m)
    }
}
