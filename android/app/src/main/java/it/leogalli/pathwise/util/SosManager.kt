package it.leogalli.pathwise.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.util.Locale

/**
 * Modulo SOS: genera un SMS di emergenza con le coordinate GPS
 * (latitudine, longitudine, altitudine) e un link Google Maps,
 * pensato per i soccorsi in assenza di rete dati.
 *
 * NOTA: si usa ACTION_SENDTO (smsto:) che apre l'app SMS precompilata
 * SENZA richiedere il permesso SEND_SMS: l'utente preme un solo invio.
 * Il link maps.google.com/?q=lat,lng funziona anche offline se l'utente
 * ha aperto almeno una volta Google Maps (cache della zona).
 */
object SosManager {

    private const val DEFAULT_EMERGENCY = "112"

    /**
     * Compone il corpo dell'SMS:
     *   SOS PathWise · 45.8341 N, 6.8653 E · Alt. 2312 m
     *   https://maps.google.com/?q=45.8341,6.8653
     */
    fun buildSosBody(latitude: Double, longitude: Double, altitudeM: Double): String = buildString {
        append("SOS PathWise!\n")
        append(formatCoordinate(latitude, isLat = true))
        append(' ')
        append(formatCoordinate(longitude, isLat = false))
        if (altitudeM > 0) append(String.format(Locale.ITALY, "\nAlt. %.0f m", altitudeM))
        append("\nLink: https://maps.google.com/?q=")
        append(String.format(Locale.US, "%.6f", latitude))
        append(',')
        append(String.format(Locale.US, "%.6f", longitude))
    }

    /** Es. 45°50'02" N (DMS compatto) per la leggibilità dei soccorritori. */
    fun formatCoordinate(value: Double, isLat: Boolean): String {
        val hemisphere = if (isLat) if (value >= 0) "N" else "S" else if (value >= 0) "E" else "W"
        val abs = kotlin.math.abs(value)
        val degrees = abs.toInt()
        val minutes = ((abs - degrees) * 60).toInt()
        val seconds = ((abs - degrees) * 60 - minutes) * 60
        return String.format(Locale.ITALY, "%d°%02d'%02d\" %s", degrees, minutes, seconds.toInt(), hemisphere)
    }

    /**
     * Apre l'app SMS con destinatario e corpo precompilati.
     * @param recipients numeri separati da virgola (es. "112,3331234567")
     */
    fun openSmsComposer(context: Context, recipients: String, body: String): Boolean {
        val uri = Uri.parse("smsto:${recipients.trim().removePrefix("+")}")
        return try {
            val intent = Intent(Intent.ACTION_SENDTO, uri)
                .putExtra("sms_body", body)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false // nessuna app SMS disponibile
        }
    }

    /** Apre il compositore verso il numero di emergenza predefinito. */
    fun openSmsComposer(context: Context, body: String): Boolean =
        openSmsComposer(context, DEFAULT_EMERGENCY, body)

    /** Chiamata di emergenza diretta al 112. */
    fun openEmergencyCall(context: Context, number: String = DEFAULT_EMERGENCY): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }
}
