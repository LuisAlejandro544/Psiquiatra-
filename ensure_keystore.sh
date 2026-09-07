#!/usr/bin/env bash
# ==============================================================================
# Script: ensure_keystore.sh
# Objetivo: Garantizar la existencia de debug.keystore para firmar el APK Debug
#           en entornos de integración continua (CI / GitHub Actions) o locales.
#           Evita bloqueos o esperas de claves interactivas en pipelines desatendidos.
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KEYSTORE_PATH="${1:-$SCRIPT_DIR/debug.keystore}"
KEY_ALIAS="androiddebugkey"
STORE_PASS="android"
KEY_PASS="android"

echo "=== Verificación de Firma para APK Debug ==="
echo "Ruta de la Keystore: $KEYSTORE_PATH"

if [ -f "$KEYSTORE_PATH" ] && [ -s "$KEYSTORE_PATH" ]; then
    echo "✔ Keystore existente encontrada en $KEYSTORE_PATH."
    echo "✔ Verificando alias '$KEY_ALIAS'..."
    if keytool -list -keystore "$KEYSTORE_PATH" -storepass "$STORE_PASS" -alias "$KEY_ALIAS" >/dev/null 2>&1; then
        echo "✔ Keystore válida y lista para firmar el APK Debug."
        exit 0
    else
        echo "⚠ El alias no coincide o la clave está corrupta. Regenerando..."
    fi
fi

echo "🚀 Generando debug.keystore de forma no interactiva con keytool..."
keytool -genkeypair \
    -alias "$KEY_ALIAS" \
    -keypass "$KEY_PASS" \
    -keystore "$KEYSTORE_PATH" \
    -storepass "$STORE_PASS" \
    -dname "CN=Android Debug,O=Android,C=US" \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000

# Verificación de integridad
if [ -f "$KEYSTORE_PATH" ] && [ -s "$KEYSTORE_PATH" ]; then
    echo "✔ debug.keystore generada y verificada exitosamente en: $KEYSTORE_PATH"
else
    echo "❌ Error: No se pudo generar la keystore de depuración."
    exit 1
fi
