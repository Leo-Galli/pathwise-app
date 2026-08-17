import { Github, Heart, Mountain } from "lucide-react";

const SECTIONS = [
  {
    title: "Prodotto",
    links: ["Motore di valutazione", "Mappe offline", "Health Connect", "Modulo SOS", "Lingue"],
  },
  {
    title: "Risorse",
    links: ["Documentazione", "Privacy", "Termini", "Beta", "Contatti"],
  },
];

export default function Footer() {
  return (
    <footer className="relative border-t border-white/5 bg-night-900/40">
      <div className="mx-auto max-w-6xl px-5 py-14">
        <div className="grid gap-10 md:grid-cols-[1.4fr_1fr_1fr]">
          <div>
            <a href="#top" className="flex items-center gap-2.5">
              <span className="grid size-9 place-items-center rounded-xl bg-gradient-to-br from-mint-400 to-sky-glow">
                <Mountain className="size-5 text-night-950" strokeWidth={2.4} />
              </span>
              <span className="text-lg font-bold tracking-tight text-white">
                Path<span className="text-mint-400">Wise</span>
              </span>
            </a>
            <p className="mt-4 max-w-sm text-sm leading-relaxed text-slate-500">
              Trekking e sicurezza in montagna: fattibilità calcolata per il tuo gruppo, mappe offline, Health Connect
              e SOS via SMS. Made with care for the mountains.
            </p>
            <div className="mt-5 flex items-center gap-2 text-xs text-slate-600">
              <Heart className="size-3.5 text-rose-400" />
              Progetto open-source in sviluppo attivo
            </div>
          </div>

          {SECTIONS.map((s) => (
            <div key={s.title}>
              <p className="text-xs font-bold uppercase tracking-widest text-slate-400">{s.title}</p>
              <ul className="mt-4 flex flex-col gap-2.5">
                {s.links.map((l) => (
                  <li key={l}>
                    <a href="#top" className="text-sm text-slate-500 transition-colors hover:text-mint-300">
                      {l}
                    </a>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        <div className="mt-12 flex flex-col items-center justify-between gap-4 border-t border-white/5 pt-6 sm:flex-row">
          <p className="text-xs text-slate-600">
            © {new Date().getFullYear()} PathWise · it.leogalli.pathwise · Repo:{" "}
            <code className="rounded bg-white/5 px-1.5 py-0.5 font-mono text-[10px] text-slate-400">pathwise-app</code>
          </p>
          <a
            href="https://github.com/Leo-Galli/pathwise-app"
            target="_blank"
            rel="noreferrer"
            className="inline-flex items-center gap-2 rounded-full border border-white/10 px-4 py-2 text-xs font-semibold text-slate-300 transition-all hover:border-mint-400/40 hover:text-mint-300"
          >
            <Github className="size-4" />
            GitHub
          </a>
        </div>
      </div>
    </footer>
  );
}
