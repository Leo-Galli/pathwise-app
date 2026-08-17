package it.leogalli.pathwise.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import it.leogalli.pathwise.data.local.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO delle tracce registrate.
 * Tutte le query di lettura esposte come [Flow] sono reattive:
 * la UI si aggiorna automaticamente a ogni inserimento/cancellazione.
 */
@Dao
interface TrackDao {

    @Insert
    suspend fun insert(track: TrackEntity): Long

    @Query("SELECT * FROM tracks ORDER BY startedAtEpochMillis DESC")
    fun observeAll(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun getById(id: Long): TrackEntity?

    @Query("DELETE FROM tracks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun count(): Int
}
