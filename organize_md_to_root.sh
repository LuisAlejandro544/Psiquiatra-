#!/usr/bin/env bash
# ==============================================================================
# Script: organize_md_to_root.sh
# Objetivo: Mover y asegurar que todos los archivos Markdown (.md) de documentación
#           estén ubicados exclusivamente en la raíz del proyecto y no en app/
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$SCRIPT_DIR"

echo "=== Moviendo y centralizando archivos .md en la raíz del repositorio ==="
echo "Directorio raíz: $ROOT_DIR"

# 1. Buscar archivos .md dentro de la carpeta 'app/'
if [ -d "$ROOT_DIR/app" ]; then
    find "$ROOT_DIR/app" -maxdepth 1 -name "*.md" | while read -r md_file; do
        filename="$(basename "$md_file")"
        echo " -> Moviendo: $md_file a $ROOT_DIR/$filename"
        cp "$md_file" "$ROOT_DIR/$filename"
        rm -f "$md_file"
    done
fi

echo "✔ Archivos .md centralizados en la raíz exitosamente:"
ls -la "$ROOT_DIR"/*.md
