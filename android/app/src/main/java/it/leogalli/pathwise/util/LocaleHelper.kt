package it.leogalli.pathwise.util

import android.content.Context
import android.content.res.Configuration
import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Override della lingua IN-APP senza riavviare lo smartphone.
 *
 * - API 33+  → [LocaleManager.applicationLocales]: la lingua viene applicata
 *              dal sistema alla activity corrente e a quelle future, live.
 * - API <33  → attachBaseContext() di MainActivity ri-basa il context sulla
 *              lingua salvata (cache scritta da [applyLocale]).
 *
 * I codici supportati: it, en, de, fr, es.
 */
object LocaleHelper {

    /** Lingue supportate, in ordine di visualizzazione. */
    val SUPPORTED_LANGUAGES = listOf("it", "en", "de", "fr", "es")

    /**
     * Ultima lingua applicata. Cache sincrona per attachBaseContext() (API < 33),
     * scritta all'avvio (Application) e a ogni cambio da parte dell'utente.
     */
    @Volatile
    var lastAppliedLanguage: String = "it"
        private set

    fun isSupported(code: String): Boolean = SUPPORTED_LANGUAGES.contains(code)

    fun toLocale(code: String): Locale = Locale.forLanguageTag(code.takeIf(::isSupported) ?: "it")

    /**
     * Applica la lingua a livello di sistema.
     * Da chiamare in MainActivity quando cambia l'impostazione.
     */
    fun applyLocale(context: Context, languageCode: String) {
        if (!isSupported(languageCode)) return
        lastAppliedLanguage = languageCode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API 33+: il sistema applica la lingua alla activity corrente (live)
            val localeManager = context.getSystemService(Context.LOCALE_SERVICE) as LocaleManager
            localeManager.applicationLocales = LocaleList.forLanguageTags(languageCode)
        }
        // API < 33: l'activity viene ricreata con il base context in lingua
        // (override di attachBaseContext in MainActivity) — gestito a monte.
    }

    /**
     * Context con la lingua applicata, da usare in attachBaseContext()
     * dell'activity (API < 33): le risorse vengono risolte nella lingua salvata
     * senza dipendere dal read asincrono del DataStore.
     */
    @Suppress("DEPRECATION")
    fun localeAwareContext(context: Context, languageCode: String): Context {
        val locale = toLocale(languageCode)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
