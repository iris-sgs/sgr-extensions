package eu.kanade.tachiyomi.extension.en.cyrisia

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.asObservableSuccess
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import rx.Observable
import java.net.URLDecoder

/**
 * cyrisia.com — epub light-novel reader.
 *
 * Discovery is driven entirely off the site's sitemap.xml:
 *   - `/series/{name}`                  → one SManga per series
 *   - `/read/{series}/{filename}.epub`  → one SChapter (volume) per epub
 *
 * The epub bytes themselves live on a separate host whose path mirrors the
 * cyrisia `/read/` path exactly:
 *   https://server.elscione.com/Officially%20Translated%20Light%20Novels/{series}/{filename}.epub
 *
 * So a page is just a single "image" pointing at that epub URL, which the
 * Suwayomi epub reader downloads and renders.
 */
class Cyrisia : HttpSource() {

    override val name = "Cyrisia Light Novels"

    override val baseUrl = "https://cyrisia.com"

    override val lang = "en"

    override val supportsLatest = false

    private val sitemapUrl = "$baseUrl/sitemap.xml"

    // Trailing slash kept: the elscione path == cyrisia /read/ path, prepend only.
    private val epubBase = "https://server.elscione.com/Officially%20Translated%20Light%20Novels/"

    private val locRegex = Regex("""<loc>\s*https://cyrisia\.com([^<]+?)\s*</loc>""")

    private fun decode(s: String): String = URLDecoder.decode(s, "UTF-8")

    // ---- popular / browse: every series in the sitemap ----

    override fun popularMangaRequest(page: Int): Request = GET(sitemapUrl, headers)

    override fun popularMangaParse(response: Response): MangasPage {
        val body = response.body.string()
        val mangas = locRegex.findAll(body)
            .map { it.groupValues[1] }
            .filter { it.startsWith("/series/") && !it.removePrefix("/series/").contains("/") }
            .distinct()
            .map { path ->
                SManga.create().apply {
                    url = path // e.g. "/series/86" (URL-encoded form preserved)
                    title = decode(path.removePrefix("/series/"))
                    initialized = false
                }
            }
            .toList()
        return MangasPage(mangas, false)
    }

    // ---- search: same list, filtered client-side by title ----

    override fun fetchSearchManga(page: Int, query: String, filters: FilterList): Observable<MangasPage> {
        return client.newCall(GET(sitemapUrl, headers))
            .asObservableSuccess()
            .map { response ->
                val all = popularMangaParse(response).mangas
                val filtered = if (query.isBlank()) {
                    all
                } else {
                    all.filter { it.title.contains(query, ignoreCase = true) }
                }
                MangasPage(filtered, false)
            }
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request =
        GET(sitemapUrl, headers)

    override fun searchMangaParse(response: Response): MangasPage = popularMangaParse(response)

    // ---- details: scrape the SSR series page ----

    override fun mangaDetailsParse(response: Response): SManga {
        val document = Jsoup.parse(response.body.string(), baseUrl)
        return SManga.create().apply {
            document.selectFirst("div.stitle")?.text()?.takeIf { it.isNotBlank() }?.let { title = it }
            description = document.selectFirst("div.series-meta p, div.synopsis-wrap p, div.main p")
                ?.text()
            val genres = document.select("span.meta-chip").map { it.text() }
                .filter { it.isNotBlank() }
                .toMutableList()
            genres.add(0, "Light Novel")
            genre = genres.distinct().joinToString(", ")
            document.selectFirst("img.cover")?.absUrl("src")?.takeIf { it.isNotBlank() }
                ?.let { thumbnail_url = it }
            initialized = true
        }
    }

    // ---- chapters: every /read/ epub whose first path segment is this series ----

    override fun fetchChapterList(manga: SManga): Observable<List<SChapter>> {
        val seriesName = decode(manga.url.removePrefix("/series/"))
        return client.newCall(GET(sitemapUrl, headers))
            .asObservableSuccess()
            .map { response ->
                val body = response.body.string()
                locRegex.findAll(body)
                    .map { it.groupValues[1] }
                    .filter { it.startsWith("/read/") && it.endsWith(".epub") }
                    .filter { decode(it.removePrefix("/read/").substringBefore("/")) == seriesName }
                    .map { path ->
                        val filenameEnc = path.removePrefix("/read/").substringAfter("/")
                        val filename = decode(filenameEnc).removeSuffix(".epub")
                        val volNum = Regex("""Volume\s+(\d+)""").find(filename)
                            ?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
                        SChapter.create().apply {
                            url = path // "/read/{series}/{filename}.epub" — encoded, preserved
                            name = filename
                            chapter_number = volNum
                        }
                    }
                    .sortedByDescending { it.chapter_number }
                    .toList()
            }
    }

    override fun chapterListParse(response: Response): List<SChapter> =
        throw UnsupportedOperationException("Not used; see fetchChapterList")

    // ---- pages: a single epub "page" pointing at the elscione mirror ----

    override fun pageListParse(response: Response): List<Page> {
        // response.request.url is the cyrisia /read/ URL; its encoded path mirrors
        // the elscione path exactly, so we just re-base it onto the epub host.
        val rel = response.request.url.encodedPath.removePrefix("/read/")
        val epubUrl = epubBase + rel
        return listOf(Page(0, imageUrl = epubUrl))
    }

    override fun imageUrlParse(response: Response): String =
        throw UnsupportedOperationException("Not used")

    // ---- latest: unsupported ----

    override fun latestUpdatesRequest(page: Int): Request =
        throw UnsupportedOperationException("Not used")

    override fun latestUpdatesParse(response: Response): MangasPage =
        throw UnsupportedOperationException("Not used")
}
