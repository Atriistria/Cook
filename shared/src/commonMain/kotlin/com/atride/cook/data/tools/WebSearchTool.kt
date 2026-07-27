package com.atride.cook.data.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.serialization.typeToken
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import kotlinx.serialization.Serializable

/**
 * 网页搜索工具。内部聚合多个搜索引擎，返回综合结果。
 *
 * 当前引擎：
 * - DuckDuckGo（HTML 搜索，无需 API Key）
 * - Wikipedia（免费结构化查询）
 *
 */
object WebSearchTool : SimpleTool<WebSearchTool.Args>(
    argsType = typeToken<Args>(),
    name = "web_search",
    description = "搜索互联网获取最新信息。支持多个搜索源（DuckDuckGo、Wikipedia 等），返回综合结果。适用于查询新闻、百科知识、实时信息等场景。",
) {

    /** 在此注册所有可用的搜索引擎 */
    private val engines: List<SearchEngine> = listOf(
        DuckDuckGo,
        Wikipedia,
    )

    private val httpClient = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 10_000
            connectTimeoutMillis = 5_000
        }
    }

    @Serializable
    data class Args(
        @property:LLMDescription("搜索关键词，尽量具体和精确")
        val query: String,

        @property:LLMDescription("可选的搜索源限制，如 'DuckDuckGo' 或 'Wikipedia'。为空则搜索所有引擎。")
        val source: String = "",
    )

    override suspend fun execute(args: Args): String {
        val selectedEngines = if (args.source.isBlank()) {
            engines
        } else {
            engines.filter { it.name.equals(args.source, ignoreCase = true) }
                .ifEmpty {
                    return "未找到搜索源 '${args.source}'，可用源：${engines.joinToString(", ") { it.name }}"
                }
        }

        val lines = mutableListOf<String>()
        lines.add("# 搜索结果：${args.query}")
        lines.add("")

        for (engine in selectedEngines) {
            lines.add("## ${engine.name}")
            val results = engine.search(httpClient, args.query)

            if (results.isEmpty()) {
                lines.add("  无结果")
                continue
            }

            for ((i, result) in results.withIndex()) {
                if (result.title.startsWith("[")) {
                    // 错误信息
                    lines.add("  ⚠ ${result.snippet}")
                    continue
                }
                lines.add("  ${i + 1}. ${result.title}")
                lines.add("     ${result.url}")
                if (result.snippet.isNotBlank()) {
                    lines.add("     ${result.snippet}")
                }
            }
            lines.add("")
        }

        return lines.joinToString("\n")
    }
}
