package com.letaa.app.net

import android.text.Html
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Реальный поиск без ключей и подписок: официальное открытое MediaWiki
 * Search API Википедии (никакого скрапинга HTML, никаких ToS-нарушений).
 * Даёт настоящие заголовки/сниппеты/страницы — карточки в «летаа» открывают
 * подлинные веб-страницы через WebView.
 *
 * Плюс всегда добавляется карточка «искать в интернете» — она открывает
 * реальный поисковик (DuckDuckGo) прямо в безграничном WebView, так что
 * пользователь получает полноценную живую выдачу, а не только вики-статьи.
 */
object SearchClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val cyrillic = Regex("[\\u0400-\\u04FF]")

    fun isLikelyUrl(input: String): Boolean {
        val q = input.trim()
        if (q.isEmpty() || q.contains(' ')) return false
        if (q.startsWith("http://") || q.startsWith("https://")) return true
        // простая эвристика адреса: домен с точкой и без пробелов, напр. "ya.ru"
        return Regex("^[\\w-]+(\\.[\\w-]+)+(/.*)?$").matches(q)
    }

    fun normalizeUrl(input: String): String {
        val q = input.trim()
        return if (q.startsWith("http://") || q.startsWith("https://")) q else "https://$q"
    }

    fun webSearchUrl(query: String): String {
        val enc = URLEncoder.encode(query.trim(), "UTF-8")
        return "https://duckduckgo.com/?q=$enc"
    }

    suspend fun search(query: String): List<SearchResult> = withContext(Dispatchers.IO) {
        val q = query.trim()
        if (q.isEmpty()) return@withContext emptyList()
        val lang = if (cyrillic.containsMatchIn(q)) "ru" else "en"
        val enc = URLEncoder.encode(q, "UTF-8")
        val url = "https://$lang.wikipedia.org/w/api.php" +
            "?action=query&list=search&format=json&formatversion=2&srlimit=6&srsearch=$enc"
        val request = Request.Builder().url(url).header("User-Agent", "letaa-android/1.0").build()
        try {
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext emptyList()
                val body = resp.body?.string() ?: return@withContext emptyList()
                val root = JSONObject(body)
                val search = root.optJSONObject("query")?.optJSONArray("search") ?: return@withContext emptyList()
                val out = mutableListOf<SearchResult>()
                for (i in 0 until search.length()) {
                    val item = search.getJSONObject(i)
                    val title = item.optString("title")
                    val snippetHtml = item.optString("snippet")
                    val snippet = Html.fromHtml(snippetHtml, Html.FROM_HTML_MODE_LEGACY).toString()
                    val pageTitleUrl = URLEncoder.encode(title.replace(' ', '_'), "UTF-8")
                    val pageUrl = "https://$lang.wikipedia.org/wiki/$pageTitleUrl"
                    out.add(
                        SearchResult(
                            title = title,
                            url = pageUrl,
                            domain = "$lang.wikipedia.org",
                            snippet = snippet,
                        )
                    )
                }
                out
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
