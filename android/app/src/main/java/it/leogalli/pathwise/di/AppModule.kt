package it.leogalli.pathwise.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.leogalli.pathwise.data.repository.PoiRepositoryImpl
import it.leogalli.pathwise.data.repository.SettingsRepositoryImpl
import it.leogalli.pathwise.data.repository.TrackRepositoryImpl
import it.leogalli.pathwise.domain.repository.FitnessSyncer
import it.leogalli.pathwise.domain.repository.PoiRepository
import it.leogalli.pathwise.domain.repository.SettingsRepository
import it.leogalli.pathwise.domain.repository.TrackRepository
import it.leogalli.pathwise.util.HealthConnectManager
import javax.inject.Singleton

/**
 * Binding dei contratti del dominio alle implementazioni concrete.
 * Clean Architecture: il dominio vede solo le interfacce.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindTrackRepository(impl: TrackRepositoryImpl): TrackRepository

    @Binds
    @Singleton
    abstract fun bindPoiRepository(impl: PoiRepositoryImpl): PoiRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindFitnessSyncer(impl: HealthConnectManager): FitnessSyncer
}
