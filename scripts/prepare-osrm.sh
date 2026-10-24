#!/usr/bin/env bash
# Downloads an OpenStreetMap extract from Geofabrik and prepares it for OSRM
# (car profile, Multi-Level Dijkstra pipeline).
#
# Usage: scripts/prepare-osrm.sh [geofabrik-region-path]
#   e.g. scripts/prepare-osrm.sh europe/germany/berlin
set -euo pipefail

# Stop Git Bash on Windows from rewriting /data paths passed to docker
export MSYS_NO_PATHCONV=1

REGION="${1:-europe/united-kingdom/england/greater-london}"
IMAGE="ghcr.io/project-osrm/osrm-backend"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DATA_DIR="${ROOT_DIR}/osrm-data"
URL="https://download.geofabrik.de/${REGION}-latest.osm.pbf"

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker is required but was not found on your PATH." >&2
  exit 1
fi

# On Linux, run as the current user so the processed files aren't owned by root
USER_FLAG=""
if [ "$(uname -s)" = "Linux" ]; then
  USER_FLAG="--user $(id -u):$(id -g)"
fi

mkdir -p "${DATA_DIR}"

echo "==> Downloading ${URL}"
(
  cd "${DATA_DIR}"
  curl --fail --location --progress-bar --output "map.osm.pbf.tmp" "${URL}"
  mv "map.osm.pbf.tmp" "map.osm.pbf"
)
echo "==> Removing any previously processed data"
rm -f "${DATA_DIR}"/map.osrm*

osrm() {
  # shellcheck disable=SC2086
  docker run --rm ${USER_FLAG} -v "${DATA_DIR}:/data" "${IMAGE}" "$@"
}

echo "==> Step 1/3: osrm-extract"
osrm osrm-extract -p /opt/car.lua /data/map.osm.pbf

echo "==> Step 2/3: osrm-partition"
osrm osrm-partition /data/map.osrm

echo "==> Step 3/3: osrm-customize"
osrm osrm-customize /data/map.osrm

echo "Done. Start everything with: docker compose up --build"