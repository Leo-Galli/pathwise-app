package it.leogalli.pathwise.data.local

import it.leogalli.pathwise.domain.model.Poi
import it.leogalli.pathwise.domain.model.PoiType

/**
 * POI dimostrativi inseriti al primo avvio, così la mappa è subito viva.
 * In produzione questi dati arrivano da un backend o da un file GPX offline;
 * la struttura rimane identica.
 */
object PoiSeed {

    fun demoPois(): List<Poi> = listOf(
        Poi(
            type = PoiType.RIFUGIO, name = "Rifugio Alpe Alta", latitude = 45.8341, longitude = 6.8653,
            altitudeM = 2312.0, description = "Rifugio alpino con cucina tipica", isOpen = true, capacity = 46, phone = "+39 0165 123456",
        ),
        Poi(
            type = PoiType.RIFUGIO, name = "Rifugio La Madonnina", latitude = 45.8490, longitude = 6.9021,
            altitudeM = 1985.0, description = "Aperto da giugno a settembre", isOpen = false, capacity = 32, phone = "+39 0165 654321",
        ),
        Poi(
            type = PoiType.BIVACCO, name = "Bivacco F.lli Calvi", latitude = 45.8611, longitude = 6.9380,
            altitudeM = 2730.0, description = "Bivacco non custodito, 6 posti, senza acqua", isOpen = true, capacity = 6,
        ),
        Poi(
            type = PoiType.PANORAMA, name = "Belvedere Cima Bianca", latitude = 45.8275, longitude = 6.8810,
            altitudeM = 2550.0, description = "Panorama a 360° sul massiccio del Bianco",
        ),
        Poi(
            type = PoiType.SORGENTE, name = "Fontana del Bricco", latitude = 45.8402, longitude = 6.8450,
            altitudeM = 1780.0, description = "Acqua potabile tutto l'anno",
        ),
        Poi(
            type = PoiType.PERICOLO, name = "Tratto esposto al Colle", latitude = 45.8510, longitude = 6.9230,
            altitudeM = 2650.0, description = "Tratto esposto con cavo d'acciaio: attenzione in caso di nebbia",
        ),
    )
}
