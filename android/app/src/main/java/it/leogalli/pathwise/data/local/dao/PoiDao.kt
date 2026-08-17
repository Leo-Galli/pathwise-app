package it.leogalli.pathwise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.leogalli.pathwise.data.local.entity.PoiEntity
import it.leogalli.pathwise.domain.model.PoiType
import kotlinx.coroutines.flow.Flow

/**
 * DAO dei punti di interesse (layer della mappa).
 */
@Dao
interface PoiDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pois: List<PoiEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(poi: PoiEntity): Long

    @Query("SELECT * FROM pois ORDER BY type, name")
    fun observeAll(): Flow<List<PoiEntity>>

    @Query("SELECT * FROM pois WHERE type = :type ORDER BY name")
    fun observeByType(type: PoiType): Flow<List<PoiEntity>>

    @Query("SELECT COUNT(*) FROM pois")
    suspend fun count(): Int

    @Query("DELETE FROM pois")
    suspend fun clear()
}
