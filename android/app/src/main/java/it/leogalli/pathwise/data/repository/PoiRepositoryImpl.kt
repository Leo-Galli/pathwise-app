package it.leogalli.pathwise.data.repository

import it.leogalli.pathwise.data.local.PoiSeed
import it.leogalli.pathwise.data.local.dao.PoiDao
import it.leogalli.pathwise.data.local.entity.PoiEntity
import it.leogalli.pathwise.domain.model.Poi
import it.leogalli.pathwise.domain.model.PoiType
import it.leogalli.pathwise.domain.repository.PoiRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementazione Room di [PoiRepository].
 */
class PoiRepositoryImpl @Inject constructor(
    private val poiDao: PoiDao,
) : PoiRepository {

    override fun observePois(): Flow<List<Poi>> =
        poiDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observePoisByType(type: PoiType): Flow<List<Poi>> =
        poiDao.observeByType(type).map { list -> list.map { it.toDomain() } }

    override suspend fun seedIfEmpty() {
        if (poiDao.count() > 0) return
        poiDao.insertAll(PoiSeed.demoPois().map { it.toEntity() })
    }

    // ── Mapping ───────────────────────────────────────────────

    private fun PoiEntity.toDomain(): Poi = Poi(
        id = id,
        type = type,
        name = name,
        latitude = latitude,
        longitude = longitude,
        altitudeM = altitudeM,
        description = description,
        isOpen = isOpen,
        capacity = capacity,
        phone = phone,
    )

    private fun Poi.toEntity(): PoiEntity = PoiEntity(
        id = id,
        type = type,
        name = name,
        latitude = latitude,
        longitude = longitude,
        altitudeM = altitudeM,
        description = description,
        isOpen = isOpen,
        capacity = capacity,
        phone = phone,
    )
}
