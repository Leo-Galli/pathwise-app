# 🏔️ PathWise

**Trekking sicuro, decisioni intelligenti.** App Android nativa (Kotlin · Jetpack Compose) per la valutazione della
fattibilità dei percorsi in montagna, con mappe offline, Health Connect e SOS via SMS — più landing page web pubblicata su Vercel.

```
┌──────────────────────┐     ┌───────────────────────────────────────┐
│   android/           │     │   web/                                │
│   App nativa         │     │   Landing page Next.js (Vercel)       │
│   it.leogalli.pathwise│  ⇄  │   motore TS identico al motore Kotlin │
└──────────────────────┘     └───────────────────────────────────────┘
         scripts/deploy-github.sh · scripts/deploy-vercel.sh
```

---

## 0. Deployment automatico (GitHub + Vercel)

Prerequisiti: `gh` e `vercel` CLI autenticati (`gh auth status` / `vercel whoami`).

```bash
# 1) Repository pubblica  pathwise-app  + primo commit + push
bash scripts/deploy-github.sh

# 2) Landing page in produzione  →  https://pathwise.vercel.app
bash scripts/deploy-vercel.sh
```

Gli script sono **idempotenti** e sicuri da rieseguire.

---

## 1. Landing page web (`web/`)

- **Next.js 15 (App Router) · Tailwind CSS v4 · lucide-react**
- Sezione **PathWise Engine**: demo interattiva live che replica il motore Kotlin
  (Langmuir + Pandolf) compilato in TypeScript (`web/lib/engine.ts`)
- Sezioni: Hero con profilo altimetrico animato, funzionalità, demo layer mappa,
  modulo SOS, download APK / beta

```bash
cd web
npm install
npm run dev      # sviluppo  → http://localhost:3000
npm run build    # build produzione (validazione)
```

---

## 2. App Android (`android/`)

Apri la cartella `android/` in **Android Studio (Ladybug+)**, poi:

0. **JDK per Gradle (importante!)** — il progetto usa Gradle 8.11.1 + AGP 8.9.2,
   che supportano **Java 17–23** (sconsigliato il JBR 25.0.2 embedded di Android
   Studio, incompatibile). In *File → Settings → Build, Execution, Deployment →
   Build Tools → Gradle → Gradle JDK* seleziona **JDK 21** (o 17/23).
   Con JDK 21 il sync e i test girano a colpo sicuro:
   `./gradlew :app:testDebugUnitTest`

1. **API Key Google Maps** — senza chiave l'app gira ma la mappa resta vuota:
   ```bash
   echo "MAPS_API_KEY=AIza..." >> android/local.properties   # non committato
   ```
2. **Health Connect** — abilita il provider nell'app "Health Connect" del telefono
   e concedi i permessi alla prima sincronizzazione.
3. Build: `./gradlew :app:assembleDebug` → APK in `app/build/outputs/apk/debug/`.

### Architettura (Clean Architecture)

```
it.leogalli.pathwise/
├── di/                     # Hilt: DatabaseModule + AppModule (binds)
├── domain/
│   ├── engine/             # PathEvaluator (Naismith-Langmuir + Pandolf)
│   ├── model/              # GroupProfile, TrailGeometry, PathEvaluation, Track, Poi, MapRegion
│   ├── repository/         # interfacce (Track, Poi, Settings, FitnessSyncer)
│   └── usecase/            # EvaluatePath, SaveCompletedTrack (sync Health Connect)
├── data/
│   ├── local/              # Room DB + entità + DAO + Converters (offline-first)
│   └── repository/         # implementazioni + seed POI demo
├── ui/
│   ├── map/                # MapScreen: mappa 3D, layer, dashboard live, SOS
│   ├── evaluation/         # EvaluationScreen: motore + selettore lingua
│   ├── history/            # Storico tracce (Room Flow reattivo)
│   ├── navigation/         # Bottom bar a 3 destinazioni
│   └── theme/              # Material 3 "notte alpina"
└── util/
    ├── HealthConnectManager.kt  # lettura peso/FC, scrittura sessione+calorie+passi+km
    ├── LocationTracker.kt       # GPS a campionamento dinamico (batteria)
    ├── SosManager.kt            # SMS di emergenza con coordinate + link Maps
    └── LocaleHelper.kt          # 5 lingue senza riavviare il telefono
```

### Funzionalità chiave

| Funzionalità | Dove |
|---|---|
| **PathWise Engine** (tempi Langmuir, calorie Pandolf, verdetto per il più vulnerabile) | `domain/engine/PathEvaluator.kt` |
| **Mappe 3D + layer POI** (rifugi, bivacchi, panorami, sorgenti, pericoli) | `ui/map/MapScreen.kt` |
| **GPS a risparmio energetico** (5s→60s in base a velocità/batteria) | `util/LocationTracker.kt` |
| **Health Connect bidirezionale** | `util/HealthConnectManager.kt` |
| **SOS SMS** senza rete dati | `util/SosManager.kt` |
| **5 lingue** (it/en/de/fr/es) live, senza riavvio | `util/LocaleHelper.kt` |

### Parità motore web ⇄ app

Il motore è **uno solo**, compilato due volte:

```bash
node scripts/engine-parity.ts                      # golden values (TS)
# lato Android: ./gradlew :app:testDebugUnitTest   # assert identici (Kotlin)
```

---

## 3. Package e versioni

- Package Android: `it.leogalli.pathwise` (minSdk 26, target/compile 36)
- Kotlin 2.0.21 · AGP 8.7.3 · Compose BOM 2024.12.01 · Hilt 2.53 · Room 2.6.1
- Health Connect client 1.1.0 · Maps Compose 6.4.1
- Web: Next.js 15.3 · React 19 · Tailwind 4

## 4. Roadmap suggerita

- [ ] Import GPX e salvataggio tracce dal moto reale (completare `MapScreen` recording)
- [ ] Tile offline (es. MapTiler/MBTiles) per regioni scaricabili
- [ ] Backend POI (rifugi) con aggiornamento stagionale
- [ ] CI GitHub Actions: build APK + deploy web a ogni push su `main`

---

© 2026 PathWise · Sviluppato con ❤️ per la montagna.
