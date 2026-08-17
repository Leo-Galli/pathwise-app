package it.leogalli.pathwise.domain.model

/** Tipologia di punto di interesse, mappata sui layer attivabili della mappa. */
enum class PoiType {
    RIFUGIO,
    BIVACCO,
    PANORAMA,
    SORGENTE,
    PERICOLO,
}

/**
 * Punto di interesse geolocalizzato (layer della mappa).
 *
 * @property isOpen stato di apertura stagionale (rifugi)
 * @property capacity posti letto (rifugi/bivacchi)
 * @property phone contatto telefonico
 */
data class Poi(
    val id: Long = 0L,
    val type: PoiType,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val altitudeM: Double,
    val description: String? = null,
    val isOpen: Boolean? = null,
    val capacity: Int? = null,
    val phone: String? = null,
)
