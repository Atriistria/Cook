package com.atride.cook.data

import ai.koog.http.client.KoogHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import io.ktor.http.content.TextContent


@OptIn(InternalSerializationApi::class)
class KtorKoogHttpClient(
    override val clientName: String,
    private val baseUrl: String,
    private val defaultHeaders: Map<String, String>,
    private val defaultParams: Map<String, String>,
    private val requestTimeout: Long,
    private val connectTimeout: Long,
    private val socketTimeout: Long,
    private val json: Json

): KoogHttpClient {

    private val ktorClient = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = requestTimeout
            connectTimeoutMillis = connectTimeout
            socketTimeoutMillis = socketTimeout
        }

        install(Logging) {
            level = LogLevel.ALL // 监控包括 Request/Response Body、Headers 在内的所有网络数据
            logger = Logger.DEFAULT // 输出到系统控制台
        }
    }

    // 2. 辅助函数：根据 KClass 动态获取 kotlinx.serialization 的序列化器
    private fun <T : Any> getSerializer(kClass: KClass<T>): KSerializer<T> {
        return kClass.serializer()
    }

    private fun HttpRequestBuilder.applyParamsAndHeaders(
        callParams: Map<String, String>,
        callHeaders: Map<String, String>
    ) {
        (defaultParams + callParams).forEach { (key, value) ->
            parameter(key, value)
        }
        (defaultHeaders + callHeaders).forEach { (key, value) ->
            header(key, value)
        }
    }

    override suspend fun <R : Any> get(
        path: String,
        responseType: KClass<R>,
        parameters: Map<String, String>,
        headers: Map<String, String>
    ): R {
        val fullUrl = resolveUrl(baseUrl, path)
        println("🌟 最终调用的 API 地址是: $fullUrl")

        val response = ktorClient.get(fullUrl) {
            applyParamsAndHeaders(parameters, headers)
        }
        val responseText = response.bodyAsText()
        return json.decodeFromString(getSerializer(responseType), responseText)
    }

    override suspend fun <T : Any, R : Any> post(
        path: String,
        requestBody: T,
        requestBodyType: KClass<T>,
        responseType: KClass<R>,
        parameters: Map<String, String>,
        headers: Map<String, String>
    ): R {
        val fullUrl = resolveUrl(baseUrl, path)
        println("🌟 最终调用的 API 地址是: $fullUrl")

        val response = ktorClient.post(fullUrl) {
            applyParamsAndHeaders(parameters, headers)

            // 🌟 核心修改：智能解析请求体，防止对已经序列化好的 String 进行二次损坏
            val bodyText = resolveBodyText(requestBody, requestBodyType)

            setBody(TextContent(bodyText, ContentType.Application.Json))
        }

        println("Koog HTTP Status: ${response.status.value}")
        val responseText = response.bodyAsText()
        println("Koog HTTP Response: $responseText")
        if (response.status.value !in 200..299) {
            throw Exception("HTTP request failed with status ${response.status.value}: $responseText")
        }
        // 🌟 核心修改：如果是 String 类型，直接返回原始报文，防止 kotlinx-serialization 误判
        return if (responseType == String::class) {
            responseText as R
        } else {
            json.decodeFromString(getSerializer(responseType), responseText)
        }
    }

    override fun <T : Any, R : Any, O : Any> sse(
        path: String,
        requestBody: T,
        requestBodyType: KClass<T>,
        dataFilter: (String?) -> Boolean,
        decodeStreamingResponse: (String) -> R,
        processStreamingChunk: (R) -> O?,
        parameters: Map<String, String>,
        headers: Map<String, String>
    ): Flow<O> = flow {
        val fullUrl = resolveUrl(baseUrl, path)
        println("🌟 开始建立 SSE 流式连接: $fullUrl")

        val bodyText = resolveBodyText(requestBody, requestBodyType)
        println("🌟 SSE request body: $bodyText")

        // 1. 使用 preparePost 建立延迟下载连接
        ktorClient.preparePost(fullUrl) {
            applyParamsAndHeaders(parameters, headers)
            setBody(TextContent(bodyText, ContentType.Application.Json))
        }.execute { response ->
            println("Koog SSE HTTP Status: ${response.status.value}")

            if (response.status.value != 200) {
                val errorBody = response.bodyAsText()
                println("Koog SSE Error Body: $errorBody")
                throw Exception("SSE Request failed with status ${response.status.value}: $errorBody")
            }

            val channel = response.bodyAsChannel()

            // 2. 循环读取底层通道，直到通道被服务器关闭
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break
                val trimmed = line.trim()

                // 3. 筛选出标准的 SSE 数据行
                if (trimmed.startsWith("data: ")) {
                    val rawData = trimmed.substringAfter("data: ").trim()
                    println(rawData)
                    if (rawData == "[DONE]") {
                        println("🌟 SSE 数据流正常结束")
                        break
                    }

                    if (dataFilter(rawData)) {
                        try {
                            val decoded = decodeStreamingResponse(rawData)
                            val processed = processStreamingChunk(decoded)
                            if (processed != null) {
                                emit(processed)
                            }
                        } catch (e: Exception) {
                            // 优雅地忽略单个异常分片
                        }
                    }
                }
            }
        }
    }

    override fun <T : Any> lines(
        path: String,
        requestBody: T,
        requestBodyType: KClass<T>,
        parameters: Map<String, String>,
        headers: Map<String, String>
    ): Flow<String> = flow {
        ktorClient.preparePost(baseUrl + path) {
            applyParamsAndHeaders(parameters, headers)
            contentType(ContentType.Application.Json)
            val bodyText = json.encodeToString(getSerializer(requestBodyType), requestBody)
            setBody(bodyText)
        }.execute { response ->
            val channel = response.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break
                if (line.isNotBlank()) {
                    emit(line)
                }
            }
        }
    }

    override fun close() {
        ktorClient.close()
    }

    private fun resolveUrl(baseUrl: String, path: String): String {
        val cleanBaseUrl = baseUrl.removeSuffix("/")
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        return "$cleanBaseUrl$cleanPath"
    }

    private fun <T : Any> resolveBodyText(body: T, kClass: KClass<T>): String {
        return if (body is String) {
            body // 🌟 如果已经是 String，直接返回，不再二次序列化
        } else {
            json.encodeToString(getSerializer(kClass), body) // 🌟 否则，才进行序列化
        }
    }
}

class KtorKoogHttpClientFactory : KoogHttpClient.Factory {
    override fun create(
        clientName: String,
        baseUrl: String,
        headers: Map<String, String>,
        queryParameters: Map<String, String>,
        requestTimeoutMillis: Long,
        connectTimeoutMillis: Long,
        socketTimeoutMillis: Long,
        json: Json
    ): KoogHttpClient {
        return KtorKoogHttpClient(
            clientName = clientName,
            baseUrl = baseUrl,
            defaultHeaders = headers,
            defaultParams = queryParameters,
            requestTimeout = requestTimeoutMillis,
            connectTimeout = connectTimeoutMillis,
            socketTimeout = socketTimeoutMillis,
            json = json
        )
    }
}