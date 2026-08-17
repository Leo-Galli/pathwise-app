package it.leogalli.pathwise.domain.repository

import it.leogalli.pathwise.domain.model.Poi
import it.leogalli.pathwise.domain.model.PoiType
import kotlinx.coroutines.flow.Flow

/**
 * Contratto del dominio per i punti di interesse (layer della mappa).
 */
interface PoiRepository {

    fun observePois(): Flow<List<Poi>>

    fun observePoisByType(type: PoiType): Flow<List<Poi>>

    /** Popola il database con i POI demo al primo avvio (no-op se già popolato). */
    suspend fun seedIfEmpty()
}
