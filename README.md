# sgr-extensions

Private Tachiyomi/Suwayomi extension repo for the SGR (Super Giga Reader) homelab.

Targets **Suwayomi v2.2.2100** (Tachiyomi 1.x RxJava extension API, `lib_version` 1.4).

## Sources

| Source | Lang | Site | Notes |
|--------|------|------|-------|
| Cyrisia Light Novels | en | https://cyrisia.com | epub light novels; bytes served from `server.elscione.com` |

### How Cyrisia works

- **Browse / search** parse `https://cyrisia.com/sitemap.xml`:
  - `/series/{name}` → one manga entry per series (title = URL-decoded name).
  - `/read/{series}/{file}.epub` → one chapter (volume) per epub.
- **Details** scrape the SSR series page (`/series/{name}`) for synopsis + genre chips;
  `"Light Novel"` is prepended to the genres.
- **Pages** map the cyrisia `/read/` path onto the epub mirror:
  `https://server.elscione.com/Officially%20Translated%20Light%20Novels/{series}/{file}.epub`
  and return it as a single page for the epub reader.

## Build

CI (`.github/workflows/build.yml`) builds a **debug-signed** APK on every push to `main`,
uploads it as an artifact, and attaches it to the `v1.4.1` GitHub Release.

The extension API (`eu.kanade.tachiyomi.*`), OkHttp, Jsoup and RxJava are `compileOnly` —
they are provided by the Suwayomi runtime and are **not** bundled in the APK.

## Installing into Suwayomi

1. Download `tachiyomi-en.cyrisia-v1.4.1.apk` from the latest Release.
2. Put it at `~/sgr/ext/apk/tachiyomi-en.cyrisia-v1.4.1.apk` on the SGR host (192.168.0.125).
   The repo index is served by the SGR backend at `http://192.168.0.125:3001/ext/`.
3. In Suwayomi → Settings → Browse → Extension repos, add:
   `http://192.168.0.125:3001/ext/index.min.json`
   (the bare `http://192.168.0.125:3001/ext/` also resolves to the index).
4. Install **Cyrisia Light Novels** from the Extensions list (you may need to trust the
   debug signature).

## Repo index

`index.min.json` / `repo.json` describe the published extension. Regenerate with
`scripts/gen-repo-json.sh [code]`. `version` must stay `1.4.x` (Suwayomi reads it as the
lib version and only accepts `[1.3, 1.5]`).
