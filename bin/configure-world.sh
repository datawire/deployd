#!/usr/bin/env bash
set -euo pipefail

curl -X POST \
     -H "Content-Type: application/yaml" \
     -H "Accept: application/json" \
     --data "@world.json" "$MD_ADDR/world"
