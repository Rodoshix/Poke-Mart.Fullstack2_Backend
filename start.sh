#!/usr/bin/env bash
set -euo pipefail

echo ">> Preparando Oracle Wallet..."
APP_DIR="/app"
WALLET_TMP="/app/wallet"

mkdir -p "$WALLET_TMP"
echo "$ORACLE_WALLET_ZIP_B64" | base64 -d > /tmp/wallet.zip

cd "$WALLET_TMP"
jar xf /tmp/wallet.zip

WALLET_DIR="$(find "$WALLET_TMP" -maxdepth 4 -name tnsnames.ora -print -quit | xargs -r dirname)"
if [ -z "${WALLET_DIR:-}" ]; then
  echo "ERROR: No se encontro tnsnames.ora tras extraer el wallet."
  exit 1
fi

export TNS_ADMIN="$WALLET_DIR"
export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} -Doracle.net.tns_admin=$WALLET_DIR"

echo ">> TNS_ADMIN=$TNS_ADMIN"
echo ">> Wallet detectado en: $WALLET_DIR"

# Log de entorno relevante para debug en Railway
echo ">> PORT=${PORT:-<no-definido>}"
echo ">> JAVA_TOOL_OPTIONS=${JAVA_TOOL_OPTIONS:-<no-definido>}"

# Volver al directorio raiz de la app para que gradlew/JAR esten disponibles
cd "$APP_DIR"

echo ">> Contenido de build/libs:"
ls -1 build/libs 2>/dev/null || true

# Preferimos el bootJar (no plain). Si no existe, tomamos el primer .jar
JAR="$(ls -1 build/libs/*-SNAPSHOT.jar 2>/dev/null | grep -v -- '-plain' | head -n 1 || true)"
if [ -z "$JAR" ]; then
  JAR="$(ls -1 build/libs/*.jar 2>/dev/null | head -n 1 || true)"
fi

if [ -n "$JAR" ]; then
  echo ">> Ejecutando JAR: $JAR"
  exec java -jar "$JAR"
fi

echo ">> Ejecutando ./gradlew bootRun"
chmod +x ./gradlew || true
exec ./gradlew bootRun --no-daemon
