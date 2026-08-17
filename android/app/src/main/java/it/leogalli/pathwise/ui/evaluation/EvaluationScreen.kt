package it.leogalli.pathwise.ui.evaluation

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.leogalli.pathwise.R
import it.leogalli.pathwise.domain.engine.PathEvaluator
import it.leogalli.pathwise.domain.model.FitnessLevel
import it.leogalli.pathwise.domain.model.FeasibilityStatus
import it.leogalli.pathwise.domain.model.GroupProfile
import it.leogalli.pathwise.domain.model.TerrainType
import it.leogalli.pathwise.domain.model.TrailGeometry
import it.leogalli.pathwise.domain.model.WarningSeverity
import it.leogalli.pathwise.ui.theme.Amber400
import it.leogalli.pathwise.ui.theme.Mint400
import it.leogalli.pathwise.ui.theme.Night900
import it.leogalli.pathwise.ui.theme.Night950
import it.leogalli.pathwise.ui.theme.Rose400
import it.leogalli.pathwise.ui.theme.TextSecondary
import it.leogalli.pathwise.util.LocaleHelper
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Schermata di valutazione: profilo del gruppo + geometria del percorso,
 * verdetto live del PathWise Engine. In cima anche il selettore di lingua
 * (override in-app senza riavviare lo smartphone).
 */
@Composable
fun EvaluationScreen(
    evaluateViewModel: EvaluateViewModel = hiltViewModel(),
) {
    val state by evaluateViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Lingua (override dinamico) ─────────────────────────
        Card(colors = CardDefaults.cardColors(containerColor = Night900)) {
            Column(Modifier.padding(14.dp)) {
                Text("Lingua / Language", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LocaleHelper.SUPPORTED_LANGUAGES.forEach { code ->
                        FilterChip(
                            selected = state.language == code,
                            onClick = {
                                scope.launch {
                                    evaluateViewModel.setLanguage(code)
                                    LocaleHelper.applyLocale(context, code)
                                    if (android.os.Build.VERSION.SDK_INT < 33) {
                                        (context as? Activity)?.recreate()
                                    }
                                }
                            },
                            label = { Text(code.uppercase(Locale.ROOT)) },
                        )
                    }
                }
            }
        }

        // ── Gruppo ─────────────────────────────────────────────
        Card(colors = CardDefaults.cardColors(containerColor = Night900)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Gruppo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    state.profile.ages.forEachIndexed { index, age ->
                        OutlinedTextField(
                            value = age.toString(),
                            onValueChange = { evaluateViewModel.setAgeAt(index, it) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("${index + 1}°") },
                            trailingIcon = if (state.profile.ages.size > 1) {
                                {
                                    IconButton(onClick = { evaluateViewModel.removeAgeAt(index) }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Rimuovi")
                                    }
                                }
                            } else null,
                        )
                    }
                    IconButton(onClick = evaluateViewModel::addAge) {
                        Icon(Icons.Filled.Add, contentDescription = "Aggiungi")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FitnessLevel.entries.forEach { level ->
                        FilterChip(
                            selected = state.profile.fitnessLevel == level,
                            onClick = { evaluateViewModel.setFitness(level) },
                            label = { Text(level.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        )
                    }
                }
            }
        }

        // ── Percorso ───────────────────────────────────────────
        Card(colors = CardDefaults.cardColors(containerColor = Night900)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Percorso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TerrainType.entries.forEach { terrain ->
                        FilterChip(
                            selected = state.geometry.terrain == terrain,
                            onClick = { evaluateViewModel.setTerrain(terrain) },
                            label = { Text(terrainLabel(terrain)) },
                        )
                    }
                }
                SliderRow("Distanza", state.geometry.distanceKm, 1.0..30.0, 0.5, "km", evaluateViewModel::setDistance)
                SliderRow("D+ (dislivello)", state.geometry.ascentM, 0.0..2500.0, 10.0, "m", evaluateViewModel::setAscent)
                SliderRow("Quota massima", state.geometry.maxAltitudeM, 300.0..4200.0, 50.0, "m", evaluateViewModel::setMaxAltitude)
                SliderRow("Zaino medio", state.profile.backpackWeightKg, 0.0..25.0, 0.5, "kg", evaluateViewModel::setBackpack)
            }
        }

        // ── Verdetto live ──────────────────────────────────────
        val result = remember(state.profile, state.geometry) {
            PathEvaluator.evaluate(state.profile, state.geometry)
        }
        VerdictCard(result.status, result.estimatedTimeMinutes, result.caloriesKcal, result.averageMet, result.warnings)

        // Il motore è già live: ogni modifica aggiorna subito il verdetto.
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Double,
    range: ClosedFloatingPointRange<Double>,
    step: Double,
    unit: String,
    onChange: (Double) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "$label: %.1f $unit".format(value),
            modifier = Modifier.width(170.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toDouble()) },
            valueRange = range.start.toFloat()..range.endInclusive.toFloat(),
            steps = ((range.endInclusive - range.start) / step).toInt() - 1,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Card del verdetto con colore per stato e lista avvisi. */
@Composable
private fun VerdictCard(
    status: FeasibilityStatus,
    minutes: Int,
    calories: Int,
    met: Double,
    warnings: List<it.leogalli.pathwise.domain.model.SafetyWarning>,
) {
    val (bg, fg, label) = when (status) {
        FeasibilityStatus.SUITABLE_ALL -> Triple(Mint400, Night950, "Adatto a tutti")
        FeasibilityStatus.NEEDS_TRAINING -> Triple(Amber400, Night950, "Richiede allenamento")
        FeasibilityStatus.NOT_RECOMMENDED -> Triple(Rose400, Night950, "Non consigliato")
    }
    Card(colors = CardDefaults.cardColors(containerColor = Night900)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(color = bg, contentColor = fg, shape = MaterialTheme.shapes.small) {
                Text(label, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Stat("Tempo", "%dh %02dm".format(minutes / 60, minutes % 60))
                Stat("Calorie", calories.toString())
                Stat("MET", "%.1f".format(met))
            }
            warnings.forEach { warning ->
                Surface(
                    color = when (warning.severity) {
                        WarningSeverity.BLOCK -> Rose400.copy(alpha = 0.15f)
                        WarningSeverity.WARNING -> Amber400.copy(alpha = 0.15f)
                        WarningSeverity.INFO -> Mint400.copy(alpha = 0.15f)
                    },
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(warning.message, modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

private fun terrainLabel(terrain: TerrainType): String = when (terrain) {
    TerrainType.SENTIERO -> "Sentiero"
    TerrainType.TRAIL_CAI_EE -> "CAI/EE"
    TerrainType.ROCCIA -> "Roccia"
    TerrainType.FERRATA -> "Ferrata"
}
