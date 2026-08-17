#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
#  PathWise — Fase 0a · Creazione Repository GitHub Pubblica
#
#  Requisiti: git + GitHub CLI (gh) autenticato
#     gh auth status            # verifica
#
#  Cosa fa:
#    1. Inizializza la repo git locale (se non già presente)
#    2. Crea la repository pubblica  "pathwise-app"  sull'account autenticato
#    3. Push del primo commit iniziale (codice + landing page)
#
#  Uso:  bash scripts/deploy-github.sh
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

REPO_NAME="pathwise-app"
BRANCH="main"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "▸ Verifica autenticazione GitHub…"
# gh 2.97 non ha il flag --quiet: l'exit code di `gh auth status` è sufficiente
gh auth status >/dev/null 2>&1 || { echo "✗ Esegui prima: gh auth login"; exit 1; }

# Helper: riprova un comando `gh` fino a 5 volte (l'API GitHub può rispondere 503/504
# in momenti di carico; la REST è talvolta degradata mentre GraphQL resta attivo).
gh_retry() {
  local attempt=1
  until "$@"; do
    if [ "$attempt" -ge 5 ]; then
      echo "  ✗ API GitHub non raggiungibile dopo 5 tentativi: riprova tra qualche minuto."
      return 1
    fi
    echo "  · tentativo $attempt/5 fallito, nuovo tentativo tra 10s…"
    attempt=$((attempt + 1))
    sleep 10
  done
}

echo "▸ Inizializzazione repository git locale…"
if [ ! -d .git ]; then
  git init -b "$BRANCH"
else
  echo "  · repo già inizializzata"
fi

echo "▸ Configurazione utente git locale…"
# Username: priorità a git config (mai fallisce) e solo in assenza all'API REST
# (che può essere degradata 503/504 e restituire JSON di errore su stdout).
GITHUB_USER="$(git config user.name 2>/dev/null || gh api user -q .login 2>/dev/null)"
if [ -z "$GITHUB_USER" ]; then
  echo "✗ Impossibile determinare l'utente GitHub. Imposta: git config user.name"
  exit 1
fi
git config user.name  "${GIT_AUTHOR_NAME:-$GITHUB_USER}"
git config user.email "${GIT_AUTHOR_EMAIL:-$GITHUB_USER@users.noreply.github.com}"

echo "▸ Verifica esistenza repo remota (creazione idempotente)…"
if gh repo view "$GITHUB_USER/$REPO_NAME" >/dev/null 2>&1; then
  echo "  · repo esistente, collego soltanto il remote"
  # Aggiunge il remote se manca (branch idempotente: il push finale ne ha bisogno)
  if ! git remote get-url origin >/dev/null 2>&1; then
    git remote add origin "https://github.com/$GITHUB_USER/$REPO_NAME.git"
  fi
else
  echo "▸ Creazione repository pubblica: $GITHUB_USER/$REPO_NAME"
  if [ -z "$(git log --oneline -1 2>/dev/null)" ]; then
    # Nessun commit ancora: crea la repo e aggiunge il remote, il commit/push
    # avvengono nei passi successivi dello script (l'ordine giusto per gh repo create).
    gh_retry gh repo create "$GITHUB_USER/$REPO_NAME" --public --confirm
    git remote add origin "https://github.com/$GITHUB_USER/$REPO_NAME.git"
  else
    # Ci sono già commit: crea la repo, collega il remote e fai il push.
    gh_retry gh repo create "$REPO_NAME" --public --source . --remote origin --push || {
      # fallback: crea e aggiunge remote manualmente (idempotente: se `--push`
      # era già riuscito ad aggiungere origin, evita il doppio add che romperebbe set -e)
      gh_retry gh repo create "$GITHUB_USER/$REPO_NAME" --public --confirm
      git remote add origin "https://github.com/$GITHUB_USER/$REPO_NAME.git" 2>/dev/null || true
    }
  fi
fi

echo "▸ Primo commit iniziale…"
git add -A
if git diff --cached --quiet; then
  echo "  · nessuna modifica da committare"
else
  git commit -m "chore: setup iniziale PathWise (web + android + deploy scripts)" --no-verify
fi

echo "▸ Push su origin/$BRANCH…"
git push -u origin "$BRANCH" 2>/dev/null || git push --set-upstream origin "$BRANCH"

echo
echo "✔ Repository pubblica pronta:  https://github.com/$GITHUB_USER/$REPO_NAME"
