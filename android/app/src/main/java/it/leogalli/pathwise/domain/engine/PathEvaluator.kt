package it.leogalli.pathwise.domain.engine

import it.leogalli.pathwise.domain.model.FeasibilityStatus
import it.leogalli.pathwise.domain.model.FitnessLevel
import it.leogalli.pathwise.domain.model.GroupProfile
import it.leogalli.pathwise.domain.model.PathEvaluation
import it.leogalli.pathwise.domain.model.SafetyWarning
import it.leogalli.pathwise.domain.model.TerrainType
import it.leogalli.pathwise.domain.model.TrailGeometry
import it.leogalli.pathwise.domain.model.WarningSeverity
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max
import kotlin.math.min

/**
 * ─────────────────────────────────────────────────────────────────────────────
 *  PathWise Engine — Motore di valutazione e fattibilità.
 *
 *  Implementazione nativa (Kotlin) degli stessi identici algoritmi che girano
 *  sulla landing page (web/lib/engine.ts): i numeri sono intercambiabili.
 *
 *  • TEMPO        → Regola di Naismith modificata da Langmuir:
 *                    4 km/h in piano, +1 h ogni 600 m di D+, −10 min ogni
 *                    300 m di D− (sconto mai oltre il 30% del tempo base),
 *                    +25% di margine come da prassi Langmuir.
 *  • VELOCITÀ     → tarata sul partecipante PIÙ VULNERABILE (fattore età
 *                    massimo del gruppo) × preparazione × peso zaino × terreno.
 *  • CALORIE      → Equazione di Pandolf (1977) per la marcia con carico:
 *                    M = 1.5W + 2(W+L)(L/W)² + η(W+L)(1.5V² + 0.35VG)
 *                    con W = massa corporea, L = carico, V = velocità (m/s),
 *                    G = pendenza (%), η = fattore di terreno. Conversione
 *                    1 W = 0.01433 kcal/min + metabolismo basale (1 kcal/kg/h).
 *                    La discesa pesa al 60% del costo di pianura.
 *  • FATTIBILITÀ  → regole di sicurezza per bambini, anziani, cani, fobia
 *                    delle altezze e ore di luce stimate.
 * ─────────────────────────────────────────────────────────────────────────────
 */
object PathEvaluator {

    /**
     * Fattore di velocità per fascia d'età.
     * Sotto i 16 e sopra i 60 anni la velocità sostenibile cala;
     * il motore applica il peggiore del gruppo.
     */
    fun ageFactor(age: Int): Double = when {
        age < 8 -> 1.55
        age < 12 -> 1.35
        age < 16 -> 1.15
        age <= 60 -> 1.00
        age <= 70 -> 1.20
        age <= 80 -> 1.45
        else -> 1.70
    }

    /**
     * Durata "pulita" in ore secondo Langmuir, prima di margini e fattori.
     */
    fun langmuirTimeHours(distanceKm: Double, ascentM: Double, descentM: Double): Double {
        val base = distanceKm / 4.0 + ascentM / 600.0
        // −10 min ogni 300 m di discesa
        val credit = (descentM / 300.0) * (10.0 / 60.0)
        // lo sconto non può superare il 30% del tempo base (protezione anti-assurdo)
        val cap = base * 0.3
        return base - min(credit, cap)
    }

    /**
     * Potenza metabolica in watt secondo Pandolf (1977).
     *
     * @param bodyKg massa corporea W
     * @param loadKg carico trasportato L
     * @param speedMs velocità in m/s
     * @param gradePct pendenza in %
     * @param eta fattore di terreno η
     */
    fun pandolfWatts(bodyKg: Double, loadKg: Double, speedMs: Double, gradePct: Double, eta: Double): Double =
        1.5 * bodyKg +
            2.0 * (bodyKg + loadKg) * (loadKg / bodyKg) * (loadKg / bodyKg) +
            eta * (bodyKg + loadKg) * (1.5 * speedMs * speedMs + 0.35 * speedMs * gradePct)

    /**
     * Valutazione completa del percorso per il gruppo dato.
     */
    fun evaluate(profile: GroupProfile, geometry: TrailGeometry): PathEvaluation {

        // ── 1. TEMPO STIMATO ─────────────────────────────────────────────────
        // Taratura sul più vulnerabile: fattore età peggiore del gruppo.
        // (1 until 120: identico al filtro del motore TypeScript `a > 0 && a < 120`)
        val worstAgeFactor = profile.ages
            .filter { it in 1 until 120 }
            .maxOfOrNull { ageFactor(it) }
            ?: 1.0

        // Peso dello zaino: +0.8% di tempo per ogni kg oltre i 5 kg.
        val loadFactor = 1.0 + max(0.0, profile.backpackWeightKg - 5.0) * 0.008

        val groupFactor = worstAgeFactor *
            profile.fitnessLevel.timeFactor *
            loadFactor *
            geometry.terrain.speedFactor

        val rawHours = langmuirTimeHours(geometry.distanceKm, geometry.ascentM, geometry.descentM)
        // +25% di margine Langmuir, con floor di 15 minuti
        val totalHours = max(rawHours * groupFactor * 1.25, 0.25)
        // Math.round: identico al `Math.round` del motore TypeScript
        val estimatedTimeMinutes = Math.round(totalHours * 60).toInt()
        val averageSpeedKmh = geometry.distanceKm / totalHours

        // ── 2. CALORIE (Pandolf) ────────────────────────────────────────────
        // Modello a tratti: metà distanza in salita (pendenza effettiva),
        // metà in discesa (costo ridotto al 60%).
        val speedMs = averageSpeedKmh / 3.6
        val bodyKg = profile.averageBodyWeightKg
        val loadKg = profile.backpackWeightKg
        val climbKm = geometry.distanceKm * 0.5
        val gradeUpPct = if (climbKm > 0) (geometry.ascentM / (climbKm * 1000.0)) * 100.0 else 0.0
        val eta = geometry.terrain.pandolfEta

        val wattsUp = pandolfWatts(bodyKg, loadKg, speedMs, gradeUpPct, eta)
        val wattsDown = pandolfWatts(bodyKg, loadKg, speedMs, 0.0, eta) * 0.6

        val halfMinutes = estimatedTimeMinutes / 2.0
        // 1 W = 0.01433 kcal/min
        val metabolicKcal = (wattsUp * halfMinutes + wattsDown * halfMinutes) * 0.01433
        // Metabolismo basale ≈ 1 kcal/kg/h
        val rmrKcalPerMin = bodyKg / 60.0
        // Math.round: identico al motore TypeScript
        val caloriesKcal = Math.round(metabolicKcal + rmrKcalPerMin * estimatedTimeMinutes).toInt()

        val kcalPerMin = if (estimatedTimeMinutes > 0) caloriesKcal.toDouble() / estimatedTimeMinutes else 0.0
        val averageMet = round1(kcalPerMin / rmrKcalPerMin)

        // ── 3. AVVISI DI SICUREZZA ──────────────────────────────────────────
        val warnings = buildList {
            val child = profile.hasChildren
            val ferrata = geometry.terrain == TerrainType.FERRATA
            val rock = geometry.terrain == TerrainType.ROCCIA

            if (child && geometry.maxGradePercent > 30) {
                val altitude = if (geometry.maxAltitudeM > 2200) " a ${Math.round(geometry.maxAltitudeM).toInt()} m" else ""
                add(SafetyWarning(
                    message = "Tratto >${Math.round(geometry.maxGradePercent).toInt()}%$altitude sconsigliato a under 10",
                    severity = WarningSeverity.WARNING,
                ))
            }
            if (ferrata && profile.hasMinorsUnder14) {
                add(SafetyWarning(
                    message = "Ferrata vietata ai minori di 14 anni (imbrago obbligatorio)",
                    severity = WarningSeverity.BLOCK,
                ))
            }
            if (child && geometry.maxAltitudeM > 2200) {
                add(SafetyWarning(
                    message = "Quota oltre 2.200 m: rischio di mal di montagna (AMS) nei bambini",
                    severity = WarningSeverity.WARNING,
                ))
            }
            if (child && geometry.distanceKm > 10) {
                add(SafetyWarning(
                    message = "Distanza >10 km impegnativa per i più piccoli",
                    severity = WarningSeverity.WARNING,
                ))
            }
            if (child && geometry.ascentM > 700) {
                add(SafetyWarning(
                    message = "Dislivello >700 m: prevedere soste frequenti",
                    severity = WarningSeverity.WARNING,
                ))
            }
            if (profile.hasElderly && geometry.maxGradePercent > 35) {
                add(SafetyWarning(
                    message = "Pendenze >${Math.round(geometry.maxGradePercent).toInt()}%: rischio cadute per i meno giovani",
                    severity = WarningSeverity.WARNING,
                ))
            }
            if (profile.hasElderly && geometry.maxAltitudeM > 2800) {
                add(SafetyWarning(
                    message = "Quota oltre 2.800 m: attenzione a AMS e temperatura",
                    severity = WarningSeverity.WARNING,
                ))
            }
            if (profile.hasElderly && geometry.distanceKm > 15) {
                add(SafetyWarning(
                    message = "Distanza >15 km: prevedere piano B e punti di fuga",
                    severity = WarningSeverity.WARNING,
                ))
            }
            if (profile.hasVertigoPhobia && (ferrata || rock || geometry.maxGradePercent > 45)) {
                add(SafetyWarning(
                    message = "Tratti esposti rilevati: sconsigliato in caso di fobia delle altezze",
                    severity = WarningSeverity.WARNING,
                ))
            }
            if (profile.hasDogs && ferrata) {
                add(SafetyWarning(
                    message = "Ferrata vietata ai cani",
                    severity = WarningSeverity.BLOCK,
                ))
            }
            if (profile.hasDogs && rock) {
                add(SafetyWarning(
                    message = "Terreno roccioso: previste scarpette protettive per il cane",
                    severity = WarningSeverity.WARNING,
                ))
            }
            if (estimatedTimeMinutes > 480) {
                // Virgola decimale: identica al `.replace(".", ",")` del motore TS.
                // Nuova BigDecimal(d) usa il VALORE ESATTO del double e arrotonda
                // con HALF_UP, replicando fedelmente toFixed(1) del motore TS
                // (verificato empiricamente: 897 min → 14,9 h, non 15,0 h).
                // String.format("%.1f") NON va bene: arrotonda la rappresentazione
                // più corta (14,95 → 15,0), rompendo la parità con il web.
                val hours = BigDecimal(estimatedTimeMinutes / 60.0)
                    .setScale(1, RoundingMode.HALF_UP)
                    .toPlainString()
                    .replace('.', ',')
                add(SafetyWarning(
                    message = "Durata stimata ~$hours h: rischio di ore di luce insufficienti",
                    severity = WarningSeverity.WARNING,
                ))
            }
        }

        // ── 4. VERDETTO ─────────────────────────────────────────────────────
        val status = when {
            warnings.any { it.severity == WarningSeverity.BLOCK } -> FeasibilityStatus.NOT_RECOMMENDED
            warnings.isNotEmpty() -> FeasibilityStatus.NEEDS_TRAINING
            else -> FeasibilityStatus.SUITABLE_ALL
        }

        return PathEvaluation(
            status = status,
            estimatedTimeMinutes = estimatedTimeMinutes,
            caloriesKcal = caloriesKcal,
            averageMet = averageMet,
            averageSpeedKmh = round1(averageSpeedKmh),
            warnings = warnings,
        )
    }

    /** Arrotonda a una cifra decimale (equivalente a `toFixed(1)` del TS). */
    private fun round1(value: Double): Double = Math.round(value * 10) / 10.0
}
