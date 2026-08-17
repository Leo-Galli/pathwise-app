"use client";

import { useMemo, useState } from "react";
import {
  AlertTriangle,
  Baby,
  BrainCircuit,
  Dog,
  Flame,
  Gauge,
  Mountain,
  Plus,
  ShieldAlert,
  Timer,
  X,
} from "lucide-react";
import {
  DEFAULT_PROFILE,
  DEFAULT_TRAIL,
  TERRAIN_DEFAULT_GRADE,
  evaluatePath,
  fmt,
  fmtDuration,
  type FitnessLevel,
  type TerrainType,
} from "@/lib/engine";

/* ─────────────────────────────────────────────────────────────
   Demo interattiva: lo stesso identico PathWise Engine Kotlin
   compilato in TypeScript, eseguito live nel browser.
   ───────────────────────────────────────────────────────────── */

const TERRAIN_OPTIONS: { id: TerrainType; label: string }[] = [
  { id: "sentiero", label: "Sentiero" },
  { id: "trail", label: "CAI / EE" },
  { id: "roccia", label: "Roccia" },
  { id: "ferrata", label: "Ferrata" },
];

const FITNESS_OPTIONS: { id: FitnessLevel; label: string }[] = [
  { id: "principiante", label: "Principiante" },
  { id: "medio", label: "Medio" },
  { id: "allenato", label: "Allenato" },
];

function Slider({
  label,
  value,
  min,
  max,
  step,
  unit,
  onChange,
}: {
  label: string;
  value: number;
  min: number;
  max: number;
  step: number;
  unit: string;
  onChange: (v: number) => void;
}) {
  const fill = ((value - min) / (max - min)) * 100;
  return (
    <label className="block">
      <div className="mb-2 flex items-baseline justify-between">
        <span className="text-xs font-semibold uppercase tracking-wide text-slate-500">{label}</span>
        <span className="text-sm font-bold tabular-nums text-mint-300">
          {fmt(value, step < 1 ? 1 : 0)} {unit}
        </span>
      </div>
      <input
        type="range"
        min={min}
        max={max}
        step={step}
        value={value}
        style={{ ["--fill" as string]: `${fill}%` }}
        onChange={(e) => onChange(Number(e.target.value))}
        className="w-full"
      />
    </label>
  );
}

export default function EngineDemo() {
  const [profile, setProfile] = useState({ ...DEFAULT_PROFILE });
  const [trail, setTrail] = useState({ ...DEFAULT_TRAIL });

  const result = useMemo(() => evaluatePath(profile, trail), [profile, trail]);

  const addAge = () => {
    setProfile((p) => ({ ...p, ages: [...p.ages, 30] }));
  };
  const removeAge = (idx: number) => {
    setProfile((p) => ({ ...p, ages: p.ages.filter((_, i) => i !== idx) }));
  };
  const setAge = (idx: number, v: string) => {
    const n = Number(v);
    setProfile((p) => ({
      ...p,
      ages: p.ages.map((a, i) => (i === idx ? (isNaN(n) ? a : Math.max(1, Math.min(110, n))) : a)),
    }));
  };
  const setTerrain = (t: TerrainType) => {
    setTrail((tr) => ({ ...tr, terrain: t, maxGradePct: TERRAIN_DEFAULT_GRADE[t] }));
  };

  const warningDot =
    result.status === "SUITABLE_ALL" ? "bg-emerald-400" : result.status === "NEEDS_TRAINING" ? "bg-amber-400" : "bg-rose-400";

  return (
    <section id="motore" className="relative py-24 sm:py-32">
      <div
        aria-hidden
        className="absolute left-0 top-1/3 h-[420px] w-[420px] rounded-full bg-emerald-500/8 blur-[120px]"
      />
      <div className="relative mx-auto max-w-6xl px-5">
        <div className="mx-auto max-w-2xl text-center">
          <p className="text-xs font-bold uppercase tracking-[0.25em] text-mint-400">PathWise Engine</p>
          <h2 className="mt-3 text-3xl font-extrabold tracking-tight text-white sm:text-4xl">
            Provalo adesso: <span className="text-gradient">lo stesso motore dell'app</span>
          </h2>
          <p className="mt-4 text-slate-400">
            Inserisci il tuo gruppo e il percorso. Il calcolo qui sotto usa esattamente le stesse formule
            (Langmuir + Pandolf + regole di sicurezza) che girano nativamente in Kotlin su Android.
          </p>
        </div>

        <div className="mt-14 grid gap-6 lg:grid-cols-[1.15fr_1fr]">
          {/* ── Pannello input ─────────────────────────────────── */}
          <div className="glass rounded-3xl p-7">
            <div className="flex items-center justify-between">
              <h3 className="flex items-center gap-2 text-sm font-bold text-white">
                <span className="grid size-8 place-items-center rounded-lg bg-mint-500/15">
                  <Baby className="size-4 text-mint-300" />
                </span>
                Gruppo
              </h3>
              <button
                onClick={addAge}
                className="inline-flex items-center gap-1 rounded-full border border-mint-400/30 bg-mint-500/10 px-3 py-1.5 text-xs font-semibold text-mint-300 transition-colors hover:bg-mint-500/20"
              >
                <Plus className="size-3.5" /> Aggiungi
              </button>
            </div>

            <div className="mt-4 flex flex-wrap gap-2">
              {profile.ages.map((age, i) => (
                <div
                  key={i}
                  className="group flex items-center gap-1.5 rounded-xl border border-white/10 bg-night-800 px-2 py-1.5 transition-colors hover:border-mint-400/30"
                >
                  <input
                    type="number"
                    min={1}
                    max={110}
                    value={age}
                    onChange={(e) => setAge(i, e.target.value)}
                    aria-label={`Età membro ${i + 1}`}
                    className="w-11 bg-transparent text-center text-sm font-bold text-white outline-none [appearance:textfield] [&::-webkit-inner-spin-button]:appearance-none"
                  />
                  <span className="text-[10px] font-medium text-slate-500">anni</span>
                  {profile.ages.length > 1 && (
                    <button
                      onClick={() => removeAge(i)}
                      className="grid size-5 place-items-center rounded-md text-slate-500 transition-colors hover:bg-rose-500/20 hover:text-rose-300"
                      aria-label="Rimuovi membro"
                    >
                      <X className="size-3" />
                    </button>
                  )}
                </div>
              ))}
            </div>

            <div className="mt-6 grid gap-6 sm:grid-cols-2">
              <div>
                <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-slate-500">Preparazione atletica</p>
                <div className="flex flex-wrap gap-2">
                  {FITNESS_OPTIONS.map((f) => (
                    <button
                      key={f.id}
                      onClick={() => setProfile((p) => ({ ...p, fitness: f.id }))}
                      className={`rounded-full px-3.5 py-1.5 text-xs font-semibold transition-all ${
                        profile.fitness === f.id
                          ? "bg-mint-500 text-night-950 shadow-md shadow-emerald-500/30"
                          : "border border-white/10 text-slate-400 hover:border-mint-400/40 hover:text-white"
                      }`}
                    >
                      {f.label}
                    </button>
                  ))}
                </div>

                <div className="mt-5 flex flex-wrap gap-2">
                  <button
                    onClick={() => setProfile((p) => ({ ...p, hasDogs: !p.hasDogs }))}
                    className={`inline-flex items-center gap-1.5 rounded-full px-3.5 py-1.5 text-xs font-semibold transition-all ${
                      profile.hasDogs
                        ? "bg-amber-500/20 text-amber-300 ring-1 ring-amber-400/40"
                        : "border border-white/10 text-slate-400 hover:border-amber-400/40 hover:text-white"
                    }`}
                  >
                    <Dog className="size-3.5" /> Cane
                  </button>
                  <button
                    onClick={() => setProfile((p) => ({ ...p, hasVertigo: !p.hasVertigo }))}
                    className={`inline-flex items-center gap-1.5 rounded-full px-3.5 py-1.5 text-xs font-semibold transition-all ${
                      profile.hasVertigo
                        ? "bg-sky-500/20 text-sky-300 ring-1 ring-sky-400/40"
                        : "border border-white/10 text-slate-400 hover:border-sky-400/40 hover:text-white"
                    }`}
                  >
                    <ShieldAlert className="size-3.5" /> Fobia altezze
                  </button>
                </div>
              </div>

              <div className="flex flex-col gap-5">
                <Slider
                  label="Peso medio"
                  value={profile.bodyWeightKg}
                  min={40}
                  max={110}
                  step={1}
                  unit="kg"
                  onChange={(v) => setProfile((p) => ({ ...p, bodyWeightKg: v }))}
                />
                <Slider
                  label="Zaino medio"
                  value={profile.backpackKg}
                  min={0}
                  max={25}
                  step={0.5}
                  unit="kg"
                  onChange={(v) => setProfile((p) => ({ ...p, backpackKg: v }))}
                />
              </div>
            </div>

            <div className="my-6 h-px bg-gradient-to-r from-transparent via-white/10 to-transparent" />

            <h3 className="flex items-center gap-2 text-sm font-bold text-white">
              <span className="grid size-8 place-items-center rounded-lg bg-sky-500/15">
                <Mountain className="size-4 text-sky-300" />
              </span>
              Percorso
            </h3>

            <div className="mt-4 grid gap-5 sm:grid-cols-2">
              <div>
                <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-slate-500">Terreno</p>
                <div className="flex flex-wrap gap-2">
                  {TERRAIN_OPTIONS.map((t) => (
                    <button
                      key={t.id}
                      onClick={() => setTerrain(t.id)}
                      className={`rounded-full px-3.5 py-1.5 text-xs font-semibold transition-all ${
                        trail.terrain === t.id
                          ? "bg-sky-500 text-night-950 shadow-md shadow-sky-500/30"
                          : "border border-white/10 text-slate-400 hover:border-sky-400/40 hover:text-white"
                      }`}
                    >
                      {t.label}
                    </button>
                  ))}
                </div>
                <div className="mt-4 rounded-xl border border-white/5 bg-night-800/60 px-3.5 py-2.5 text-xs text-slate-400">
                  Pendenza massima rilevata:{" "}
                  <span className="font-bold tabular-nums text-sky-300">&gt;{fmt(trail.maxGradePct)}%</span>
                </div>
              </div>

              <div className="flex flex-col gap-5">
                <Slider
                  label="Distanza"
                  value={trail.distanceKm}
                  min={1}
                  max={30}
                  step={0.5}
                  unit="km"
                  onChange={(v) => setTrail((t) => ({ ...t, distanceKm: v }))}
                />
                <Slider
                  label="Dislivello positivo"
                  value={trail.ascentM}
                  min={0}
                  max={2500}
                  step={10}
                  unit="m"
                  onChange={(v) => setTrail((t) => ({ ...t, ascentM: v, descentM: v }))}
                />
                <Slider
                  label="Quota massima"
                  value={trail.maxAltitudeM}
                  min={300}
                  max={4200}
                  step={50}
                  unit="m"
                  onChange={(v) => setTrail((t) => ({ ...t, maxAltitudeM: v }))}
                />
              </div>
            </div>
          </div>

          {/* ── Pannello risultato ─────────────────────────────── */}
          <div className="flex flex-col gap-4">
            <div
              className={`relative overflow-hidden rounded-3xl border p-7 transition-colors duration-500 ${
                result.status === "SUITABLE_ALL"
                  ? "border-emerald-400/25 bg-emerald-500/[0.06]"
                  : result.status === "NEEDS_TRAINING"
                  ? "border-amber-400/25 bg-amber-500/[0.06]"
                  : "border-rose-400/25 bg-rose-500/[0.06]"
              }`}
            >
              <div className="flex items-start justify-between">
                <div>
                  <p className="text-xs font-semibold uppercase tracking-widest text-slate-500">Verdetto</p>
                  <div className="mt-2 inline-flex items-center gap-2 rounded-full px-4 py-2 text-sm font-bold ring-1 ${result.statusColor}">
                    <span className={`size-2 rounded-full ${warningDot} animate-pulseglow`} />
                    {result.statusLabel}
                  </div>
                </div>
                <span className="grid size-11 place-items-center rounded-2xl bg-white/5 ring-1 ring-white/10">
                  <BrainCircuit className="size-6 text-mint-300" />
                </span>
              </div>

              <div className="mt-6 grid grid-cols-3 gap-3">
                <div className="rounded-2xl border border-white/5 bg-night-900/60 p-3.5">
                  <Timer className="size-4 text-sky-400" />
                  <p className="mt-2 text-lg font-extrabold tabular-nums text-white">
                    {fmtDuration(result.estimatedTimeMinutes)}
                  </p>
                  <p className="text-[10px] font-medium uppercase tracking-wide text-slate-500">Tempo stimato</p>
                </div>
                <div className="rounded-2xl border border-white/5 bg-night-900/60 p-3.5">
                  <Flame className="size-4 text-orange-400" />
                  <p className="mt-2 text-lg font-extrabold tabular-nums text-white">
                    {fmt(result.caloriesKcal)}
                    <span className="text-xs font-semibold text-slate-500"> kcal</span>
                  </p>
                  <p className="text-[10px] font-medium uppercase tracking-wide text-slate-500">Pandolf</p>
                </div>
                <div className="rounded-2xl border border-white/5 bg-night-900/60 p-3.5">
                  <Gauge className="size-4 text-emerald-400" />
                  <p className="mt-2 text-lg font-extrabold tabular-nums text-white">
                    {result.avgMet}
                    <span className="text-xs font-semibold text-slate-500"> MET</span>
                  </p>
                  <p className="text-[10px] font-medium uppercase tracking-wide text-slate-500">Media · {fmt(result.avgSpeedKmh, 1)} km/h</p>
                </div>
              </div>

              <div className="mt-5 flex flex-wrap items-center gap-x-4 gap-y-1 text-[11px] text-slate-500">
                <span className="flex items-center gap-1.5">
                  <Mountain className="size-3.5" /> D+ {fmt(trail.ascentM)} m
                </span>
                <span className="flex items-center gap-1.5">
                  <AlertTriangle className="size-3.5" /> pend. max &gt;{fmt(trail.maxGradePct)}%
                </span>
                <span>tarato sul membro più vulnerabile</span>
              </div>
            </div>

            {/* Avvisi */}
            <div className="glass rounded-3xl p-6">
              <p className="text-xs font-semibold uppercase tracking-widest text-slate-500">
                Avvisi di sicurezza ({result.warnings.length})
              </p>
              <ul className="mt-3 flex flex-col gap-2.5">
                {result.warnings.length === 0 && (
                  <li className="rounded-xl border border-emerald-400/20 bg-emerald-500/10 px-4 py-3 text-sm text-emerald-300">
                    ✓ Nessun avviso critico: percorso compatibile con tutto il gruppo.
                  </li>
                )}
                {result.warnings.map((w, i) => (
                  <li
                    key={i}
                    className={`flex items-start gap-2.5 rounded-xl border px-4 py-3 text-sm ${
                      w.severity === "block"
                        ? "border-rose-400/25 bg-rose-500/10 text-rose-300"
                        : w.severity === "warning"
                        ? "border-amber-400/25 bg-amber-500/10 text-amber-200"
                        : "border-sky-400/20 bg-sky-500/10 text-sky-200"
                    }`}
                  >
                    <AlertTriangle className="mt-0.5 size-4 shrink-0" />
                    {w.message}
                  </li>
                ))}
              </ul>
              <p className="mt-4 text-[11px] leading-relaxed text-slate-600">
                Stima a scopo informativo: valida sempre le condizioni reali (meteo, neve, orario di luce) prima di
                partire. L'app Android aggiunge GPS, quota e cartografia in tempo reale.
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
