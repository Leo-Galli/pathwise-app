"use client";

import { useState } from "react";
import { AlertTriangle, Camera, Droplets, House, Tent, Mountain } from "lucide-react";

/* ─────────────────────────────────────────────────────────────
   Demo dei layer della mappa: filtri attivabili che mostrano i
   POI geo-localizzati (stessa UX del LayerPanel di MapScreen.kt)
   ───────────────────────────────────────────────────────────── */

interface MarkerDef {
  id: string;
  layer: string;
  x: number; // % posizione sulla mappa
  y: number;
  icon: typeof House;
  color: string;
  label: string;
}

const MARKERS: MarkerDef[] = [
  { id: "r1", layer: "rifugi", x: 68, y: 38, icon: House, color: "text-emerald-300 bg-emerald-500/15 ring-emerald-400/40", label: "Rifugio Alpe Alta" },
  { id: "r2", layer: "rifugi", x: 42, y: 72, icon: House, color: "text-emerald-300 bg-emerald-500/15 ring-emerald-400/40", label: "Rifugio La Madonnina" },
  { id: "b1", layer: "bivacchi", x: 30, y: 60, icon: Tent, color: "text-lime-300 bg-lime-500/15 ring-lime-400/40", label: "Bivacco F.lli Calvi" },
  { id: "p1", layer: "panorami", x: 50, y: 22, icon: Camera, color: "text-violet-300 bg-violet-500/15 ring-violet-400/40", label: "Belvedere Cima Bianca" },
  { id: "s1", layer: "sorgenti", x: 76, y: 62, icon: Droplets, color: "text-sky-300 bg-sky-500/15 ring-sky-400/40", label: "Fontana del Bricco" },
  { id: "d1", layer: "pericoli", x: 22, y: 32, icon: AlertTriangle, color: "text-orange-300 bg-orange-500/15 ring-orange-400/40", label: "Tratto esposto" },
];

const LAYERS = [
  { id: "rifugi", label: "Rifugi", icon: House },
  { id: "bivacchi", label: "Bivacchi", icon: Tent },
  { id: "panorami", label: "Panorami", icon: Camera },
  { id: "sorgenti", label: "Sorgenti", icon: Droplets },
  { id: "pericoli", label: "Pericoli", icon: AlertTriangle },
];

export default function LayersDemo() {
  const [active, setActive] = useState<Set<string>>(new Set(["rifugi", "bivacchi", "panorami", "sorgenti"]));

  const toggle = (id: string) => {
    setActive((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const visible = MARKERS.filter((m) => active.has(m.layer));

  return (
    <section id="mappa" className="relative py-24 sm:py-32">
      <div className="mx-auto max-w-6xl px-5">
        <div className="mx-auto max-w-2xl text-center">
          <p className="text-xs font-bold uppercase tracking-[0.25em] text-sky-400">Mappa &amp; Layer</p>
          <h2 className="mt-3 text-3xl font-extrabold tracking-tight text-white sm:text-4xl">
            Il territorio <span className="text-gradient">stratificato per te</span>
          </h2>
          <p className="mt-4 text-slate-400">
            Rifugi, bivacchi, panorami, sorgenti e pericoli: ogni layer è un filtro indipendente, funzionante anche
            offline dopo il download della regione.
          </p>
        </div>

        <div className="mt-14 grid gap-6 lg:grid-cols-[1.2fr_1fr]">
          {/* Mappa stilizzata con curve di livello */}
          <div className="glass relative overflow-hidden rounded-3xl">
            <div className="relative aspect-[4/3] w-full bg-night-900">
              <svg viewBox="0 0 400 300" className="absolute inset-0 size-full" aria-hidden>
                {/* curve di livello concentriche */}
                {[
                  "M80 260 C 60 180, 120 140, 170 130 C 230 118, 300 120, 340 160 C 370 190, 380 230, 350 255",
                  "M95 265 C 80 195, 135 155, 180 145 C 235 133, 295 138, 330 172 C 358 200, 362 235, 335 258",
                  "M112 268 C 102 210, 150 172, 190 162 C 240 150, 288 158, 320 186 C 344 210, 345 238, 322 258",
                  "M132 270 C 128 226, 168 192, 200 182 C 244 170, 278 180, 306 202 C 326 219, 326 242, 306 260",
                  "M158 268 C 160 240, 188 214, 210 204 C 244 190, 266 202, 288 218 C 303 230, 302 248, 286 260",
                ].map((d, i) => (
                  <path key={i} d={d} fill="none" stroke="rgba(56,189,248,0.18)" strokeWidth="1" />
                ))}
                {/* sentiero */}
                <path
                  d="M25 275 C 90 250, 140 215, 180 200 C 220 185, 250 205, 285 175 C 320 145, 355 130, 385 120"
                  fill="none"
                  stroke="rgba(52,211,153,0.65)"
                  strokeWidth="2.5"
                  strokeDasharray="6 5"
                  strokeLinecap="round"
                />
                {/* cresta / tratto esposto */}
                <path
                  d="M285 175 C 300 150, 315 140, 330 135"
                  fill="none"
                  stroke="rgba(251,146,60,0.7)"
                  strokeWidth="3"
                  strokeLinecap="round"
                />
              </svg>

              {/* POI */}
              {visible.map((m) => (
                <div
                  key={m.id}
                  className="group absolute z-10 -translate-x-1/2 -translate-y-1/2 transition-all duration-300"
                  style={{ left: `${m.x}%`, top: `${m.y}%` }}
                >
                  <span className={`grid size-9 place-items-center rounded-full ring-1 transition-transform duration-300 group-hover:scale-125 ${m.color}`}>
                    <m.icon className="size-4" />
                  </span>
                  <span className="pointer-events-none absolute left-1/2 top-full mt-1.5 -translate-x-1/2 whitespace-nowrap rounded-md bg-night-950/95 px-2 py-1 text-[10px] font-medium text-slate-300 opacity-0 shadow-lg transition-opacity duration-200 group-hover:opacity-100">
                    {m.label}
                  </span>
                </div>
              ))}

              <div className="absolute bottom-3 left-3 rounded-full bg-night-950/70 px-3 py-1 text-[10px] font-medium text-slate-400 backdrop-blur">
                Regione scaricata · 12,4 MB
              </div>
              <div className="absolute bottom-3 right-3 rounded-full bg-mint-500/15 px-3 py-1 text-[10px] font-bold text-mint-300 ring-1 ring-mint-400/30">
                3D attivo
              </div>
            </div>
          </div>

          {/* Toggle dei layer */}
          <div className="flex flex-col gap-3">
            {LAYERS.map((l) => {
              const on = active.has(l.id);
              const count = MARKERS.filter((m) => m.layer === l.id).length;
              return (
                <button
                  key={l.id}
                  onClick={() => toggle(l.id)}
                  className={`group flex items-center justify-between rounded-2xl border px-5 py-4 text-left transition-all duration-300 ${
                    on
                      ? "border-mint-400/30 bg-mint-500/[0.07] shadow-lg shadow-emerald-500/5"
                      : "border-white/5 bg-white/[0.02] opacity-55 hover:opacity-80"
                  }`}
                >
                  <span className="flex items-center gap-3.5">
                    <span
                      className={`grid size-10 place-items-center rounded-xl ring-1 transition-all ${
                        on
                          ? "bg-mint-500/15 text-mint-300 ring-mint-400/40"
                          : "bg-night-800 text-slate-500 ring-white/10"
                      }`}
                    >
                      <l.icon className="size-5" />
                    </span>
                    <span>
                      <span className="block text-sm font-bold text-white">{l.label}</span>
                      <span className="block text-[11px] text-slate-500">
                        {on ? "Attivo sulla mappa" : "Nascosto"}
                      </span>
                    </span>
                  </span>
                  <span className="flex items-center gap-3">
                    {on && (
                      <span className="rounded-full bg-mint-500/15 px-2 py-0.5 text-xs font-bold tabular-nums text-mint-300">
                        {count}
                      </span>
                    )}
                    <span
                      className={`relative h-6 w-11 rounded-full transition-colors duration-300 ${
                        on ? "bg-mint-500" : "bg-night-600"
                      }`}
                    >
                      <span
                        className={`absolute top-0.5 size-5 rounded-full bg-white shadow transition-all duration-300 ${
                          on ? "left-[22px]" : "left-0.5"
                        }`}
                      />
                    </span>
                  </span>
                </button>
              );
            })}

            <div className="glass mt-1 flex items-start gap-3 rounded-2xl p-4">
              <Mountain className="mt-0.5 size-5 shrink-0 text-sky-400" />
              <p className="text-xs leading-relaxed text-slate-400">
                <span className="font-semibold text-slate-200">Rilievo 3D</span> — con la modalità terreno la mappa
                mostra le pendenze reali; i tratti esposti (in arancione) vengono segnalati automaticamente dal motore.
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
