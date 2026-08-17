package it.leogalli.pathwise.domain.model

/**
 * Geometria del tracciato, da GPX o da inserimento manuale.
 *
 * @property distanceKm distanza totale in km
 * @property ascentM dislivello positivo (D+) in metri
 * @property descentM dislivello negativo (D−) in metri
 * @property maxAltitudeM altitudine massima raggiunta
 * @property avgGradePercent pendenza media (%) sull'intero tracciato
 * @property maxGradePercent pendenza massima rilevata (tratti singoli)
 * @property terrain tipologia di terreno prevalente
 */
data class TrailGeometry(
    val distanceKm: Double,
    val ascentM: Double,
    val descentM: Double,
    val maxAltitudeM: Double,
    val avgGradePercent: Double,
    val maxGradePercent: Double,
    val terrain: TerrainType,
)

/** Tipologia di terreno con i relativi fattori di correzione. */
enum class TerrainType(
    /** Moltiplicatore del tempo stimato (sentiero = riferimento). */
    val speedFactor: Double,
    /** Fattore η di Pandolf per il costo metabolico. */
    val pandolfEta: Double,
) {
    SENTIERO(1.00, 1.1),
    TRAIL_CAI_EE(1.15, 1.5),
    ROCCIA(1.35, 2.5),
    FERRATA(1.50, 3.0),
}

/** Verdetto di fattibilità del percorso per il gruppo. */
enum class FeasibilityStatus {
    /** Nessun avviso: percorso adatto a tutti i membri. */
    SUITABLE_ALL,

    /** Presenza di avvisi: consigliabile allenamento/attenzione. */
    NEEDS_TRAINING,

    /** Almeno un vincolo bloccante: percorso sconsigliato. */
    NOT_RECOMMENDED,
}

/** Severità di un singolo avviso di sicurezza. */
enum class WarningSeverity { INFO, WARNING, BLOCK }

/**
 * Avviso di sicurezza generato dal motore.
 * I messaggi BLOCK comportano lo stato [FeasibilityStatus.NOT_RECOMMENDED].
 */
data class SafetyWarning(
    val message: String,
    val severity: WarningSeverity,
)

/**
 * Output completo del [it.leogalli.pathwise.domain.engine.PathEvaluator].
 *
 * @property status verdetto di fattibilità
 * @property estimatedTimeMinutes tempo stimato (Naismith-Langmuir + margine 25%)
 * @property caloriesKcal calorie totali stimate (equazione di Pandolf)
 * @property averageMet intensità media in MET (multiplicatore del metabolismo basale)
 * @property averageSpeedKmh velocità media effettiva risultante
 * @property warnings avvisi di sicurezza ordinati per severità
 */
data class PathEvaluation(
    val status: FeasibilityStatus,
    val estimatedTimeMinutes: Int,
    val caloriesKcal: Int,
    val averageMet: Double,
    val averageSpeedKmh: Double,
    val warnings: List<SafetyWarning>,
)
