import { CheckCircle2, MapPin, MessageSquareText, Phone, Smartphone } from "lucide-react";

const SOS_POINTS = [
  {
    title: "SMS composto in un tocco",
    desc: "Coordinate GPS (latitudine, longitudine, altitudine) e link Google Maps pronti nell'app di messaggistica.",
  },
  {
    title: "Funziona senza rete dati",
    desc: "Il canale SMS copre dove la copertura dati non arriva: il soccorso riceve la tua posizione esatta.",
  },
  {
    title: "Destinatari configurabili",
    desc: "Numeri di emergenza preimpostati (112 / 118) e contatti personali modificabili dalle impostazioni.",
  },
];

export default function SosSection() {
  return (
    <section id="sos" className="relative py-24 sm:py-32">
      <div
        aria-hidden
        className="absolute right-0 top-1/4 h-[380px] w-[380px] rounded-full bg-rose-500/8 blur-[120px]"
      />
      <div className="relative mx-auto grid max-w-6xl items-center gap-14 px-5 lg:grid-cols-2">
        {/* Mockup telefono */}
        <div className="relative mx-auto w-full max-w-[300px]">
          <div
            aria-hidden
            className="absolute -inset-8 rounded-[3.5rem] bg-gradient-to-b from-rose-500/20 to-transparent blur-2xl"
          />
          <div className="relative overflow-hidden rounded-[2.8rem] border border-white/10 bg-night-900 p-3 shadow-2xl shadow-black/60">
            <div className="rounded-[2.2rem] bg-night-800 ring-1 ring-white/5">
              {/* notch */}
              <div className="mx-auto mt-2 h-6 w-28 rounded-full bg-night-950" />
              {/* header chat */}
              <div className="flex items-center gap-3 border-b border-white/5 px-4 pb-3 pt-3">
                <span className="grid size-9 place-items-center rounded-full bg-rose-500/15 ring-1 ring-rose-400/30">
                  <Smartphone className="size-4 text-rose-300" />
                </span>
                <div>
                  <p className="text-xs font-bold text-white">Emergenza · 112</p>
                  <p className="text-[10px] text-emerald-400">● Inviato via SMS</p>
                </div>
              </div>
              {/* messaggi */}
              <div className="flex min-h-[300px] flex-col gap-2.5 p-4">
                <div className="max-w-[85%] self-start rounded-2xl rounded-bl-md bg-night-700/80 px-3.5 py-2.5 text-[11px] leading-relaxed text-slate-300">
                  SOS! Siamo sul sentiero, un escursionista si è infortunato alla caviglia.
                </div>
                <div className="max-w-[92%] self-end rounded-2xl rounded-br-md bg-emerald-600/90 px-3.5 py-2.5 text-[11px] leading-relaxed text-emerald-50 shadow-lg shadow-emerald-900/40">
                  <p className="mb-1 flex items-center gap-1 font-bold">
                    <MapPin className="size-3" /> SOS PathWise · 45,8341 N 6,8653 E
                  </p>
                  <p className="font-mono">Alt. 2.312 m · 45°50'02.8"N 6°51'55.1"E</p>
                  <p className="mt-1 break-all font-mono underline">
                    https://maps.google.com/?q=45.8341,6.8653
                  </p>
                  <p className="mt-1.5">1 adulto · 1 minore · nessun altro ferito.</p>
                </div>
                <div className="mt-auto self-start rounded-2xl rounded-bl-md bg-night-700/60 px-3 py-1.5 text-[10px] text-slate-400">
                  Inviato · 14:32
                </div>
              </div>
            </div>
          </div>

          <div className="animate-float absolute -right-10 -top-6 hidden sm:block">
            <div className="glass flex items-center gap-2 rounded-2xl px-3.5 py-2.5 shadow-xl shadow-black/40">
              <MessageSquareText className="size-4 text-emerald-400" />
              <span className="text-xs font-bold text-white">0 dati · solo SMS</span>
            </div>
          </div>
        </div>

        {/* Testo */}
        <div>
          <p className="text-xs font-bold uppercase tracking-[0.25em] text-rose-400">Modulo SOS</p>
          <h2 className="mt-3 text-3xl font-extrabold tracking-tight text-white sm:text-4xl">
            Quando il telefono non prende,
            <br />
            <span className="text-gradient">l'SMS prende sempre</span>
          </h2>
          <p className="mt-5 max-w-lg leading-relaxed text-slate-400">
            Il modulo SOS di PathWise compila in automatico un messaggio di emergenza con la tua posizione esatta:
            latitudine, longitudine, altitudine e un link cliccabile per Google Maps. Basta un tocco per inviarlo al
            112 o ai tuoi contatti.
          </p>

          <ul className="mt-8 flex flex-col gap-5">
            {SOS_POINTS.map((p) => (
              <li key={p.title} className="flex items-start gap-4">
                <CheckCircle2 className="mt-0.5 size-5 shrink-0 text-emerald-400" />
                <div>
                  <p className="text-sm font-bold text-white">{p.title}</p>
                  <p className="mt-1 text-sm leading-relaxed text-slate-400">{p.desc}</p>
                </div>
              </li>
            ))}
          </ul>

          <div className="mt-8 inline-flex items-center gap-2.5 rounded-2xl border border-rose-400/20 bg-rose-500/10 px-5 py-3.5 text-sm text-rose-200">
            <Phone className="size-5 shrink-0" />
            In caso di pericolo immediato chiama sempre prima il <strong className="ml-1">112</strong>.
          </div>
        </div>
      </div>
    </section>
  );
}
