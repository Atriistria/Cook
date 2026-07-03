package com.atride.cook.data

import ai.koog.http.client.KoogHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
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
        val response = ktorClient.get(baseUrl + path) {
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
        val response = ktorClient.post(baseUrl + path) {
            applyParamsAndHeaders(parameters, headers)
            contentType(ContentType.Application.Json)
            // 序列化 Request Body
            val bodyText = json.encodeToString(getSerializer(requestBodyType), requestBody)
            setBody(bodyText)
        }
        val responseText = response.bodyAsText()
        return json.decodeFromString(getSerializer(responseType), responseText)
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
        ktorClient.preparePost(baseUrl + path) {
            applyParamsAndHeaders(parameters, headers)
            contentType(ContentType.Application.Json)
            val bodyText = json.encodeToString(getSerializer(requestBodyType), requestBody)
            setBody(bodyText)
        }.execute { response ->
            val channel = response.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break
                if (line.startsWith("data: ")) {
                    val rawData = line.substringAfter("data: ").trim()
                    if (rawData == "[DONE]") break

                    if (dataFilter(rawData)) {
                        try {
                            // 调用 Koog 传入的解码函数和处理函数
                            val decoded = decodeStreamingResponse(rawData)
                            val processed = processStreamingChunk(decoded)
                            if (processed != null) {
                                emit(processed)
                            }
                        } catch (e: Exception) {
                            // 忽略部分格式不符的块，避免流异常中断
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