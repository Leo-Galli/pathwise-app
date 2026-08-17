import { Download as DownloadIcon, Mail, Package, QrCode, Smartphone } from "lucide-react";

export default function Download() {
  return (
    <section id="download" className="relative py-24 sm:py-32">
      <div className="mx-auto max-w-6xl px-5">
        <div className="glass relative overflow-hidden rounded-[2.5rem] p-10 sm:p-16">
          <div
            aria-hidden
            className="absolute -left-20 -top-24 h-72 w-72 rounded-full bg-emerald-500/15 blur-[100px]"
          />
          <div
            aria-hidden
            className="absolute -bottom-24 -right-16 h-72 w-72 rounded-full bg-sky-500/10 blur-[100px]"
          />
          <div
            aria-hidden
            className="bg-grid absolute inset-0 opacity-60 [mask-image:radial-gradient(65%_65%_at_50%_40%,black,transparent)]"
          />

          <div className="relative grid items-center gap-12 lg:grid-cols-[1.2fr_1fr]">
            <div>
              <p className="text-xs font-bold uppercase tracking-[0.25em] text-mint-400">Beta aperta</p>
              <h2 className="mt-3 text-3xl font-extrabold tracking-tight text-white sm:text-4xl">
                Porta il verdetto <span className="text-gradient">in montagna</span>
              </h2>
              <p className="mt-4 max-w-xl leading-relaxed text-slate-400">
                Scarica l'APK Android di PathWise e prova il motore di valutazione, le mappe offline e il modulo SOS.
                Package: <code className="rounded bg-white/5 px-1.5 py-0.5 font-mono text-xs text-mint-300">it.leogalli.pathwise</code>
              </p>

              <div className="mt-8 flex flex-wrap items-center gap-4">
                {/* Quando l'APK sarà buildato, punta qui: href="/pathwise.apk" */}
                <a
                  href="#download"
                  aria-disabled="true"
                  className="group inline-flex items-center gap-3 cursor-not-allowed rounded-2xl bg-mint-500 px-7 py-4 text-sm font-bold text-night-950 shadow-xl shadow-emerald-500/30 opacity-90"
                >
                  <DownloadIcon className="size-5 transition-transform group-hover:translate-y-0.5" />
                  Scarica APK · Android
                </a>
                <a
                  href="mailto:beta@pathwise.app?subject=Richiesta%20accesso%20beta"
                  className="group inline-flex items-center gap-2.5 rounded-2xl border border-white/15 bg-white/5 px-7 py-4 text-sm font-semibold text-white transition-all hover:border-mint-400/40 hover:bg-white/10"
                >
                  <Mail className="size-5" />
                  Unisciti alla beta
                </a>
              </div>

              <div className="mt-8 flex flex-wrap gap-x-5 gap-y-2 text-[11px] font-medium text-slate-500">
                {["Android 8+ (API 26)", "Health Connect", "Mappe offline", "Nessun account richiesto"].map((t) => (
                  <span key={t} className="flex items-center gap-1.5">
                    <span className="size-1.5 rounded-full bg-sky-400" />
                    {t}
                  </span>
                ))}
              </div>
            </div>

            {/* QR + chip */}
            <div className="relative mx-auto w-full max-w-[260px]">
              <div className="rounded-3xl border border-white/10 bg-night-900 p-6 text-center shadow-2xl shadow-black/50">
                <div className="mx-auto grid size-40 place-items-center rounded-2xl bg-white p-4">
                  {/* QR stilizzato */}
                  <div className="grid size-full grid-cols-[repeat(7,1fr)] gap-1">
                    {Array.from({ length: 49 }).map((_, i) => {
                      const on =
                        i === 0 || i === 1 || i === 2 || i === 7 || i === 14 ||
                        i === 6 || i === 13 || i === 20 || i === 21 ||
                        i === 28 || i === 29 || i === 30 || i === 36 || i === 35 ||
                        i === 42 || i === 43 || i === 44 || i === 22 || i === 23 ||
                        i === 24 || i === 25 || i === 26 || i === 27 || i === 34 ||
                        i === 41 || i === 48 || i === 47 || i === 40 || i === 33 ||
                        i === 32 || i === 31 || i === 18 || i === 19 || i === 12 ||
                        i === 11 || i === 10 || i === 9 || i === 8 || i === 15 ||
                        i === 16 || i === 17 || i === 37 || i === 38 || i === 39 ||
                        i === 45 || i === 46;
                      return (
                        <span key={i} className={`rounded-[2px] ${on ? "bg-night-950" : "bg-white"}`} />
                      );
                    })}
                  </div>
                </div>
                <div className="mt-4 flex items-center justify-center gap-2 text-xs font-semibold text-slate-300">
                  <QrCode className="size-4 text-mint-400" />
                  APK distribuito via beta
                </div>
              </div>

              <div className="animate-float-slow absolute -left-16 top-10 hidden sm:block">
                <div className="glass flex items-center gap-2 rounded-2xl px-3.5 py-2.5 shadow-xl shadow-black/40">
                  <Smartphone className="size-4 text-sky-400" />
                  <span className="text-xs font-bold text-white">~28 MB</span>
                </div>
              </div>
              <div className="animate-float absolute -right-10 bottom-8 hidden sm:block" style={{ animationDelay: "1.4s" }}>
                <div className="glass flex items-center gap-2 rounded-2xl px-3.5 py-2.5 shadow-xl shadow-black/40">
                  <Package className="size-4 text-mint-400" />
                  <span className="text-xs font-bold text-white">v1.0.0-beta</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
