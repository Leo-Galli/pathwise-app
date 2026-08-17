import { ArrowRight, BrainCircuit, Download, Flame, MapPin, Timer } from "lucide-react";
import { DEFAULT_PROFILE, DEFAULT_TRAIL, evaluatePath, fmt, fmtDuration } from "@/lib/engine";

/* Valori calcolati dal vero engine: sempre coerenti con la demo interattiva */
const HERO_DEMO = evaluatePath(DEFAULT_PROFILE, DEFAULT_TRAIL);

/* Profilo altimetrico animato: la linea si "disegna" e un puntino la percorre */
function ElevationProfile() {
  return (
    <div className="relative">
      <div className="glass glow-emerald relative overflow-hidden rounded-3xl p-6">
        <div className="mb-4 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="relative flex size-2">
              <span className="absolute inline-flex size-full animate-ping rounded-full bg-emerald-400 opacity-60" />
              <span className="relative inline-flex size-2 rounded-full bg-emerald-400" />
            </span>
            <span className="text-xs font-semibold uppercase tracking-widest text-slate-400">
              Percorso simulato · Rifugio Alpe Alta
            </span>
          </div>
          <span className="rounded-full bg-white/5 px-2.5 py-1 text-[10px] font-medium text-slate-400">
            GPX · 9,5 km
          </span>
        </div>

        <svg viewBox="0 0 600 240" className="w-full" role="img" aria-label="Profilo altimetrico">
          <defs>
            <linearGradient id="routeStroke" x1="0" y1="0" x2="1" y2="0">
              <stop offset="0%" stopColor="#34d399" />
              <stop offset="55%" stopColor="#6ee7b7" />
              <stop offset="100%" stopColor="#38bdf8" />
            </linearGradient>
            <linearGradient id="routeFill" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#34d399" stopOpacity="0.28" />
              <stop offset="100%" stopColor="#34d399" stopOpacity="0" />
            </linearGradient>
          </defs>

          {/* griglia quota */}
          {[30, 80, 130, 180].map((y) => (
            <line key={y} x1="0" x2="600" y1={y} y2={y} stroke="rgba(148,163,184,0.08)" strokeDasharray="3 6" />
          ))}

          <path
            id="routePath"
            d="M0 205 L45 190 L95 200 L140 158 L185 168 L240 105 L285 132 L340 68 L395 100 L440 86 L500 138 L555 128 L600 150"
            fill="none"
            stroke="url(#routeStroke)"
            strokeWidth="3"
            strokeLinecap="round"
            strokeLinejoin="round"
            pathLength={1}
            className="route-draw"
          />
          <path
            d="M0 205 L45 190 L95 200 L140 158 L185 168 L240 105 L285 132 L340 68 L395 100 L440 86 L500 138 L555 128 L600 150 L600 240 L0 240 Z"
            fill="url(#routeFill)"
          />

          <circle r="7" fill="#34d399">
            <animateMotion dur="10s" repeatCount="indefinite">
              <mpath href="#routePath" />
            </animateMotion>
          </circle>
          <circle r="14" fill="#34d399" opacity="0.25">
            <animateMotion dur="10s" repeatCount="indefinite">
              <mpath href="#routePath" />
            </animateMotion>
          </circle>
        </svg>

        <div className="mt-3 flex justify-between text-[11px] font-medium text-slate-500">
          <span>Partenza · 1.480 m</span>
          <span>D+ 980 m</span>
          <span>Arrivo · 2.200 m</span>
        </div>
      </div>

      {/* Chip fluttuanti */}
      <div className="animate-float absolute -right-4 -top-5 hidden sm:block">
        <div className="glass flex items-center gap-2.5 rounded-2xl px-4 py-3 shadow-xl shadow-black/40">
          <span className="grid size-9 place-items-center rounded-xl bg-orange-500/15">
            <Flame className="size-5 text-orange-400" />
          </span>
          <div>
            <p className="text-[10px] font-medium uppercase tracking-wide text-slate-500">Calorie stimate</p>
            <p className="text-sm font-bold text-white">{fmt(HERO_DEMO.caloriesKcal)} kcal</p>
          </div>
        </div>
      </div>

      <div className="animate-float-slow absolute -left-6 top-16 hidden sm:block" style={{ animationDelay: "1.2s" }}>
        <div className="glass flex items-center gap-2.5 rounded-2xl px-4 py-3 shadow-xl shadow-black/40">
          <span className="grid size-9 place-items-center rounded-xl bg-sky-500/15">
            <Timer className="size-5 text-sky-400" />
          </span>
          <div>
            <p className="text-[10px] font-medium uppercase tracking-wide text-slate-500">Tempo stimato</p>
            <p className="text-sm font-bold text-white">{fmtDuration(HERO_DEMO.estimatedTimeMinutes)} · Langmuir</p>
          </div>
        </div>
      </div>

      <div className="animate-float absolute -right-2 bottom-20 hidden sm:block" style={{ animationDelay: "2.1s" }}>
        <div className="glass flex items-center gap-2.5 rounded-2xl px-4 py-3 shadow-xl shadow-black/40">
          <span className="grid size-9 place-items-center rounded-xl bg-emerald-500/15">
            <MapPin className="size-5 text-emerald-400" />
          </span>
          <div>
            <p className="text-[10px] font-medium uppercase tracking-wide text-slate-500">Dislivello +</p>
            <p className="text-sm font-bold text-white">{fmt(DEFAULT_TRAIL.ascentM)} m</p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default function Hero() {
  return (
    <section id="top" className="relative overflow-hidden pt-32 pb-20 sm:pt-40 sm:pb-28">
      <div aria-hidden className="bg-grid absolute inset-0 [mask-image:radial-gradient(70%_60%_at_50%_30%,black,transparent)]" />
      <div
        aria-hidden
        className="absolute -top-40 left-1/2 h-[480px] w-[720px] -translate-x-1/2 rounded-full bg-emerald-500/10 blur-[120px]"
      />

      <div className="relative mx-auto grid max-w-6xl items-center gap-16 px-5 lg:grid-cols-[1.05fr_1fr]">
        <div className="animate-rise">
          <div className="mb-6 inline-flex items-center gap-2 rounded-full border border-mint-400/25 bg-mint-500/10 px-4 py-1.5 text-xs font-semibold text-mint-300">
            <BrainCircuit className="size-4" />
            Engine v2 · Naismith · Langmuir · Pandolf
          </div>

          <h1 className="text-4xl font-extrabold leading-[1.08] tracking-tight text-white sm:text-6xl">
            Ogni sentiero ha un verdetto.
            <br />
            <span className="text-gradient">PathWise lo calcola.</span>
          </h1>

          <p className="mt-6 max-w-xl text-lg leading-relaxed text-slate-400">
            L'app Android nativa che valuta la <strong className="text-slate-200">fattibilità di un percorso per il tuo gruppo</strong> —
            età, allenamento, zaino, pendenza, quota — e ti porta in montagna con mappe offline, dati Health Connect e un
            SOS che funziona <em className="not-italic text-slate-200">senza rete</em>.
          </p>

          <div className="mt-9 flex flex-wrap items-center gap-4">
            <a
              href="#download"
              className="group inline-flex items-center gap-2.5 rounded-full bg-mint-500 px-7 py-3.5 text-sm font-bold text-night-950 shadow-xl shadow-emerald-500/30 transition-all hover:-translate-y-0.5 hover:bg-mint-400 hover:shadow-emerald-400/40"
            >
              <Download className="size-5 transition-transform group-hover:translate-y-0.5" />
              Scarica l'APK
            </a>
            <a
              href="#motore"
              className="group inline-flex items-center gap-2 rounded-full border border-white/15 bg-white/5 px-7 py-3.5 text-sm font-semibold text-white backdrop-blur transition-all hover:border-mint-400/40 hover:bg-white/10"
            >
              Prova l'Engine
              <ArrowRight className="size-4 transition-transform group-hover:translate-x-1" />
            </a>
          </div>

          <div className="mt-10 flex flex-wrap items-center gap-x-6 gap-y-2 text-xs text-slate-500">
            {["Mappe offline 3D", "SOS via SMS", "Health Connect", "5 lingue"].map((t) => (
              <span key={t} className="flex items-center gap-1.5">
                <span className="size-1.5 rounded-full bg-mint-400" />
                {t}
              </span>
            ))}
          </div>
        </div>

        <div className="animate-rise lg:pl-4" style={{ animationDelay: "0.15s" }}>
          <ElevationProfile />
        </div>
      </div>
    </section>
  );
}
