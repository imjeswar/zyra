

package zyra.echo.music.listentogether

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber

@Serializable
data class ListenTogetherServer(
    val name: String,
    val url: String,
    val location: String,
    val operator: String
)

object ListenTogetherServers {
    private const val SERVER_JSON_URL = "https://raw.githubusercontent.com/imjeswar/zyra/refs/heads/main/app/server.json"

    private val _servers = MutableStateFlow(
        listOf(
            ListenTogetherServer(
                name = "Zyra Server",
                url = "wss://jeswar-zyra-listen-together.hf.space/ws",
                location = "Global",
                operator = "Zyra"
            )
        )
    )
    
    val serversFlow: StateFlow<List<ListenTogetherServer>> = _servers

    val servers: List<ListenTogetherServer>
        get() = _servers.value

    private val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

    init {
        scope.launch {
            fetchAndWakeUpServers()
        }
    }

    fun wakeUpServers() {
        scope.launch {
            wakeUpServersInternal()
        }
    }

    private fun fetchAndWakeUpServers() {
        try {
            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            val request = okhttp3.Request.Builder().url(SERVER_JSON_URL).build()
            client.newCall(request).execute().use { response ->
                response.body?.string()?.let { jsonString ->
                    val jsonObject = Json.parseToJsonElement(jsonString).jsonObject
                    val name = jsonObject["name"]?.jsonPrimitive?.content ?: "Hugging Face Sync"
                    val url = jsonObject["serverUrl"]?.jsonPrimitive?.content ?: "wss://jeswar-zyra-listen-together.hf.space/ws"
                    val region = jsonObject["region"]?.jsonPrimitive?.content ?: "Global - VIVIDH"
                    
                    _servers.value = listOf(
                        ListenTogetherServer(
                            name = name,
                            url = url,
                            location = region,
                            operator = ""
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch server list")
        } finally {
            wakeUpServersInternal()
        }
    }

    private fun wakeUpServersInternal() {
        _servers.value.forEach { server ->
            pingHuggingFaceSpace(server.url)
        }
    }

    private fun pingHuggingFaceSpace(wsUrl: String) {
        try {
            val httpUrl = wsUrl
                .replace("wss://", "https://")
                .replace("ws://", "http://")
                .substringBefore("/ws")

            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val targetUrl = if (httpUrl.endsWith("/")) httpUrl else "$httpUrl/"
            val request = okhttp3.Request.Builder()
                .url(targetUrl)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                Timber.d("ListenTogether: Pinged Hugging Face space $targetUrl (Status: ${response.code})")
            }
        } catch (e: Exception) {
            Timber.e(e, "ListenTogether: Failed to ping Hugging Face space $wsUrl")
        }
    }

    val defaultServerUrl: String
        get() = servers.first().url

    fun findByUrl(url: String): ListenTogetherServer? = servers.firstOrNull { it.url == url }
}
