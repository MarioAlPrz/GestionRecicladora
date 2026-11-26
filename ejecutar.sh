#!/bin/bash
# ============================================================================
# GreenTrack - Script de Ejecución de la Aplicación
# ============================================================================

BASEDIR=$(dirname "$0")
cd "$BASEDIR"

CP="bin:resources"
if [ -d "lib" ]; then
    for f in lib/*.jar; do
        if [ -f "$f" ]; then
            CP="$CP:$f"
        fi
    done
fi

echo "Iniciando GreenTrack..."
java -cp "$CP" greentrack.ui.VentanaLogin
