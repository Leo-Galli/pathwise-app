package it.leogalli.pathwise.domain.model

/**
 * Traccia di trekking registrata (o importata da GPX).
 * I timestamp sono epoch millis (UTC) per evitare dipendenze di data/ora.
 */
data class Track(
    val id: Long = 0L,
    val name: String,
    /** Epoch millis di inizio registrazione. */
    val startedAtEpochMillis: Long,
    val durationMinutes: Int,
    val distanceMeters: Double,
    val ascentM: Double,
    val descentM: Double,
    val maxAltitudeM: Double,
    val caloriesKcal: Int,
    val steps: Int,
    val points: List<TrackPoint>,
)

/**
 * Singolo punto GPS campionato.
 * @property altitudeM altitudine dal GPS/barometro (o 0 se ignota)
 */
data class TrackPoint(
    val latitude: Double,
    val longitude: Double,
    val altitudeM: Double,
    val timestampEpochMillis: Long,
) {
    /** Calcolo della distanza planimetrica (formula di Haversine) dal punto precedente. */
    fun distanceToMeters(other: TrackPoint): Double {
        val earthRadiusM = 6_371_000.0
        val dLat = Math.toRadians(other.latitude - latitude)
        val dLng = Math.toRadians(other.longitude - longitude)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(other.latitude)) *
            Math.sin(dLng / 2) * Math.sin(dLng / 2)
        return 2 * earthRadiusM * Math.asin(Math.sqrt(a))
    }
}
