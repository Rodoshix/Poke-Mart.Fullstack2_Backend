#!/usr/bin/env bash
set -euo pipefail

echo ">> Preparando Oracle Wallet..."

mkdir -p /app/wallet
echo "$ORACLE_WALLET_ZIP_B64" | base64 -d > /tmp/wallet.zip

cd /app/wallet
jar xf /tmp/wallet.zip

WALLET_DIR="$(find /app/wallet -maxdepth 4 -name tnsnames.ora -print -quit | xargs -r dirname)"
if [ -z "${WALLET_DIR:-}" ]; then
  echo "ERROR: No se encontró tnsnames.ora tras extraer el wallet."
  exit 1
fi

export TNS_ADMIN="$WALLET_DIR"
export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} -Doracle.net.tns_admin=$WALLET_DIR"

echo ">> TNS_ADMIN=$TNS_ADMIN"

JAR="$(ls -1 build/libs/*.jar 2>/dev/null | head -n 1 || true)"
if [ -n "$JAR" ]; then
  exec java -jar "$JAR"
else
  exec ./gradlew bootRun --no-daemon
fi
