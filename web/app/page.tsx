import Navbar from "@/components/Navbar";
import Hero from "@/components/Hero";
import Stats from "@/components/Stats";
import Features from "@/components/Features";
import EngineDemo from "@/components/EngineDemo";
import LayersDemo from "@/components/LayersDemo";
import SosSection from "@/components/SosSection";
import Download from "@/components/Download";
import Footer from "@/components/Footer";

export default function Home() {
  return (
    <main className="relative min-h-screen bg-night-950">
      {/* Glow di sfondo globale */}
      <div
        aria-hidden
        className="pointer-events-none fixed inset-0 z-0"
        style={{
          background:
            "radial-gradient(60% 40% at 15% 0%, rgba(16,185,129,0.09), transparent 60%), radial-gradient(50% 35% at 90% 12%, rgba(56,189,248,0.07), transparent 60%), radial-gradient(45% 45% at 50% 100%, rgba(52,211,153,0.05), transparent 60%)",
        }}
      />

      <div className="relative z-10">
        <Navbar />
        <Hero />
        <Stats />
        <Features />
        <EngineDemo />
        <LayersDemo />
        <SosSection />
        <Download />
        <Footer />
      </div>
    </main>
  );
}
