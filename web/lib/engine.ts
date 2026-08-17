/* ─────────────────────────────────────────────────────────────────────────────
 * PathWise Engine — specchio TypeScript di `PathEvaluator.kt` (Android).
 *
 * Gli stessi identici numeri girano nativamente in Kotlin; questa versione
 * alimenta la demo interattiva della landing page.
 *
 *   • Tempo        → Regola di Naismith modificata da Langmuir
 *                    (4 km/h + 1h/600m D+ − 10min/300m D−, +25% di margine)
 *   • Velocità     → tariffata sul partecipante PIÙ VULNERABILE del gruppo
 *   • Calorie      → Equazione di Pandolf (1977) per marcia con carico,
 *                    fattore η per il terreno, discesa al 60% del costo.
 *   • Fattibilità  → regole di sicurezza per bambini, anziani, cani,
 *                    fobia delle altezze e ore di luce stimate.
 * ───────────────────────────────────────────────────────────────────────────── */

export type TerrainType = "sentiero" | "trail" | "roccia" | "ferrata";
export type FitnessLevel = "principiante" | "medio" | "allenato";
export type Status = "SUITABLE_ALL" | "NEEDS_TRAINING" | "NOT_RECOMMENDED";

export interface GroupProfile {
  ages: number[];
  fitness: FitnessLevel;
  bodyWeightKg: number;
  backpackKg: number;
  hasDogs: boolean;
  hasVertigo: boolean;
}

export interface TrailGeometry {
  distanceKm: number;
  ascentM: number;
  descentM: number;
  maxAltitudeM: number;
  terrain: TerrainType;
  maxGradePct: number; // pendenza massima rilevata (derivata o da GPX)
}

export interface EngineWarning {
  message: string;
  severity: "info" | "warning" | "block";
}

export interface PathResult {
  status: Status;
  statusLabel: string;
  statusColor: string; // classe Tailwind per il pill
  estimatedTimeMinutes: number;
  caloriesKcal: number;
  avgMet: number;
  avgSpeedKmh: number;
  warnings: EngineWarning[];
}

export const STATUS_LABEL: Record<Status, string> = {
  SUITABLE_ALL: "Adatto a tutti",
  NEEDS_TRAINING: "Richiede allenamento",
  NOT_RECOMMENDED: "Non consigliato",
};

export const STATUS_COLOR: Record<Status, string> = {
  SUITABLE_ALL: "bg-emerald-500/15 text-emerald-300 ring-emerald-400/40",
  NEEDS_TRAINING: "bg-amber-500/15 text-amber-300 ring-amber-400/40",
  NOT_RECOMMENDED: "bg-rose-500/15 text-rose-300 ring-rose-400/40",
};

/* Fattore velocità per fascia d'età (taratura "partecipante più vulnerabile") */
export function ageFactor(age: number): number {
  if (age < 8) return 1.55;
  if (age < 12) return 1.35;
  if (age < 16) return 1.15;
  if (age <= 60) return 1.0;
  if (age <= 70) return 1.2;
  if (age <= 80) return 1.45;
  return 1.7;
}

const FITNESS_FACTOR: Record<FitnessLevel, number> = {
  principiante: 1.3,
  medio: 1.0,
  allenato: 0.85,
};

/* Pendenza massima tipica per tipologia di terreno (valore di default GPX) */
export const TERRAIN_DEFAULT_GRADE: Record<TerrainType, number> = {
  sentiero: 28,
  trail: 35,
  roccia: 55,
  ferrata: 70,
};

const TERRAIN_SPEED: Record<TerrainType, number> = {
  sentiero: 1.0,
  trail: 1.15,
  roccia: 1.35,
  ferrata: 1.5,
};

/* Fattore η di Pandolf per il terreno (1.0 tapis roulant, cresce con la difficoltà) */
const TERRAIN_ETA: Record<TerrainType, number> = {
  sentiero: 1.1,
  trail: 1.5,
  roccia: 2.5,
  ferrata: 3.0,
};

/** Langmuir: durata "pulita" in ore prima di margini e fattori di gruppo. */
export function langmuirTimeHours(distanceKm: number, ascentM: number, descentM: number): number {
  const base = distanceKm / 4 + ascentM / 600;
  const credit = (descentM / 300) * (10 / 60); // −10 min ogni 300 m di discesa
  const cap = base * 0.3; // lo sconto non può superare il 30% del tempo base
  return base - Math.min(credit, cap);
}

/** Pandolf (1977): potenza metabolica in watt per marcia con carico. */
export function pandolfWatts(
  bodyKg: number,
  loadKg: number,
  v: number, // velocità in m/s
  gradePct: number, // pendenza %
  eta: number
): number {
  const w = bodyKg;
  const l = loadKg;
  return (
    1.5 * w +
    2.0 * (w + l) * Math.pow(l / w, 2) +
    eta * (w + l) * (1.5 * v * v + 0.35 * v * gradePct)
  );
}

export const DEFAULT_PROFILE: GroupProfile = {
  ages: [7, 39, 41, 67],
  fitness: "medio",
  bodyWeightKg: 70,
  backpackKg: 8,
  hasDogs: false,
  hasVertigo: false,
};

export const DEFAULT_TRAIL: TrailGeometry = {
  distanceKm: 9.5,
  ascentM: 980,
  descentM: 980,
  maxAltitudeM: 2200,
  terrain: "trail",
  maxGradePct: 35,
};

/** Valutazione completa del percorso per il gruppo dato. */
export function evaluatePath(profile: GroupProfile, geo: TrailGeometry): PathResult {
  const ages = profile.ages.filter((a) => a > 0 && a < 120);
  const safeAges = ages.length > 0 ? ages : [30];
  const minAge = Math.min(...safeAges);
  const maxAge = Math.max(...safeAges);

  /* ── Tempo stimato ─────────────────────────────────────────── */
  const worstAgeFactor = Math.max(...safeAges.map(ageFactor));
  const groupFactor =
    worstAgeFactor *
    FITNESS_FACTOR[profile.fitness] *
    (1 + Math.max(0, profile.backpackKg - 5) * 0.008) *
    TERRAIN_SPEED[geo.terrain];
  const rawHours = langmuirTimeHours(geo.distanceKm, geo.ascentM, geo.descentM);
  const totalHours = Math.max(rawHours * groupFactor * 1.25, 0.25); // +25% Langmuir
  const estimatedTimeMinutes = Math.round(totalHours * 60);
  const avgSpeedKmh = geo.distanceKm / totalHours;

  /* ── Calorie (Pandolf) ─────────────────────────────────────── */
  const v = avgSpeedKmh / 3.6;
  const w = profile.bodyWeightKg;
  const l = profile.backpackKg;
  const climbKm = geo.distanceKm * 0.5; // metà della distanza in salita
  const gradeUp = (geo.ascentM / (climbKm * 1000)) * 100;
  const mUp = pandolfWatts(w, l, v, gradeUp, TERRAIN_ETA[geo.terrain]);
  const mDown = pandolfWatts(w, l, v, 0, TERRAIN_ETA[geo.terrain]) * 0.6; // discesa ≈ 60%
  const tHalf = estimatedTimeMinutes * 0.5;
  const kcalMetabolic = (mUp * tHalf + mDown * tHalf) * 0.01433; // 1 W = 0.01433 kcal/min
  const rmrKcalPerMin = w / 60; // ≈ 1 kcal/kg/h a riposo
  const caloriesKcal = Math.round(kcalMetabolic + rmrKcalPerMin * estimatedTimeMinutes);
  const kcalPerMin = caloriesKcal / estimatedTimeMinutes;
  const avgMet = Number((kcalPerMin / rmrKcalPerMin).toFixed(1));

  /* ── Avvisi di sicurezza ───────────────────────────────────── */
  const warnings: EngineWarning[] = [];
  const child = minAge < 10;
  const elderly = maxAge > 65;
  const ferrata = geo.terrain === "ferrata";
  const rock = geo.terrain === "roccia";

  if (child && geo.maxGradePct > 30)
    warnings.push({
      severity: "warning",
      message: `Tratto >${Math.round(geo.maxGradePct)}%${
        geo.maxAltitudeM > 2200 ? ` a ${Math.round(geo.maxAltitudeM)} m` : ""
      } sconsigliato a under 10`,
    });
  if (ferrata && minAge < 14)
    warnings.push({ severity: "block", message: "Ferrata vietata ai minori di 14 anni (imbrago obbligatorio)" });
  if (child && geo.maxAltitudeM > 2200)
    warnings.push({
      severity: "warning",
      message: "Quota oltre 2.200 m: rischio di mal di montagna (AMS) nei bambini",
    });
  if (child && geo.distanceKm > 10)
    warnings.push({ severity: "warning", message: "Distanza >10 km impegnativa per i più piccoli" });
  if (child && geo.ascentM > 700)
    warnings.push({ severity: "warning", message: "Dislivello >700 m: prevedere soste frequenti" });
  if (elderly && geo.maxGradePct > 35)
    warnings.push({ severity: "warning", message: `Pendenze >${Math.round(geo.maxGradePct)}%: rischio cadute per i meno giovani` });
  if (elderly && geo.maxAltitudeM > 2800)
    warnings.push({ severity: "warning", message: "Quota oltre 2.800 m: attenzione a AMS e temperatura" });
  if (elderly && geo.distanceKm > 15)
    warnings.push({ severity: "warning", message: "Distanza >15 km: prevedere piano B e punti di fuga" });

  if (profile.hasVertigo && (ferrata || rock || geo.maxGradePct > 45))
    warnings.push({
      severity: "warning",
      message: "Tratti esposti rilevati: sconsigliato in caso di fobia delle altezze",
    });
  if (profile.hasDogs && ferrata)
    warnings.push({ severity: "block", message: "Ferrata vietata ai cani" });
  if (profile.hasDogs && rock)
    warnings.push({ severity: "warning", message: "Terreno roccioso: previste scarpette protettive per il cane" });

  if (estimatedTimeMinutes > 480) {
    const h = (estimatedTimeMinutes / 60).toFixed(1).replace(".", ",");
    warnings.push({ severity: "warning", message: `Durata stimata ~${h} h: rischio di ore di luce insufficienti` });
  }

  /* ── Verdetto ──────────────────────────────────────────────── */
  const hasBlock = warnings.some((x) => x.severity === "block");
  const status: Status = hasBlock ? "NOT_RECOMMENDED" : warnings.length > 0 ? "NEEDS_TRAINING" : "SUITABLE_ALL";

  return {
    status,
    statusLabel: STATUS_LABEL[status],
    statusColor: STATUS_COLOR[status],
    estimatedTimeMinutes,
    caloriesKcal,
    avgMet,
    avgSpeedKmh: Number(avgSpeedKmh.toFixed(1)),
    warnings,
  };
}

/**
 * Formatta numeri in stile italiano (es. 1.840 kcal).
 * Implementazione manuale: Intl/CLDR differiscono tra runtime (Node vs Chromium)
 * e causerebbero mismatch di hydration — qui il formato è deterministico.
 */
export function fmt(n: number, digits = 0): string {
  const [int, dec] = n.toFixed(digits).split(".");
  const grouped = int.replace(/\B(?=(\d{3})+(?!\d))/g, ".");
  return dec !== undefined ? `${grouped},${dec}` : grouped;
}

/** Conversione minuti → "6h 35m". */
export function fmtDuration(minutes: number): string {
  const h = Math.floor(minutes / 60);
  const m = Math.round(minutes % 60);
  if (h <= 0) return `${m} min`;
  return `${h}h ${String(m).padStart(2, "0")}m`;
}
