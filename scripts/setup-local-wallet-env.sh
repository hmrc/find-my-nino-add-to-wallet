#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

SECRETS_DIR="$HOME/.hmrc/find-my-nino-add-to-wallet"
OUT_DIR="$PROJECT_ROOT/target/wallet-local"
ENV_FILE="$OUT_DIR/wallet-local.env"

APPLE_KEY="$SECRETS_DIR/apple-dev.key"
APPLE_CRT="$SECRETS_DIR/apple-dev.crt"
APPLE_P12="$SECRETS_DIR/apple-dev.p12"

GOOGLE_JSON="$SECRETS_DIR/google-wallet-dev.json"

APPLE_P12_PASSWORD="devpass"

log() { echo "[wallet-env-setup] $*"; }

mkdir -p "$SECRETS_DIR"
mkdir -p "$OUT_DIR"

########################################
# 1) Generate Apple key/cert if missing
########################################
if [ ! -f "$APPLE_KEY" ] || [ ! -f "$APPLE_CRT" ]; then
  log "Generating Apple self-signed key+cert (dev only)..."
  openssl req -x509 -newkey rsa:2048 \
    -keyout "$APPLE_KEY" \
    -out "$APPLE_CRT" \
    -days 365 \
    -nodes \
    -subj "/CN=Dev Apple Pass/O=HMRC Dev/OU=NI"

  chmod 600 "$APPLE_KEY"
  chmod 644 "$APPLE_CRT"
  log "✔ Created:"
  log "  $APPLE_KEY"
  log "  $APPLE_CRT"
else
  log "⏭ Apple key/cert already exist — skipping"
fi

########################################
# 2) Generate Apple PKCS12 if missing
########################################
if [ ! -f "$APPLE_P12" ]; then
  log "Creating Apple PKCS12 (.p12) bundle..."
  openssl pkcs12 -export \
    -inkey "$APPLE_KEY" \
    -in "$APPLE_CRT" \
    -out "$APPLE_P12" \
    -name "HMRC Dev Pass" \
    -password "pass:$APPLE_P12_PASSWORD"

  chmod 600 "$APPLE_P12"
  log "✔ Created: $APPLE_P12"
else
  log "⏭ Apple p12 already exists — skipping"
fi

########################################
# GOOGLE SERVICE ACCOUNT JSON
########################################

if [ ! -f "$GOOGLE_JSON" ]; then
  log "❌ Missing Google Wallet service account key:"
  log "  $GOOGLE_JSON"
  log ""
  log "This file cannot be generated automatically."
  log ""
  log "Steps to create it:"
  log "1. Open Google Cloud Console:"
  log "   https://console.cloud.google.com/iam-admin/serviceaccounts"
  log ""
  log "2. Select your DEV project"
  log "3. Create or select a service account"
  log "4. Go to 'Keys' → 'Add key' → 'Create new key' → JSON"
  log "5. Download the file"
  log "6. Save it as:"
  log "   $GOOGLE_JSON"
  log ""
  log "Opening browser now..."
  open "https://console.cloud.google.com/iam-admin/serviceaccounts" || true
  exit 1
fi

log "✔ Found Google Wallet service account JSON"

########################################
# 4) Base64 encode (always refresh)
########################################
log "Base64-encoding Apple cert and p12, and Google JSON..."

APPLE_CRT_B64="$(base64 < "$APPLE_CRT" | tr -d '\n')"
APPLE_P12_B64="$(base64 < "$APPLE_P12" | tr -d '\n')"
GOOGLE_JSON_B64="$(base64 < "$GOOGLE_JSON" | tr -d '\n')"

########################################
# 5) Write env file
########################################
log "Writing env file → $ENV_FILE"

cat > "$ENV_FILE" <<EOF
# Generated – do not commit

export PRIVATE_CERTIFICATE_PASSWORD="$APPLE_P12_PASSWORD"
export PRIVATE_CERTIFICATE="$APPLE_P12_B64"
export PUBLIC_CERTIFICATE="$APPLE_CRT_B64"
export GOOGLE_PASS_KEY="$GOOGLE_JSON_B64"

# Local dev only – secure default is true when unset
export APPLE_PASS_SIGNING_ENABLED=false
EOF

log "✔ Done"
log ""
log "Load env with:"
log "  source \"$ENV_FILE\""
