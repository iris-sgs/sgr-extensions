#!/usr/bin/env bash
# Regenerate index.min.json / repo.json from the known extension metadata.
# Keep `version` as "<libversion>.<code>" with libversion in [1.3, 1.5] — Suwayomi
# parses `version.substringBeforeLast('.')` as the lib version and filters on it.
set -euo pipefail

NAME="Tachiyomi: Cyrisia Light Novels"
PKG="eu.kanade.tachiyomi.extension.en.cyrisia"
LIB="1.4"
CODE="${1:-1}"
VERSION="${LIB}.${CODE}"
APK="tachiyomi-en.cyrisia-v${VERSION}.apk"

read -r -d '' JSON <<EOF || true
[
  {
    "name": "${NAME}",
    "pkg": "${PKG}",
    "apk": "${APK}",
    "lang": "en",
    "code": ${CODE},
    "version": "${VERSION}",
    "nsfw": 0
  }
]
EOF

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
printf '%s\n' "$JSON" > "$ROOT/index.min.json"
printf '%s\n' "$JSON" > "$ROOT/repo.json"
echo "Wrote index.min.json and repo.json (apk=$APK)"
