package it.leogalli.pathwise.data.local

import androidx.room.TypeConverter
import it.leogalli.pathwise.domain.model.TrackPoint
import org.json.JSONArray
import org.json.JSONObject

/**
 * Convertitori Room per i tipi non primitivi.
 *
 * I punti del tracciato vengono serializzati in JSON compatto
 * (formato array di oggetti) usando org.json, presente nella
 * libreria standard Android — zero dipendenze aggiuntive.
 */
class Converters {

    @TypeConverter
    fun trackPointsToJson(points: List<TrackPoint>): String {
        val array = JSONArray()
        points.forEach { p ->
            array.put(
                JSONObject()
                    .put("la", p.latitude)
                    .put("lo", p.longitude)
                    .put("al", p.altitudeM)
                    .put("t", p.timestampEpochMillis)
            )
        }
        return array.toString()
    }

    @TypeConverter
    fun jsonToTrackPoints(json: String): List<TrackPoint> {
        if (json.isBlank()) return emptyList()
        val array = JSONArray(json)
        return buildList {
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                add(
                    TrackPoint(
                        latitude = o.getDouble("la"),
                        longitude = o.getDouble("lo"),
                        altitudeM = o.optDouble("al", 0.0),
                        timestampEpochMillis = o.getLong("t"),
                    )
                )
            }
        }
    }
}
