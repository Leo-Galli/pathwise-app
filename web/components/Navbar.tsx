"use client";

import { useEffect, useState } from "react";
import { Menu, Mountain, X, Download } from "lucide-react";

const LINKS = [
  { href: "#motore", label: "Motore" },
  { href: "#funzionalita", label: "Funzionalità" },
  { href: "#mappa", label: "Mappa" },
  { href: "#sos", label: "SOS" },
];

export default function Navbar() {
  const [scrolled, setScrolled] = useState(false);
  const [open, setOpen] = useState(false);

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 24);
    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  return (
    <header
      className={`fixed inset-x-0 top-0 z-50 transition-all duration-300 ${
        scrolled ? "border-b border-white/5 bg-night-950/80 backdrop-blur-xl" : "bg-transparent"
      }`}
    >
      <nav className="mx-auto flex h-16 max-w-6xl items-center justify-between px-5">
        <a href="#top" className="group flex items-center gap-2.5">
          <span className="grid size-9 place-items-center rounded-xl bg-gradient-to-br from-mint-400 to-sky-glow shadow-lg shadow-emerald-500/25 transition-transform duration-300 group-hover:rotate-6 group-hover:scale-105">
            <Mountain className="size-5 text-night-950" strokeWidth={2.4} />
          </span>
          <span className="text-lg font-bold tracking-tight text-white">
            Path<span className="text-mint-400">Wise</span>
          </span>
        </a>

        <div className="hidden items-center gap-8 md:flex">
          {LINKS.map((l) => (
            <a
              key={l.href}
              href={l.href}
              className="text-sm font-medium text-slate-400 transition-colors hover:text-mint-300"
            >
              {l.label}
            </a>
          ))}
          <a
            href="#download"
            className="inline-flex items-center gap-2 rounded-full bg-mint-500 px-4 py-2 text-sm font-semibold text-night-950 shadow-lg shadow-emerald-500/30 transition-all hover:bg-mint-400 hover:shadow-emerald-400/40 hover:-translate-y-0.5"
          >
            <Download className="size-4" />
            Scarica APK
          </a>
        </div>

        <button
          onClick={() => setOpen(!open)}
          className="grid size-10 place-items-center rounded-xl border border-white/10 text-slate-200 md:hidden"
          aria-label="Apri menu"
        >
          {open ? <X className="size-5" /> : <Menu className="size-5" />}
        </button>
      </nav>

      {open && (
        <div className="border-t border-white/5 bg-night-950/95 px-5 py-4 backdrop-blur-xl md:hidden">
          <div className="flex flex-col gap-3">
            {LINKS.map((l) => (
              <a
                key={l.href}
                href={l.href}
                onClick={() => setOpen(false)}
                className="rounded-lg px-3 py-2 text-sm font-medium text-slate-300 transition-colors hover:bg-white/5 hover:text-mint-300"
              >
                {l.label}
              </a>
            ))}
            <a
              href="#download"
              onClick={() => setOpen(false)}
              className="mt-1 inline-flex items-center justify-center gap-2 rounded-full bg-mint-500 px-4 py-2.5 text-sm font-semibold text-night-950"
            >
              <Download className="size-4" /> Scarica APK
            </a>
          </div>
        </div>
      )}
    </header>
  );
}
