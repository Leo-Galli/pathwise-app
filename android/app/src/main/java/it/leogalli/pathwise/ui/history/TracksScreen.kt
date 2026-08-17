package it.leogalli.pathwise.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.leogalli.pathwise.domain.model.Track
import it.leogalli.pathwise.ui.theme.Night900
import it.leogalli.pathwise.ui.theme.Rose400
import it.leogalli.pathwise.ui.theme.TextSecondary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Storico delle tracce registrate, letto in tempo reale da Room (Flow).
 */
@Composable
fun TracksScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()

    if (tracks.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Nessuna traccia registrata.", style = MaterialTheme.typography.titleMedium)
            Text(
                "Avvia una registrazione dalla mappa per vedere qui i tuoi percorsi.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(tracks, key = { it.id }) { track ->
            TrackCard(track = track, onDelete = { viewModel.delete(track.id) })
        }
    }
}

@Composable
private fun TrackCard(track: Track, onDelete: () -> Unit) {
    val date = Instant.ofEpochMilli(track.startedAtEpochMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))

    Card(colors = CardDefaults.cardColors(containerColor = Night900)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(track.name, fontWeight = FontWeight.Bold)
                Text(date, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(
                    "%.1f km · D+ %.0f m · %d min · %d kcal".format(
                        track.distanceMeters / 1000, track.ascentM, track.durationMinutes, track.caloriesKcal,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Elimina", tint = Rose400)
            }
        }
    }
}
