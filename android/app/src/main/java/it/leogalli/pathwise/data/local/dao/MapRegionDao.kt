package it.leogalli.pathwise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.leogalli.pathwise.data.local.entity.MapRegionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO delle regioni offline scaricate.
 */
@Dao
interface MapRegionDao {

    @Insert
    suspend fun insert(region: MapRegionEntity): Long

    @Query("SELECT * FROM map_regions ORDER BY downloadedAtEpochMillis DESC")
    fun observeAll(): Flow<List<MapRegionEntity>>

    @Query("DELETE FROM map_regions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM map_regions WHERE :lat BETWEEN minLat AND maxLat AND :lng BETWEEN minLng AND maxLng LIMIT 1")
    suspend fun findContaining(lat: Double, lng: Double): MapRegionEntity?
}
