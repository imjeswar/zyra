/*
 * EchoMusic (2026)
 * © Chartreux Westia — github.com/koiverse
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package zyra.echo.music.spotify

import zyra.echo.music.spotify.models.SpotifyInternalToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.HttpURLConnection
import java.net.URL
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.floor

/**
 * Handles Spotify authentication using the web player's internal token endpoint.
 * Uses sp_dc cookies (extracted from WebView login) to obtain access tokens
 * without requiring a Spotify Developer Client ID.
 *
 * Token acquisition requires a TOTP (Time-based One-Time Password) generated
 * from a shared secret that Spotify rotates periodically. The secret and its
 * version are fetched from a community-maintained GitHub Gist.
 *
 * Reference: https://github.com/sonic-liberation/spotube-plugin-spotify
 */
object SpotifyAuth {
    private const val TOKEN_URL = "https://open.spotify.com/api/token"
    private const val SERVER_TIME_URL = "https://open.spotify.com/api/server-time"
    private const val NUANCE_GIST_URL =
        "https://gist.githubusercontent.com/sonic-liberation/22ed9c6ba463899e933427f7de1f0eef/raw/"
    private const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"

    const val LOGIN_URL = "https://accounts.spotify.com/login?continue=https%3A%2F%2Fopen.spotify.com%2F"

    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    @Serializable
    private data class Nuance(val s: String, val v: Int)

    @Serializable
    private data class ServerTimeResponse(val serverTime: Long)

    private val client = OkHttpClient.Builder()
        .connectTimeout(java.time.Duration.ofSeconds(15))
        .readTimeout(java.time.Duration.ofSeconds(15))
        .followRedirects(true)
        .build()

    suspend fun fetchGuestToken(): Result<SpotifyInternalToken> = runCatching {
        val baseUrl = "https://open.spotify.com/"
        val guestUrl = "https://open.spotify.com/get_access_token?reason=transport&productType=web_player"
        
        android.util.Log.d("SPOTIFY_DIAG", "[fetchGuestToken] Step 1: Getting cookies from $baseUrl")
        val cookies = withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(baseUrl)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                .build()
                
            client.newCall(request).execute().use { response ->
                android.util.Log.d("SPOTIFY_DIAG", "[fetchGuestToken] Base URL Response: ${response.code}")
                android.util.Log.d("SPOTIFY_DIAG", "[fetchGuestToken] Base URL Headers: ${response.headers.toMultimap()}")
                val cookieHeaders = response.headers("Set-Cookie")
                cookieHeaders.mapNotNull { header ->
                    header.split(";").firstOrNull()?.trim()
                }.joinToString("; ")
            }
        }

        android.util.Log.d("SPOTIFY_DIAG", "[fetchGuestToken] Step 2: Requesting guest token from $guestUrl")
        android.util.Log.d("SPOTIFY_DIAG", "[fetchGuestToken] Extracted cookies: $cookies")

        val body = withContext(Dispatchers.IO) {
            val headers = HashMap<String, String>()
            if (cookies.isNotEmpty()) {
                headers["Cookie"] = cookies
            }
            headers["Referer"] = "https://open.spotify.com/"
            
            android.util.Log.d("SPOTIFY_DIAG", "[fetchGuestToken] Sending headers: $headers")
            httpGet(guestUrl, headers)
        }
        android.util.Log.d("SPOTIFY_DIAG", "[fetchGuestToken] Success! Response body: $body")
        json.decodeFromString<SpotifyInternalToken>(body)
    }.onFailure { error ->
        android.util.Log.e("SPOTIFY_DIAG", "[fetchGuestToken] FAILED with error: ", error)
        if (error is Spotify.SpotifyException) {
            android.util.Log.e("SPOTIFY_DIAG", "[fetchGuestToken] HTTP Exception details: status=${error.statusCode}, body=${error.message}")
        }
    }

    /**
     * Fetches an internal web-player access token using session cookies and TOTP.
     *
     * 1. Fetches the TOTP secret from the community Gist
     * 2. Gets the server time from Spotify
     * 3. Generates a 6-digit TOTP (SHA1, 30s interval)
     * 4. Calls /api/token with the TOTP and sp_dc cookie
     */
    suspend fun fetchAccessToken(
        spDc: String,
        spKey: String = "",
    ): Result<SpotifyInternalToken> = runCatching {
        val nuance = fetchNuance()
        val serverTimeSec = fetchServerTime()
        val totp = generateTotp(nuance.s, serverTimeSec)

        val tokenUrl = buildString {
            append(TOKEN_URL)
            append("?reason=transport")
            append("&productType=web-player")
            append("&totp=$totp")
            append("&totpServer=$totp")
            append("&totpVer=${nuance.v}")
        }

        val cookieHeader = buildString {
            append("sp_dc=$spDc")
            if (spKey.isNotEmpty()) {
                append("; sp_key=$spKey")
            }
        }

        val body = withContext(Dispatchers.IO) {
            httpGet(tokenUrl, mapOf("Cookie" to cookieHeader))
        }

        val token = json.decodeFromString<SpotifyInternalToken>(body)

        if (token.isAnonymous || token.accessToken.isBlank()) {
            throw Spotify.SpotifyException(
                401,
                "Received anonymous token — sp_dc cookie is invalid or expired",
            )
        }

        token
    }

    private suspend fun fetchNuance(): Nuance = withContext(Dispatchers.IO) {
        val body = try {
            httpGet(NUANCE_GIST_URL, emptyMap())
        } catch (e: Exception) {
            throw Spotify.SpotifyException(
                503,
                "Failed to fetch TOTP secret from gist: ${e.message}",
            )
        }
        val nuances = json.decodeFromString<List<Nuance>>(body)
        nuances.maxByOrNull { it.v }
            ?: throw Spotify.SpotifyException(500, "No nuance data found in gist")
    }

    private suspend fun fetchServerTime(): Long = withContext(Dispatchers.IO) {
        val body = try {
            httpGet(SERVER_TIME_URL, emptyMap())
        } catch (e: Exception) {
            throw Spotify.SpotifyException(
                503,
                "Failed to fetch Spotify server time: ${e.message}",
            )
        }
        val response = json.decodeFromString<ServerTimeResponse>(body)
        response.serverTime
    }

    /**
     * Generates a 6-digit TOTP using HMAC-SHA1 (RFC 6238).
     * @param secret Base32-encoded shared secret
     * @param serverTimeSec Spotify server time in seconds since epoch
     */
    private fun generateTotp(secret: String, serverTimeSec: Long): String {
        val key = base32Decode(secret)
        val interval = 30L
        val timeStep = floor(serverTimeSec.toDouble() / interval).toLong()

        val timeBytes = ByteArray(8)
        var value = timeStep
        for (i in 7 downTo 0) {
            timeBytes[i] = (value and 0xFF).toByte()
            value = value shr 8
        }

        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key, "HmacSHA1"))
        val hash = mac.doFinal(timeBytes)

        val offset = hash[hash.size - 1].toInt() and 0x0F
        val code = ((hash[offset].toInt() and 0x7F) shl 24) or
            ((hash[offset + 1].toInt() and 0xFF) shl 16) or
            ((hash[offset + 2].toInt() and 0xFF) shl 8) or
            (hash[offset + 3].toInt() and 0xFF)

        val otp = code % 1_000_000
        return otp.toString().padStart(6, '0')
    }

    private fun base32Decode(input: String): ByteArray {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val cleaned = input.uppercase().replace("=", "")

        val output = mutableListOf<Byte>()
        var buffer = 0
        var bitsLeft = 0

        for (c in cleaned) {
            val value = alphabet.indexOf(c)
            if (value < 0) continue
            buffer = (buffer shl 5) or value
            bitsLeft += 5
            if (bitsLeft >= 8) {
                bitsLeft -= 8
                output.add(((buffer shr bitsLeft) and 0xFF).toByte())
            }
        }

        return output.toByteArray()
    }

    private fun httpGet(urlString: String, extraHeaders: Map<String, String>): String {
        val requestBuilder = Request.Builder()
            .url(urlString)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json, text/plain, */*")
            .header("Accept-Language", "en")
            
        for ((key, value) in extraHeaders) {
            requestBuilder.header(key, value)
        }

        client.newCall(requestBuilder.build()).execute().use { response ->
            val responseCode = response.code
            val body = response.body?.string().orEmpty()
            if (responseCode !in 200..299) {
                throw Spotify.SpotifyException(
                    responseCode,
                    "HTTP $responseCode: $body",
                )
            }
            return body
        }
    }


}
