package it.leogalli.pathwise.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import it.leogalli.pathwise.domain.model.PoiType
import it.leogalli.pathwise.domain.model.Track

/**
 * Entità Room per una traccia registrata.
 * I punti GPS sono conservati come JSON ([Track.points]).
 */
@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val startedAtEpochMillis: Long,
    val durationMinutes: Int,
    val distanceMeters: Double,
    val ascentM: Double,
    val descentM: Double,
    val maxAltitudeM: Double,
    val caloriesKcal: Int,
    val steps: Int,
    val pointsJson: String,
)

/**
 * Punto di interesse geolocalizzato. L'enum [PoiType] viene
 * serializzato automaticamente da Room come String.
 */
@Entity(tableName = "pois", indices = [Index("type")])
data class PoiEntity(
    @PrimaryKey(autoGenerate = true)
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

/**
 * Regione cartografica scaricata per la fruizione offline.
 */
@Entity(tableName = "map_regions")
data class MapRegionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val minLat: Double,
    val maxLat: Double,
    val minLng: Double,
    val maxLng: Double,
    val downloadedAtEpochMillis: Long,
    val sizeBytes: Long,
)
