package it.leogalli.pathwise.domain.repository

import it.leogalli.pathwise.domain.model.Track
import kotlinx.coroutines.flow.Flow

/**
 * Contratto del dominio per la persistenza delle tracce.
 * L'implementazione (Room) vive nello strato data e viene iniettata via Hilt.
 */
interface TrackRepository {

    /** Tutte le tracce, dalla più recente. Reattivo: la UI segue il Flow. */
    fun observeTracks(): Flow<List<Track>>

    suspend fun getTrack(id: Long): Track?

    /** @return id della traccia salvata */
    suspend fun saveTrack(track: Track): Long

    suspend fun deleteTrack(id: Long)
}
