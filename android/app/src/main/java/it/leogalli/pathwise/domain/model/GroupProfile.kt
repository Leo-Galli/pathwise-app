package it.leogalli.pathwise.domain.model

/**
 * Profilo del gruppo escursionistico.
 *
 * Il motore si tarerà SEMPRE sul partecipante più vulnerabile:
 * l'età minima guida le soglie dei bambini, l'età massima quelle degli anziani,
 * mentre il fattore velocità peggiore rallenta l'intero gruppo.
 *
 * @property ages età (in anni) di ogni partecipante, es. [7, 39, 41, 67]
 * @property fitnessLevel livello di preparazione atletica medio del gruppo
 * @property averageBodyWeightKg peso medio dei partecipanti (per il calcolo calorico)
 * @property backpackWeightKg peso medio dello zaino trasportato
 * @property hasDogs presenza di cani nel gruppo
 * @property hasVertigoPhobia presenza di fobia delle altezze
 */
data class GroupProfile(
    val ages: List<Int>,
    val fitnessLevel: FitnessLevel = FitnessLevel.MEDIO,
    val averageBodyWeightKg: Double = 70.0,
    val backpackWeightKg: Double = 8.0,
    val hasDogs: Boolean = false,
    val hasVertigoPhobia: Boolean = false,
) {
    /** Età minima del gruppo (default 30 se vuoto). */
    val minAge: Int get() = ages.filter { it in 1 until 120 }.minOrNull() ?: 30

    /** Età massima del gruppo (default 30 se vuoto). */
    val maxAge: Int get() = ages.filter { it in 1 until 120 }.maxOrNull() ?: 30

    /** True se nel gruppo c'è almeno un bambino sotto i 10 anni. */
    val hasChildren: Boolean get() = minAge < 10

    /** True se c'è almeno un minore di 14 anni (rilevante per le ferrate). */
    val hasMinorsUnder14: Boolean get() = minAge < 14

    /** True se c'è almeno un over 65. */
    val hasElderly: Boolean get() = maxAge > 65
}

/**
 * Livello di preparazione atletica.
 * @property timeFactor moltiplicatore del tempo base (1.0 = medio).
 */
enum class FitnessLevel(val timeFactor: Double) {
    PRINCIPIANTE(1.30),
    MEDIO(1.00),
    ALLENATO(0.85)
}
