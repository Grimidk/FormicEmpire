#!/usr/bin/env bash
# Writes PascalCase PNG placeholders under src/main/resources/sprites/buildings/rooms/
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
  local head="${chain^}"
  for l in 0 1 2 3; do
    scale_to "$src" "$OUT/${head}L${l}.png"
  done
}

emit_chain royal   icons/ants/omni/Queen.png
emit_chain egg     icons/ants/Egg.png
emit_chain mushroom icons/resources/Mushroom.png
emit_chain plant   icons/resources/Plant.png
emit_chain water   icons/resources/Water.png
emit_chain meat    icons/resources/Protein.png
emit_chain syrup   icons/resources/Syrup.png
emit_chain rock    icons/resources/Mineral.png
emit_chain resin   icons/resources/Resin.png

# Passives (single file each)
scale_to "$RES/icons/misc/Research.png"       "$OUT/PassiveLab.png"
scale_to "$RES/icons/resources/Water.png"      "$OUT/PassiveWater.png"
scale_to "$RES/icons/bugs/Aphid.png"          "$OUT/PassiveAphid.png"
scale_to "$RES/icons/ants/Larva.png"          "$OUT/PassiveNurse.png"
scale_to "$RES/icons/resources/Mushroom.png"  "$OUT/PassiveFarm.png"
scale_to "$RES/icons/status/Dead.png"         "$OUT/PassiveGrave.png"
scale_to "$RES/icons/biomes/Swamp.png"        "$OUT/PassiveComposter.png"

echo "Wrote $(find "$OUT" -name '*.png' | wc -l | tr -d ' ') PNGs to $OUT"
