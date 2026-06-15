#!/usr/bin/env bash
# Writes camelCase PNG placeholders under src/main/resources/sprites/buildings/rooms/
# by scaling source icons (48x48). Re-run after changing icon sources or when refreshing placeholders.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
RES="$ROOT/src/main/resources"
OUT="$RES/sprites/buildings/rooms"
SIZE=48
mkdir -p "$OUT"

scale_to() {
  local src="$1"
  local dst="$2"
  if [[ ! -f "$src" ]]; then
    echo "Missing source: $src" >&2
    exit 1
  fi
  sips -z "$SIZE" "$SIZE" "$src" --out "$dst" >/dev/null
}

# Main chains: same icon copied to L0..L3 (replace per-tier art later).
emit_chain() {
  local chain="$1"
  local src="$RES/$2"
  for l in 0 1 2 3; do
    scale_to "$src" "$OUT/${chain}L${l}.png"
  done
}

emit_chain royal   icons/ants/omni/queen.png
emit_chain egg     icons/ants/egg.png
emit_chain mushroom icons/resources/mushroom.png
emit_chain plant   icons/resources/plant.png
emit_chain water   icons/resources/water.png
emit_chain meat    icons/resources/protein.png
emit_chain syrup   icons/resources/syrup.png
emit_chain rock    icons/resources/mineral.png
emit_chain resin   icons/resources/resin.png

# Passives (single file each)
scale_to "$RES/icons/misc/research.png"       "$OUT/passiveLab.png"
scale_to "$RES/icons/resources/water.png"      "$OUT/passiveWater.png"
scale_to "$RES/icons/bugs/aphid.png"          "$OUT/passiveAphid.png"
scale_to "$RES/icons/ants/larva.png"          "$OUT/passiveNurse.png"
scale_to "$RES/icons/resources/mushroom.png"  "$OUT/passiveFarm.png"
scale_to "$RES/icons/status/dead.png"         "$OUT/passiveGrave.png"
scale_to "$RES/icons/biomes/swamp.png"        "$OUT/passiveComposter.png"

echo "Wrote $(find "$OUT" -name '*.png' | wc -l | tr -d ' ') PNGs to $OUT"
