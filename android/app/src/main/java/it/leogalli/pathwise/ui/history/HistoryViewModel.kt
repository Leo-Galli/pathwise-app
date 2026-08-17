package it.leogalli.pathwise.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.leogalli.pathwise.domain.model.Track
import it.leogalli.pathwise.domain.repository.TrackRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel dello storico: espone le tracce come StateFlow reattivo
 * (Room notifica automaticamente ogni modifica).
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
) : ViewModel() {

    val tracks: StateFlow<List<Track>> = trackRepository.observeTracks()
        .map { list -> list.sortedByDescending { it.startedAtEpochMillis } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(id: Long) {
        viewModelScope.launch { trackRepository.deleteTrack(id) }
    }
}
