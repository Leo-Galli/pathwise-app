package it.leogalli.pathwise.ui.map

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import it.leogalli.pathwise.R
import it.leogalli.pathwise.domain.model.PoiType
import it.leogalli.pathwise.ui.theme.Amber400
import it.leogalli.pathwise.ui.theme.Mint400
import it.leogalli.pathwise.ui.theme.Mint500
import it.leogalli.pathwise.ui.theme.Night800
import it.leogalli.pathwise.ui.theme.Night900
import it.leogalli.pathwise.ui.theme.Night950
import it.leogalli.pathwise.ui.theme.Rose400
import it.leogalli.pathwise.ui.theme.Sky400
import it.leogalli.pathwise.ui.theme.TextSecondary
import it.leogalli.pathwise.util.SosManager
import java.util.Locale
import kotlinx.coroutines.delay

/**
 * Schermata mappa: Google Maps con toggle dei layer, modalità 3D,
 * registrazione (solo su azione utente) con dashboard live e SOS.
 */
@Composable
fun MapScreen(viewModel: MapViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pois by viewModel.visiblePois.collectAsState()
    val context = LocalContext.current

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(state.currentLatitude, state.currentLongitude), 13f)
    }

    // Centra la mappa sulla posizione SOLO al primo fix GPS:
    // durante la registrazione l'utente deve poter navigare liberamente.
    var cameraCentered by remember { mutableStateOf(false) }
    LaunchedEffect(state.currentLatitude, state.currentLongitude) {
        if (!cameraCentered && state.currentLatitude != MapUiState.DEFAULT_LAT) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(state.currentLatitude, state.currentLongitude), 13f
            )
            cameraCentered = true
        }
    }

    var layersOpen by remember { mutableStateOf(false) }
    var is3d by remember { mutableStateOf(false) }

    // Richiesta permessi Health Connect: lanciata con l'insieme REALE dei
    // permessi richiesti (non un set vuoto). Dopo la concessione la
    // sincronizzazione avviene in automatico a fine registrazione.
    val healthLauncher = rememberLauncherForActivityResult(viewModel.healthPermissionContract) { granted ->
        // La spunta appare solo se TUTTI i permessi richiesti sono stati concessi
        if (granted.containsAll(viewModel.requiredHealthPermissions)) viewModel.onHealthPermissionsGranted()
    }

    // Tilt 3D sui rilievi
    LaunchedEffect(is3d) {
        cameraPositionState.position = CameraPosition.Builder(cameraPositionState.position)
            .tilt(if (is3d) 60f else 0f)
            .build()
    }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
        ) {
            // Marker dei POI filtrati dai layer attivi
            pois.forEach { poi ->
                Marker(
                    state = MarkerState(position = LatLng(poi.latitude, poi.longitude)),
                    title = poi.name,
                    snippet = poi.description ?: poi.type.name,
                    zIndex = 10f,
                )
            }
        }

        // ── SOS (sempre visibile) ─────────────────────────────
        FloatingActionButton(
            onClick = {
                val body = SosManager.buildSosBody(
                    latitude = state.currentLatitude,
                    longitude = state.currentLongitude,
                    altitudeM = state.currentAltitudeM,
                )
                val recipients = state.sosRecipients.joinToString(";")
                SosManager.openSmsComposer(context, recipients, body)
            },
            containerColor = Rose400,
            contentColor = Night950,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 120.dp),
        ) {
            Icon(Icons.Filled.Warning, contentDescription = stringResource(R.string.sos_title))
        }

        // ── Toggle layer ───────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End,
        ) {
            AnimatedVisibility(
                visible = layersOpen,
                enter = fadeIn() + scaleIn(initialScale = 0.9f),
                exit = fadeOut() + scaleOut(targetScale = 0.9f),
            ) {
                LayerPanel(state = state, onToggle = viewModel::toggleLayer)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FloatingActionButton(
                    onClick = { is3d = !is3d },
                    containerColor = if (is3d) Sky400 else Night800,
                    contentColor = if (is3d) Night950 else Color.White,
                    modifier = Modifier.size(44.dp),
                ) { Text("3D", fontWeight = FontWeight.Bold) }

                FloatingActionButton(
                    onClick = { layersOpen = !layersOpen },
                    containerColor = Night800,
                    contentColor = Color.White,
                    modifier = Modifier.size(44.dp),
                ) { Icon(Icons.Filled.Layers, contentDescription = stringResource(R.string.map_layers)) }
            }
        }

        // ── Dashboard live ─────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Banner monouso e localizzato: sparisce dopo 4 secondi
            if (state.trackSaved) {
                Surface(
                    color = Mint500,
                    contentColor = Night950,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.map_track_saved), modifier = Modifier.padding(12.dp), fontWeight = FontWeight.SemiBold)
                }
                LaunchedEffect(state.trackSaved) {
                    delay(4_000)
                    viewModel.clearTrackSaved()
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Night900.copy(alpha = 0.92f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        StatItem(stringResource(R.string.map_altitude), "${state.currentAltitudeM.toInt()} m")
                        StatItem(stringResource(R.string.map_distance), "${format1(state.distanceMeters / 1000)} km")
                        StatItem(stringResource(R.string.map_duration), viewModel.durationLabel(state.durationSeconds))
                        StatItem(stringResource(R.string.map_calories_live), state.liveCaloriesKcal.toString())
                    }
                    TextButton(
                        onClick = { healthLauncher.launch(viewModel.requiredHealthPermissions) },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        if (state.healthConnected) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.size(6.dp))
                        }
                        Text(stringResource(R.string.map_sync_health))
                    }
                    Spacer(Modifier.size(12.dp))
                    Button(
                        onClick = {
                            if (state.isRecording) viewModel.stopRecording() else viewModel.startRecording()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.isRecording) Rose400 else Mint500,
                            contentColor = Night950,
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            if (state.isRecording) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                            contentDescription = null,
                        )
                        Text(
                            stringResource(if (state.isRecording) R.string.map_stop_recording else R.string.map_start_recording),
                            modifier = Modifier.padding(start = 8.dp),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

/** Pannello dei layer attivabili (filtri POI). */
@Composable
private fun LayerPanel(state: MapUiState, onToggle: (PoiType) -> Unit) {
    Surface(
        color = Night900.copy(alpha = 0.95f),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 8.dp,
        modifier = Modifier.widthIn(min = 220.dp),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.map_layers), fontWeight = FontWeight.Bold, color = TextSecondary)
            LayerChip(stringResource(R.string.layer_rifugi), PoiType.RIFUGIO, Mint400, state.activeLayers, onToggle)
            LayerChip(stringResource(R.string.layer_bivacchi), PoiType.BIVACCO, Mint400, state.activeLayers, onToggle)
            LayerChip(stringResource(R.string.layer_panorami), PoiType.PANORAMA, Sky400, state.activeLayers, onToggle)
            LayerChip(stringResource(R.string.layer_sorgenti), PoiType.SORGENTE, Sky400, state.activeLayers, onToggle)
            LayerChip(stringResource(R.string.layer_pericoli), PoiType.PERICOLO, Amber400, state.activeLayers, onToggle)
        }
    }
}

@Composable
private fun LayerChip(
    label: String,
    type: PoiType,
    color: Color,
    active: Set<PoiType>,
    onToggle: (PoiType) -> Unit,
) {
    FilterChip(
        selected = type in active,
        onClick = { onToggle(type) },
        label = { Text(label) },
        leadingIcon = if (type in active) {
            {
                Box(
                    Modifier
                        .size(8.dp)
                        .background(color, CircleShape)
                )
            }
        } else null,
    )
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

private fun format1(value: Double): String = String.format(Locale.ITALY, "%.1f", value)
