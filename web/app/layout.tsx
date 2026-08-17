import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";

const inter = Inter({
  subsets: ["latin"],
  variable: "--font-inter",
  display: "swap",
});

export const metadata: Metadata = {
  title: "PathWise — Trekking sicuro, decisioni intelligenti",
  description:
    "L'app Android per il trekking in montagna: algoritmo di fattibilità per il tuo gruppo, mappe offline 3D, layer POI, sincronizzazione Health Connect e SOS via SMS senza rete.",
  metadataBase: new URL("https://pathwise.vercel.app"),
  keywords: [
    "trekking",
    "alpinismo",
    "mappe offline",
    "Naismith",
    "Langmuir",
    "Health Connect",
    "SOS montagna",
    "PathWise",
  ],
  openGraph: {
    title: "PathWise — Trekking sicuro, decisioni intelligenti",
    description:
      "Valuta la fattibilità di un percorso per il tuo gruppo, in qualsiasi lingua, offline. Engine Naismith-Langmuir + calorie Pandolf, mappe 3D, SOS SMS.",
    type: "website",
    url: "https://pathwise.vercel.app",
    locale: "it_IT",
  },
  twitter: {
    card: "summary_large_image",
    title: "PathWise — Trekking sicuro, decisioni intelligenti",
    description: "Valuta la fattibilità di un percorso per il tuo gruppo, offline.",
  },
};

export default function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="it" className="scroll-smooth">
      <body className={`${inter.variable} font-sans antialiased`}>{children}</body>
    </html>
  );
}
