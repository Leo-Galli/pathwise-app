package it.leogalli.pathwise.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import it.leogalli.pathwise.data.local.dao.MapRegionDao
import it.leogalli.pathwise.data.local.dao.PoiDao
import it.leogalli.pathwise.data.local.dao.TrackDao
import it.leogalli.pathwise.data.local.entity.MapRegionEntity
import it.leogalli.pathwise.data.local.entity.PoiEntity
import it.leogalli.pathwise.data.local.entity.TrackEntity

/**
 * Database Room offline-first di PathWise.
 *
 * Conserva localmente:
 *  - [TrackEntity]   → tracce registrate (punti GPX serializzati)
 *  - [PoiEntity]     → punti di interesse (rifugi, bivacchi, sorgenti…)
 *  - [MapRegionEntity] → regioni cartografiche scaricate per l'offline
 *
 * La strategia è offline-first: tutto ciò che è stato visto/scaricato
 * resta disponibile anche senza copertura di rete.
 */
@Database(
    entities = [
        TrackEntity::class,
        PoiEntity::class,
        MapRegionEntity::class,
    ],
    version = 1,
    exportSchema = false, // schema export attivabile per le migrazioni (vedi README)
)
@TypeConverters(Converters::class)
abstract class PathWiseDatabase : RoomDatabase() {

    abstract fun trackDao(): TrackDao
    abstract fun poiDao(): PoiDao
    abstract fun mapRegionDao(): MapRegionDao

    companion object {
        private const val DATABASE_NAME = "pathwise.db"

        @Volatile
        private var INSTANCE: PathWiseDatabase? = null

        /**
         * Singleton thread-safe. Il modulo Hilt ([DatabaseModule]) usa questo
         * factory; in produzione con Hilt il database vive per tutto il ciclo
         * dell'applicazione.
         */
        fun getInstance(context: Context): PathWiseDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PathWiseDatabase::class.java,
                    DATABASE_NAME,
                ).build().also { INSTANCE = it }
            }
    }
}
