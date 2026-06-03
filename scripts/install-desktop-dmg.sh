#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DMG_DIR="${ROOT_DIR}/core/apps/desktopApp/build/compose/binaries/main/dmg"
APPLICATIONS_DIR="/Applications"

if [[ "$(uname -s)" != "Darwin" ]]; then
  echo "Este script só funciona no macOS." >&2
  exit 1
fi

DMG="$(find "${DMG_DIR}" -maxdepth 1 -name '*.dmg' -print -quit)"
if [[ -z "${DMG}" ]]; then
  echo "DMG não encontrado em ${DMG_DIR}." >&2
  echo "Execute 'make desktop-dmg' primeiro." >&2
  exit 1
fi

MOUNT_POINT="$(mktemp -d "${TMPDIR:-/tmp}/investments-dmg.XXXXXX")"

cleanup() {
  if mount | grep -Fq "${MOUNT_POINT}"; then
    hdiutil detach "${MOUNT_POINT}" -quiet || true
  fi
  rmdir "${MOUNT_POINT}" 2>/dev/null || true
}
trap cleanup EXIT

echo "A montar ${DMG}..."
hdiutil attach "${DMG}" -nobrowse -readonly -mountpoint "${MOUNT_POINT}" -quiet

APP="$(find "${MOUNT_POINT}" -maxdepth 1 -name '*.app' -print -quit)"
if [[ -z "${APP}" ]]; then
  echo "Nenhum .app encontrado dentro do DMG." >&2
  exit 1
fi

APP_NAME="$(basename "${APP}")"
TARGET="${APPLICATIONS_DIR}/${APP_NAME}"

echo "A instalar ${APP_NAME} em ${APPLICATIONS_DIR}..."
rm -rf "${TARGET}"
ditto "${APP}" "${TARGET}"
echo "Instalado: ${TARGET}"
