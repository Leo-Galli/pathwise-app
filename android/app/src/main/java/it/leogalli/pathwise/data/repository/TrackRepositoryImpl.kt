package it.leogalli.pathwise.data.repository

import it.leogalli.pathwise.data.local.Converters
import it.leogalli.pathwise.data.local.dao.TrackDao
import it.leogalli.pathwise.data.local.entity.TrackEntity
import it.leogalli.pathwise.domain.model.Track
import it.leogalli.pathwise.domain.repository.TrackRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementazione Room di [TrackRepository].
 * Il mapping entità ⇄ dominio è isolato qui per mantenere gli strati separati.
 */
class TrackRepositoryImpl @Inject constructor(
    private val trackDao: TrackDao,
) : TrackRepository {

    private val converters = Converters()

    override fun observeTracks(): Flow<List<Track>> =
        trackDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getTrack(id: Long): Track? =
        trackDao.getById(id)?.toDomain()

    override suspend fun saveTrack(track: Track): Long =
        trackDao.insert(track.toEntity())

    override suspend fun deleteTrack(id: Long) {
        trackDao.deleteById(id)
    }

    // ── Mapping ───────────────────────────────────────────────

    private fun TrackEntity.toDomain(): Track = Track(
        id = id,
        name = name,
        startedAtEpochMillis = startedAtEpochMillis,
        durationMinutes = durationMinutes,
        distanceMeters = distanceMeters,
        ascentM = ascentM,
        descentM = descentM,
        maxAltitudeM = maxAltitudeM,
        caloriesKcal = caloriesKcal,
        steps = steps,
        points = converters.jsonToTrackPoints(pointsJson),
    )

    private fun Track.toEntity(): TrackEntity = TrackEntity(
        id = id,
        name = name,
        startedAtEpochMillis = startedAtEpochMillis,
        durationMinutes = durationMinutes,
        distanceMeters = distanceMeters,
        ascentM = ascentM,
        descentM = descentM,
        maxAltitudeM = maxAltitudeM,
        caloriesKcal = caloriesKcal,
        steps = steps,
        pointsJson = converters.trackPointsToJson(points),
    )
}
