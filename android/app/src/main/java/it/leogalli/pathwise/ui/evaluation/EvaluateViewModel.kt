package it.leogalli.pathwise.ui.evaluation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.leogalli.pathwise.domain.model.FitnessLevel
import it.leogalli.pathwise.domain.model.GroupProfile
import it.leogalli.pathwise.domain.model.TerrainType
import it.leogalli.pathwise.domain.model.TrailGeometry
import it.leogalli.pathwise.domain.repository.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Stato della schermata di valutazione. */
data class EvaluationUiState(
    val profile: GroupProfile = GroupProfile(ages = listOf(7, 39, 41, 67)),
    val geometry: TrailGeometry = TrailGeometry(
        distanceKm = 9.5,
        ascentM = 980.0,
        descentM = 980.0,
        maxAltitudeM = 2200.0,
        avgGradePercent = 18.0,
        maxGradePercent = 35.0,
        terrain = TerrainType.TRAIL_CAI_EE,
    ),
    val language: String = "it",
)

/**
 * ViewModel della valutazione: profilo gruppo, geometria percorso e lingua.
 * Il calcolo vero e proprio è delegato a PathEvaluator (puro e testabile).
 */
@HiltViewModel
class EvaluateViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EvaluationUiState())
    val uiState: StateFlow<EvaluationUiState> = _uiState.asStateFlow()

    init {
        // La lingua salvata nelle impostazioni guida lo stato iniziale
        viewModelScope.launch {
            settingsRepository.language.collect { language ->
                _uiState.update { it.copy(language = language) }
            }
        }
    }

    /** Cambia lingua: aggiorna lo stato E persiste nel DataStore. */
    fun setLanguage(code: String) {
        _uiState.update { it.copy(language = code) }
        viewModelScope.launch { settingsRepository.setLanguage(code) }
    }

    fun addAge() = _uiState.update { state ->
        state.copy(profile = state.profile.copy(ages = state.profile.ages + 30))
    }

    fun removeAgeAt(index: Int) = _uiState.update { state ->
        val ages = state.profile.ages.toMutableList().apply { removeAt(index) }
        state.copy(profile = state.profile.copy(ages = ages))
    }

    fun setAgeAt(index: Int, raw: String) = _uiState.update { state ->
        val age = raw.toIntOrNull()?.coerceIn(1, 110) ?: return@update state
        val ages = state.profile.ages.toMutableList().apply { set(index, age) }
        state.copy(profile = state.profile.copy(ages = ages))
    }

    fun setFitness(level: FitnessLevel) = _uiState.update {
        it.copy(profile = it.profile.copy(fitnessLevel = level))
    }

    fun setBackpack(kg: Double) = _uiState.update {
        it.copy(profile = it.profile.copy(backpackWeightKg = kg))
    }

    fun setTerrain(terrain: TerrainType) = _uiState.update {
        it.copy(geometry = it.geometry.copy(terrain = terrain, maxGradePercent = defaultGrade(terrain)))
    }

    fun setDistance(km: Double) = _uiState.update {
        it.copy(geometry = it.geometry.copy(distanceKm = km))
    }

    fun setAscent(m: Double) = _uiState.update {
        it.copy(geometry = it.geometry.copy(ascentM = m, descentM = m))
    }

    fun setMaxAltitude(m: Double) = _uiState.update {
        it.copy(geometry = it.geometry.copy(maxAltitudeM = m))
    }

    private fun defaultGrade(terrain: TerrainType): Double = when (terrain) {
        TerrainType.SENTIERO -> 28.0
        TerrainType.TRAIL_CAI_EE -> 35.0
        TerrainType.ROCCIA -> 55.0
        TerrainType.FERRATA -> 70.0
    }
}
