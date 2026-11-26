#!/bin/bash
# ============================================================================
# GreenTrack - Script de Compilación
# ============================================================================

set -e
BASEDIR=$(dirname "$0")
cd "$BASEDIR"

echo "=========================================================="
echo "  COMPILANDO SISTEMA GREENTRACK (Java Swing + JDBC)"
echo "=========================================================="

mkdir -p bin

CP="bin"
if [ -d "lib" ]; then
    for f in lib/*.jar; do
        if [ -f "$f" ]; then
            CP="$CP:$f"
        fi
    done
fi

javac -encoding UTF-8 -cp "$CP" -d bin $(find src -name "*.java")

echo "✔ Compilación exitosa en la carpeta 'bin/'"
echo "=========================================================="
