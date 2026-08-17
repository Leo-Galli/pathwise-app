package it.leogalli.pathwise.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Impostazioni dell'app persistite con Jetpack DataStore.
 * La lingua viene applicata senza riavviare lo smartphone (vedi LocaleHelper).
 */
interface SettingsRepository {

    /** Codice lingua attivo (es. "it", "en", "de", "fr", "es"). */
    val language: Flow<String>

    /** Numeri destinatari del modulo SOS. */
    val sosRecipients: Flow<List<String>>

    /** Numero di emergenza predefinito (default 112). */
    val emergencyNumber: Flow<String>

    suspend fun setLanguage(code: String)

    suspend fun setSosRecipients(recipients: List<String>)

    suspend fun setEmergencyNumber(number: String)
}
