import {
  Brain,
  HeartPulse,
  Languages,
  Layers,
  Map,
  Siren,
} from "lucide-react";

const FEATURES = [
  {
    icon: Brain,
    accent: "text-mint-300 bg-mint-500/10 ring-mint-400/25",
    title: "PathWise Engine",
    desc: "Tempi con la regola di Langmuir, calorie con l'equazione di Pandolf e un verdetto di fattibilità tarato sul partecipante più vulnerabile del gruppo.",
  },
  {
    icon: Map,
    accent: "text-sky-300 bg-sky-500/10 ring-sky-400/25",
    title: "Mappe offline 3D",
    desc: "Rilievi 3D con Google Maps SDK, regioni scaricabili per la fruizione fuori copertura e GPS a campionamento dinamico per risparmiare batteria.",
  },
  {
    icon: Layers,
    accent: "text-violet-300 bg-violet-500/10 ring-violet-400/25",
    title: "Layer dei POI",
    desc: "Rifugi e bivacchi (posti letto, contatti), panorami, sorgenti e fontanelle, pericoli e tratti esposti: ogni filtro si attiva in un tocco.",
  },
  {
    icon: HeartPulse,
    accent: "text-rose-300 bg-rose-500/10 ring-rose-400/25",
    title: "Health Connect",
    desc: "Sincronizzazione bidirezionale: legge peso e frequenza cardiaca, scrive calorie bruciate, passi, distanza e la sessione di trekking completata.",
  },
  {
    icon: Siren,
    accent: "text-orange-300 bg-orange-500/10 ring-orange-400/25",
    title: "SOS via SMS",
    desc: "Coordinate GPS e link Google Maps composti in un SMS pronto all'invio, pensato per i soccorsi in assenza di dati mobili.",
  },
  {
    icon: Languages,
    accent: "text-cyan-300 bg-cyan-500/10 ring-cyan-400/25",
    title: "5 lingue dinamiche",
    desc: "Italiano, inglese, tedesco, francese e spagnolo selezionabili nelle impostazioni, applicati istantaneamente senza riavviare il dispositivo.",
  },
];

export default function Features() {
  return (
    <section id="funzionalita" className="relative py-24 sm:py-32">
      <div className="mx-auto max-w-6xl px-5">
        <div className="mx-auto max-w-2xl text-center">
          <p className="text-xs font-bold uppercase tracking-[0.25em] text-mint-400">Funzionalità</p>
          <h2 className="mt-3 text-3xl font-extrabold tracking-tight text-white sm:text-4xl">
            Tutto quello che serve, <span className="text-gradient">anche senza segnale</span>
          </h2>
          <p className="mt-4 text-slate-400">
            PathWise è costruito per la montagna reale: dove il telefono non prende, la decisione
            importante è già stata presa prima di partire.
          </p>
        </div>

        <div className="mt-16 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {FEATURES.map((f) => (
            <article
              key={f.title}
              className="group glass relative overflow-hidden rounded-3xl p-7 transition-all duration-300 hover:-translate-y-1.5 hover:border-mint-400/30 hover:shadow-xl hover:shadow-emerald-500/10"
            >
              <div
                aria-hidden
                className="absolute -right-10 -top-10 size-32 rounded-full bg-mint-400/0 blur-2xl transition-all duration-500 group-hover:bg-mint-400/10"
              />
              <span
                className={`inline-grid size-12 place-items-center rounded-2xl ring-1 ${f.accent} transition-transform duration-300 group-hover:scale-110 group-hover:rotate-3`}
              >
                <f.icon className="size-6" />
              </span>
              <h3 className="mt-5 text-lg font-bold text-white">{f.title}</h3>
              <p className="mt-2.5 text-sm leading-relaxed text-slate-400">{f.desc}</p>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
