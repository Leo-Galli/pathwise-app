/* ─────────────────────────────────────────────────────────────────────────────
 * PathWise — Verifica di PARITÀ tra il motore web (TypeScript) e il motore
 * Android (Kotlin, domain/engine/PathEvaluator.kt).
 *
 * Questo script esegue il motore TypeScript su una batteria di casi fissi e
 * stampa i valori "golden". Il test JVM PathEvaluatorTest.kt (lato Android)
 * asserisce che il motore Kotlin produca ESATTAMENTE gli stessi valori.
 *
 * Uso:
 *   node scripts/engine-parity.ts            # richiede Node 23.6+ (type stripping)
 *   npx tsx scripts/engine-parity.ts         # alternativa universale
 * ───────────────────────────────────────────────────────────────────────────── */
import { DEFAULT_PROFILE, DEFAULT_TRAIL, evaluatePath, type GroupProfile, type TrailGeometry } from "../web/lib/engine.ts";

const cases: { name: string; profile: GroupProfile; trail: TrailGeometry }[] = [
  {
    name: "default-web",
    profile: DEFAULT_PROFILE,
    trail: DEFAULT_TRAIL,
  },
  {
    name: "ferrata-bambini",
    profile: { ...DEFAULT_PROFILE, ages: [7, 39, 41, 67], hasVertigo: true },
    trail: { ...DEFAULT_TRAIL, terrain: "ferrata", maxGradePct: 70 },
  },
  {
    name: "adulti-allenati-sentiero",
    profile: { ...DEFAULT_PROFILE, ages: [28, 32, 41], fitness: "allenato", backpackKg: 5, bodyWeightKg: 72 },
    trail: { ...DEFAULT_TRAIL, distanceKm: 6.0, ascentM: 420, descentM: 420, maxAltitudeM: 1800, terrain: "sentiero", maxGradePct: 20 },
  },
  {
    name: "anziani-quota-alta",
    profile: { ...DEFAULT_PROFILE, ages: [66, 70, 74], backpackKg: 4 },
    trail: { ...DEFAULT_TRAIL, distanceKm: 14, ascentM: 1500, descentM: 1500, maxAltitudeM: 3200, terrain: "trail", maxGradePct: 40 },
  },
  {
    name: "cani-roccia",
    profile: { ...DEFAULT_PROFILE, ages: [30, 35], hasDogs: true, fitness: "principiante", backpackKg: 12 },
    trail: { ...DEFAULT_TRAIL, terrain: "roccia", maxGradePct: 55 },
  },
  {
    name: "gruppo-vuoto",
    profile: { ...DEFAULT_PROFILE, ages: [] },
    trail: { ...DEFAULT_TRAIL },
  },
  {
    name: "lunga-durata-luce",
    profile: { ...DEFAULT_PROFILE, ages: [12, 45] },
    trail: { ...DEFAULT_TRAIL, distanceKm: 26, ascentM: 2100, descentM: 2100, maxAltitudeM: 2600, terrain: "trail", maxGradePct: 32 },
  },
];

for (const c of cases) {
  const r = evaluatePath(c.profile, c.trail);
  console.log(
    JSON.stringify({
      name: c.name,
      status: r.status,
      minutes: r.estimatedTimeMinutes,
      calories: r.caloriesKcal,
      met: r.avgMet,
      speed: r.avgSpeedKmh,
      warnings: r.warnings.map((w) => ({ severity: w.severity, message: w.message })),
    })
  );
}
