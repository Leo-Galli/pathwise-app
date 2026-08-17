package it.leogalli.pathwise

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import it.leogalli.pathwise.domain.repository.SettingsRepository
import it.leogalli.pathwise.util.LocaleHelper
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Application PathWise.
 *
 * All'avvio applica la lingua salvata nelle impostazioni PRIMA che la UI
 * risolva le risorse: l'override è efficace senza riavviare lo smartphone.
 * Il runBlocking è voluto e limitato al primo read del DataStore (una tantum).
 */
@HiltAndroidApp
class PathWiseApplication : Application() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        val language = runBlocking { settingsRepository.language.first() }
        // La cache serve anche ad attachBaseContext() (API < 33)
        LocaleHelper.applyLocale(this, language)
    }
}
