package it.leogalli.pathwise.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import it.leogalli.pathwise.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** DataStore delle impostazioni (un solo file per tutta l'app). */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "pathwise_settings",
)

/**
 * Implementazione DataStore di [SettingsRepository].
 * Le preferenze sono esposte come Flow reattivi: la UI si aggiorna da sola.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsRepository {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val SOS_RECIPIENTS = stringSetPreferencesKey("sos_recipients")
        val EMERGENCY_NUMBER = stringPreferencesKey("emergency_number")
    }

    override val language: Flow<String> =
        context.settingsDataStore.data.map { it[Keys.LANGUAGE] ?: DEFAULT_LANGUAGE }

    override val sosRecipients: Flow<List<String>> =
        context.settingsDataStore.data.map {
            it[Keys.SOS_RECIPIENTS]?.sorted() ?: DEFAULT_SOS_RECIPIENTS
        }

    override val emergencyNumber: Flow<String> =
        context.settingsDataStore.data.map { it[Keys.EMERGENCY_NUMBER] ?: DEFAULT_EMERGENCY }

    override suspend fun setLanguage(code: String) {
        context.settingsDataStore.edit { it[Keys.LANGUAGE] = code }
    }

    override suspend fun setSosRecipients(recipients: List<String>) {
        context.settingsDataStore.edit { it[Keys.SOS_RECIPIENTS] = recipients.toSet() }
    }

    override suspend fun setEmergencyNumber(number: String) {
        context.settingsDataStore.edit { it[Keys.EMERGENCY_NUMBER] = number }
    }

    companion object {
        const val DEFAULT_LANGUAGE = "it"
        val DEFAULT_SOS_RECIPIENTS = listOf("112")
        const val DEFAULT_EMERGENCY = "112"
    }
}
