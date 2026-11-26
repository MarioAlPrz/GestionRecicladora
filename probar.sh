#!/bin/bash
# ============================================================================
# GreenTrack - Script de Ejecución de Pruebas Unitarias
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

java -cp "$CP" greentrack.TestSuite
