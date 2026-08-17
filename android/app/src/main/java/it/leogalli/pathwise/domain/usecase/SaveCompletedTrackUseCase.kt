package it.leogalli.pathwise.domain.usecase

import it.leogalli.pathwise.domain.model.Track
import it.leogalli.pathwise.domain.repository.FitnessSyncer
import it.leogalli.pathwise.domain.repository.TrackRepository
import javax.inject.Inject

/**
 * Persiste la traccia completata e la sincronizza automaticamente con le
 * app fitness (Health Connect) al termine della registrazione.
 *
 * La sincronizzazione è best-effort: un'eventuale indisponibilità dei
 * permessi o del provider non deve MAI far fallire il salvataggio locale.
 */
class SaveCompletedTrackUseCase @Inject constructor(
    private val trackRepository: TrackRepository,
    private val fitnessSyncer: FitnessSyncer,
) {
    /**
     * @return l'id della traccia salvata
     */
    suspend operator fun invoke(track: Track): Long {
        val id = trackRepository.saveTrack(track)
        if (fitnessSyncer.arePermissionsGranted()) {
            runCatching { fitnessSyncer.writeCompletedTrek(track) }
        }
        return id
    }
}
