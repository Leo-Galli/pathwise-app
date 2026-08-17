package it.leogalli.pathwise.domain.engine

import it.leogalli.pathwise.domain.model.FeasibilityStatus
import it.leogalli.pathwise.domain.model.FitnessLevel
import it.leogalli.pathwise.domain.model.GroupProfile
import it.leogalli.pathwise.domain.model.TerrainType
import it.leogalli.pathwise.domain.model.TrailGeometry
import it.leogalli.pathwise.domain.model.WarningSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * PARITÀ WEB ⇄ APP — Verifica che il motore Kotlin produca ESATTAMENTE gli
 * stessi output del motore TypeScript (web/lib/engine.ts).
 *
 * I valori "golden" qui sotto sono generati da:
 *   node scripts/engine-parity.ts
 *
 * Qualsiasi modifica a questi numeri rompe la corrispondenza tra la demo
 * web e l'app Android.
 */
class PathEvaluatorTest {

    private val defaultProfile = GroupProfile(ages = listOf(7, 39, 41, 67))
    private val defaultTrail = TrailGeometry(
        distanceKm = 9.5, ascentM = 980.0, descentM = 980.0, maxAltitudeM = 2200.0,
        avgGradePercent = 18.0, maxGradePercent = 35.0, terrain = TerrainType.TRAIL_CAI_EE,
    )

    @Test
    fun `default-web produce gli stessi valori del motore TypeScript`() {
        val r = PathEvaluator.evaluate(defaultProfile, defaultTrail)
        assertEquals(FeasibilityStatus.NEEDS_TRAINING, r.status)
        assertEquals(474, r.estimatedTimeMinutes)
        assertEquals(2199, r.caloriesKcal)
        assertEquals(4.0, r.averageMet, 0.05)
        assertEquals(1.2, r.averageSpeedKmh, 0.05)
        assertEquals(2, r.warnings.size)
        assertEquals("Tratto >35% sconsigliato a under 10", r.warnings[0].message)
        assertEquals(WarningSeverity.WARNING, r.warnings[0].severity)
        assertEquals("Dislivello >700 m: prevedere soste frequenti", r.warnings[1].message)
    }

    @Test
    fun `ferrata con bambini è non consigliato e i messaggi combaciano`() {
        val r = PathEvaluator.evaluate(
            defaultProfile.copy(hasVertigoPhobia = true),
            defaultTrail.copy(terrain = TerrainType.FERRATA, maxGradePercent = 70.0),
        )
        assertEquals(FeasibilityStatus.NOT_RECOMMENDED, r.status)
        assertEquals(619, r.estimatedTimeMinutes)
        assertEquals(3563, r.caloriesKcal)
        assertEquals(4.9, r.averageMet, 0.05)
        assertEquals(0.9, r.averageSpeedKmh, 0.05)
        val blocks = r.warnings.filter { it.severity == WarningSeverity.BLOCK }
        assertEquals(1, blocks.size) // un solo warning ferrata (dedup)
        assertEquals("Ferrata vietata ai minori di 14 anni (imbrago obbligatorio)", blocks[0].message)
        assertTrue(r.warnings.any { it.message.startsWith("Tratto >70%") })
        assertTrue(r.warnings.any { it.message.startsWith("Durata stimata ~10,3 h") }) // virgola, come il TS
    }

    @Test
    fun `adulti allenati su sentiero facile sono adatti a tutti`() {
        val r = PathEvaluator.evaluate(
            GroupProfile(ages = listOf(28, 32, 41), fitnessLevel = FitnessLevel.ALLENATO, backpackWeightKg = 5.0, averageBodyWeightKg = 72.0),
            TrailGeometry(6.0, 420.0, 420.0, 1800.0, 12.0, 20.0, TerrainType.SENTIERO),
        )
        assertEquals(FeasibilityStatus.SUITABLE_ALL, r.status)
        assertEquals(125, r.estimatedTimeMinutes)
        assertEquals(718, r.caloriesKcal)
        assertEquals(4.8, r.averageMet, 0.05)
        assertEquals(2.9, r.averageSpeedKmh, 0.05)
        assertTrue(r.warnings.isEmpty())
    }

    @Test
    fun `anziani a quota alta generano avvisi specifici`() {
        val r = PathEvaluator.evaluate(
            GroupProfile(ages = listOf(66, 70, 74), backpackWeightKg = 4.0),
            defaultTrail.copy(distanceKm = 14.0, ascentM = 1500.0, descentM = 1500.0, maxAltitudeM = 3200.0, maxGradePercent = 40.0),
        )
        assertEquals(FeasibilityStatus.NEEDS_TRAINING, r.status)
        assertEquals(646, r.estimatedTimeMinutes)
        assertEquals(3087, r.caloriesKcal)
        assertTrue(r.warnings.any { it.message.startsWith("Pendenze >40%") })
        assertTrue(r.warnings.any { it.message.startsWith("Quota oltre 2.800 m") })
    }

    @Test
    fun `cani su roccia producono l avviso delle scarpette`() {
        val r = PathEvaluator.evaluate(
            GroupProfile(ages = listOf(30, 35), hasDogs = true, fitnessLevel = FitnessLevel.PRINCIPIANTE, backpackWeightKg = 12.0),
            defaultTrail.copy(terrain = TerrainType.ROCCIA, maxGradePercent = 55.0),
        )
        assertEquals(FeasibilityStatus.NEEDS_TRAINING, r.status)
        assertEquals(481, r.estimatedTimeMinutes)
        assertEquals(3028, r.caloriesKcal)
        assertTrue(r.warnings.any { it.message == "Terreno roccioso: previste scarpette protettive per il cane" })
    }

    @Test
    fun `gruppo vuoto usa i default come il TypeScript`() {
        val r = PathEvaluator.evaluate(defaultProfile.copy(ages = emptyList()), defaultTrail)
        assertEquals(FeasibilityStatus.SUITABLE_ALL, r.status)
        assertEquals(306, r.estimatedTimeMinutes)
        assertEquals(1856, r.caloriesKcal)
        assertEquals(5.2, r.averageMet, 0.05)
        assertEquals(1.9, r.averageSpeedKmh, 0.05)
        assertTrue(r.warnings.isEmpty())
    }

    @Test
    fun `lunga durata segnala la luce insufficiente`() {
        val r = PathEvaluator.evaluate(
            GroupProfile(ages = listOf(12, 45)),
            defaultTrail.copy(distanceKm = 26.0, ascentM = 2100.0, descentM = 2100.0, maxAltitudeM = 2600.0, maxGradePercent = 32.0),
        )
        assertEquals(FeasibilityStatus.NEEDS_TRAINING, r.status)
        assertEquals(897, r.estimatedTimeMinutes)
        assertEquals(4622, r.caloriesKcal)
        assertTrue(r.warnings.any { it.message == "Durata stimata ~14,9 h: rischio di ore di luce insufficienti" })
    }
}
