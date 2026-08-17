package it.leogalli.pathwise.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import it.leogalli.pathwise.data.local.PathWiseDatabase
import it.leogalli.pathwise.data.local.dao.MapRegionDao
import it.leogalli.pathwise.data.local.dao.PoiDao
import it.leogalli.pathwise.data.local.dao.TrackDao
import javax.inject.Singleton

/**
 * Modulo Hilt per Room: database e DAO come singleton dell'app.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PathWiseDatabase =
        PathWiseDatabase.getInstance(context)

    @Provides
    fun provideTrackDao(db: PathWiseDatabase): TrackDao = db.trackDao()

    @Provides
    fun providePoiDao(db: PathWiseDatabase): PoiDao = db.poiDao()

    @Provides
    fun provideMapRegionDao(db: PathWiseDatabase): MapRegionDao = db.mapRegionDao()
}
