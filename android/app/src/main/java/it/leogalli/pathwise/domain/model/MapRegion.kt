package it.leogalli.pathwise.domain.model

/**
 * Regione cartografica scaricata per la fruizione offline.
 * I bounding box in latitudine/longitudine delimitano l'area:
 * il client mappa le richieste di tile all'interno della regione.
 */
data class MapRegion(
    val id: Long = 0L,
    val name: String,
    val minLat: Double,
    val maxLat: Double,
    val minLng: Double,
    val maxLng: Double,
    /** Epoch millis del download. */
    val downloadedAtEpochMillis: Long,
    val sizeBytes: Long,
) {
    /** True se il punto cade all'interno della regione scaricata. */
    fun contains(latitude: Double, longitude: Double): Boolean =
        latitude in minLat..maxLat && longitude in minLng..maxLng
}
