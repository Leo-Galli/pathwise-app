#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
#  PathWise — Fase 0b · Deploy Landing Page su Vercel (produzione)
#
#  Requisiti: Node.js + Vercel CLI autenticato
#     vercel whoami            # verifica
#
#  Cosa fa:
#    1. Installa le dipendenze della web app (web/)
#    2. Esegue la build di produzione (sanità di Next.js)
#    3. Collega il progetto a Vercel (primo run: chiede nome progetto → "pathwise")
#    4. Deploy in PRODUZIONE  →  https://pathwise.vercel.app
#
#  Uso:  bash scripts/deploy-vercel.sh
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WEB_DIR="$ROOT_DIR/web"

echo "▸ Verifica CLI Vercel…"
vercel whoami >/dev/null 2>&1 || { echo "✗ Esegui prima: vercel login"; exit 1; }

cd "$WEB_DIR"

echo "▸ Installazione dipendenze…"
npm install --no-audit --no-fund

echo "▸ Build di produzione (validazione)…"
npm run build

echo "▸ Collegamento progetto a Vercel…"
vercel link --yes --project "pathwise" 2>/dev/null || vercel link --yes

echo "▸ Deploy in PRODUZIONE…"
# L'output termina con "✓ Ready in 1m": estraiamo l'URL reale dalla riga
# "▲ Aliased https://..." (tail -1 catturerebbe la riga sbagliata).
# `|| true` evita che set -e interrompa prima del guard sotto (e lascia che
# sia il messaggio "URL non rilevato" a spiegare l'errore).
DEPLOY_URL="$(vercel deploy --prod --yes 2>&1 | grep -oE 'https://[a-z0-9.-]+\.vercel\.app' | tail -1)" || true
if [ -z "$DEPLOY_URL" ]; then
  echo "✗ Deploy eseguito ma URL non rilevato; verifica con: vercel inspect --prod"
  exit 1
fi
echo
echo "✔ Landing page in produzione:  $DEPLOY_URL"
# Nota: `pathwise.vercel.app` può essere già occupato; Vercel assegna un alias
# libero (es. pathwise-umber.vercel.app). L'URL esatto è quello stampato sopra.
# Serve `2>&1`: Vercel CLI scrive parte dell'output su stderr.
