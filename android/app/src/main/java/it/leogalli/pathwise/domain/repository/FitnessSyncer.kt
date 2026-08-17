package it.leogalli.pathwise.domain.repository

import it.leogalli.pathwise.domain.model.Track

/**
 * Porta del dominio verso le app fitness (implementata da
 * [it.leogalli.pathwise.util.HealthConnectManager]).
 *
 * Tenere l'interfaccia nel dominio (e non l'implementazione) rispetta la
 * Clean Architecture: lo strato di business non conosce i dettagli di
 * Health Connect, può essere testato con un fake e l'implementazione può
 * essere sostituita (es. Google Fit legacy).
 */
interface FitnessSyncer {

    suspend fun arePermissionsGranted(): Boolean

    /** Scrive calorie, passi, distanza e sessione di trekking completata. */
    suspend fun writeCompletedTrek(track: Track)
}
