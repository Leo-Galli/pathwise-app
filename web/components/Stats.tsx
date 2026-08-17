const STATS = [
  { value: "380+", label: "km di tracce in libreria" },
  { value: "1.240", label: "rifugi e bivacchi geo-localizzati" },
  { value: "5", label: "lingue senza riavviare il telefono" },
  { value: "≤2s", label: "per inviare un SOS con le coordinate" },
];

export default function Stats() {
  return (
    <section className="relative border-y border-white/5 bg-night-900/50">
      <div className="mx-auto grid max-w-6xl grid-cols-2 divide-x divide-y divide-white/5 overflow-hidden md:grid-cols-4 md:divide-y-0">
        {STATS.map((s) => (
          <div key={s.label} className="group px-6 py-9 text-center transition-colors hover:bg-white/[0.02]">
            <p className="text-gradient text-3xl font-extrabold tracking-tight sm:text-4xl">{s.value}</p>
            <p className="mx-auto mt-2 max-w-[180px] text-xs font-medium leading-relaxed text-slate-500">
              {s.label}
            </p>
          </div>
        ))}
      </div>
    </section>
  );
}
