package com.atride.cook.data.tools

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText

/**
 * 统一的搜索结果结构。
 */
data class SearchResult(
    val title: String,
    val url: String,
    val snippet: String,
    val source: String,
)

/**
 * 搜索引擎接口。实现此接口即可注册一个新的搜索源。
 */
interface SearchEngine {
    val name: String
    suspend fun search(client: HttpClient, query: String): List<SearchResult>
}

// ====================================================================
// DuckDuckGo
// ====================================================================

/**
 * DuckDuckGo HTML 搜索。无需 API Key，通过解析 HTML 页面提取结果。
 */
object DuckDuckGo : SearchEngine {
    override val name = "DuckDuckGo"

    override suspend fun search(client: HttpClient, query: String): List<SearchResult> {
        return try {
            val html = client.get("https://html.duckduckgo.com/html/") {
                parameter("q", query)
            }.bodyAsText()

            parseHtmlResults(html)
        } catch (e: Exception) {
            listOf(SearchResult(
                title = "[DuckDuckGo error]",
                url = "",
                snippet = "Search failed: ${e.message?.take(100) ?: "unknown error"}",
                source = name,
            ))
        }
    }

    private fun parseHtmlResults(html: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()

        // DuckDuckGo HTML 页面中每条结果的结构：
        //   <h2 class="result__title">
        //     <a class="result__a" href="URL">TITLE</a>
        //   </h2>
        //   <a class="result__snippet" ...>SNIPPET</a>

        val titlePattern = """<a[^>]*class="result__a"[^>]*href="([^"]*)"[^>]*>([\s\S]*?)</a>""".toRegex()
        val snippetPattern = """<a[^>]*class="result__snippet"[^>]*>([\s\S]*?)</a>""".toRegex()

        val titles = titlePattern.findAll(html).map { match ->
            match.groupValues[1] to stripHtmlTags(match.groupValues[2])
        }.toList()

        val snippets = snippetPattern.findAll(html).map { match ->
            stripHtmlTags(match.groupValues[1])
        }.toList()

        for (i in titles.indices) {
            val (url, title) = titles[i]
            val snippet = if (i < snippets.size) snippets[i] else ""
            results.add(SearchResult(
                title = title.trim(),
                url = url,
                snippet = snippet.trim().replace("""\s+""".toRegex(), " "),
                source = name,
            ))
        }

        return results
    }
}

// ====================================================================
// Wikipedia
// ====================================================================

/**
 * Wikipedia API 搜索。免费、结构化 JSON，适合百科类查询。
 */
object Wikipedia : SearchEngine {
    override val name = "Wikipedia"

    override suspend fun search(client: HttpClient, query: String): List<SearchResult> {
        return try {
            val response = client.get("https://en.wikipedia.org/w/api.php") {
                parameter("action", "query")
                parameter("list", "search")
                parameter("srsearch", query)
                parameter("format", "json")
                parameter("srlimit", "5")
            }.bodyAsText()

            parseJsonResults(response)
        } catch (e: Exception) {
            listOf(SearchResult(
                title = "[Wikipedia error]",
                url = "",
                snippet = "Search failed: ${e.message?.take(100) ?: "unknown error"}",
                source = name,
            ))
        }
    }

    private fun parseJsonResults(json: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()

        // 手动解析简单的 JSON 结构，避免引入额外的 JSON 解析依赖
        // 格式：{"query":{"search":[{"title":"...","snippet":"...",...},...]}}
        val searchMatch = """\[[\s\S]*?\]""".toRegex()
            .find(json.substringAfter(""""search""""))
            ?.value ?: return results

        val itemRegex = """\{"title":"([^"]*)".*?"snippet":"([\s\S]*?)"""".toRegex()
        for (match in itemRegex.findAll(searchMatch)) {
            val title = match.groupValues[1]
            var snippet = match.groupValues[2]

            // 移除 Wikipedia API 返回中的 HTML 标签
            snippet = snippet.replace(Regex("""<[^>]+>"""), "")
            snippet = snippet.replace(Regex("""\s+"""), " ")
            snippet = unescapeJson(snippet)

            val url = "https://en.wikipedia.org/wiki/${title.replace(" ", "_")}"

            results.add(SearchResult(
                title = unescapeJson(title),
                url = url,
                snippet = snippet.trim().take(200),
                source = name,
            ))
        }

        return results
    }
}

// ====================================================================
// 工具函数
// ====================================================================

private fun stripHtmlTags(text: String): String {
    return text.replace(Regex("""<[^>]+>"""), "")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&nbsp;", " ")
}

private fun unescapeJson(text: String): String {
    return text.replace("\\\"", "\"")
        .replace("\\n", " ")
        .replace("\\t", " ")
        .replace("\\/", "/")
        .replace("\\\\", "\\")
}
