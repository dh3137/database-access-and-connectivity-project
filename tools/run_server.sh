#!/usr/bin/env bash
# run_server.sh — Compile and start the AutoPrime HTTP server
# Usage: bash tools/run_server.sh
# See workflows/run_server.md for full context

set -euo pipefail

echo "→ Compiling and starting AutoPrime on http://localhost:8080 ..."
mvn compile exec:java
