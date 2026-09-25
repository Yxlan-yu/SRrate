package com.yxlanyu.refreshrate.util

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val tagName: String,
    val releaseNotes: String,
    val apkUrl: String,
    val apkSizeBytes: Long,
)

object UpdateChecker {

    fun currentVersionName(context: Context): String =
        try {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
        } catch (t: Throwable) {
            ""
        }

    private const val TAG = "SRrate_Upd"
    private const val RELEASES_API = "https://api.github.com/repos/Yxlan-yu/SRrate/releases"
    private const val USER_AGENT = "SRrate-UpdateChecker"
    private const val TIMEOUT_MS = 8_000

    private const val PREFS_NAME = "s"
    private const val KEY_ETAG = "update_etag"
    private const val KEY_BODY = "update_cached_body"
    const val KEY_CHANNEL = "update_channel"

    private val API_BASES = listOf(
        RELEASES_API,
        "https://gh-proxy.com/$RELEASES_API",
        "https://gh-proxy.org/$RELEASES_API",
    )

    fun updateChannel(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_CHANNEL, null) ?: "stable"

    fun isBetaChannel(context: Context): Boolean = updateChannel(context) == "beta"

    fun fetchLatestRelease(context: Context): UpdateInfo? {
        val channel = updateChannel(context)
        val beta = channel == "beta"
        val suffix = if (beta) "?per_page=8" else "/latest"
        for (base in API_BASES) {
            val body = fetchBody(context, "$base$suffix", channel) ?: continue
            val result = if (beta) parseReleaseList(body) else parseRelease(body)
            if (result != null) return result
        }
        val cached = cachedBody(context, channel) ?: run {
            Log.w(TAG, "fetchLatestRelease: no cached body")
            return null
        }
        Log.i(TAG, "fetchLatestRelease: using cached body, len=${cached.length} channel=$channel")
        return if (beta) parseReleaseList(cached) else parseRelease(cached)
    }

    private fun fetchBody(context: Context, url: String, channel: String): String? = try {
        Log.i(TAG, "fetchBody start: $url at ${System.currentTimeMillis()}")
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        connection.setRequestProperty("User-Agent", USER_AGENT)
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        val etagKey = "$KEY_ETAG$channel"
        val bodyKey = "$KEY_BODY$channel"
        val etag = prefs(context).getString(etagKey, null)
        if (etag != null) connection.setRequestProperty("If-None-Match", etag)
        Log.i(TAG, "fetchBody: connecting $url")
        val code = connection.responseCode
        Log.i(TAG, "fetchBody: got response $code for $url at ${System.currentTimeMillis()}")
        val body = when {
            code == 304 -> cachedBody(context, channel)
            code in 200..299 -> connection.inputStream.bufferedReader().use { it.readText() }
            else -> null
        }
        if (code in 200..299 && body != null) {
            val editor = prefs(context).edit()
            val newEtag = connection.getHeaderField("ETag")
            if (newEtag != null) editor.putString(etagKey, newEtag)
            editor.putString(bodyKey, body).apply()
        }
        connection.disconnect()
        Log.i(TAG, "fetchBody: done, bodyLen=${body?.length ?: -1}")
        body
    } catch (t: Throwable) {
        Log.e(TAG, "fetchBody error for $url: ${t.javaClass.name}: ${t.message}", t)
        null
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun cachedBody(context: Context, channel: String): String? =
        prefs(context).getString("$KEY_BODY$channel", null)?.takeIf { it.isNotEmpty() }

    private fun parseReleaseJson(json: JSONObject): UpdateInfo? {
        if (json.optBoolean("draft", false)) return null
        val tag = json.optString("tag_name", "").takeIf { it.isNotEmpty() } ?: return null
        val notes = json.optString("body", "")
        var apkUrl = ""
        var apkSize = 0L
        val assets = json.optJSONArray("assets")
        if (assets != null) {
            for (i in 0 until assets.length()) {
                val asset = assets.optJSONObject(i) ?: continue
                if (asset.optString("name", "").endsWith(".apk", ignoreCase = true)) {
                    apkUrl = asset.optString("browser_download_url", "")
                    apkSize = asset.optLong("size", 0L)
                    break
                }
            }
        }
        if (apkUrl.isEmpty()) return null
        return UpdateInfo(tag, notes, apkUrl, apkSize)
    }

    private fun parseRelease(body: String): UpdateInfo? = try {
        parseReleaseJson(JSONObject(body))
    } catch (t: Throwable) {
        null
    }

    private fun parseReleaseList(body: String): UpdateInfo? = try {
        val arr = JSONArray(body)
        for (i in 0 until arr.length()) {
            val item = arr.optJSONObject(i) ?: continue
            val r = parseReleaseJson(item)
            if (r != null) return r
        }
        null
    } catch (t: Throwable) {
        null
    }

    fun isNewerThan(localVersion: String, remoteTag: String): Boolean {
        val a = versionParts(localVersion)
        val b = versionParts(remoteTag)
        if (a.isEmpty() || b.isEmpty()) return false
        val size = maxOf(a.size, b.size)
        for (i in 0 until size) {
            val left = if (i < a.size) a[i] else 0
            val right = if (i < b.size) b[i] else 0
            if (left == right) continue
            return left < right
        }
        return false
    }

    private fun versionParts(version: String): List<Int> =
        Regex("\\d+").findAll(version).map { it.value.toIntOrNull() ?: 0 }.toList()

    fun downloadApk(url: String, destFile: File, onProgress: ((Float) -> Unit)? = null): Boolean {
        val sources = listOf(
            url,
            "https://gh-proxy.com/$url",
            "https://gh-proxy.org/$url",
        )
        for (source in sources) {
            if (downloadFrom(source, destFile, onProgress)) return true
        }
        return false
    }

    private fun downloadFrom(url: String, destFile: File, onProgress: ((Float) -> Unit)? = null): Boolean {
        var connection: HttpURLConnection? = null
        try {
            connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 20_000
            connection.readTimeout = 30_000
            connection.setRequestProperty("User-Agent", USER_AGENT)
            val code = connection.responseCode
            if (code !in 200..299) return false
            val total = connection.contentLengthLong
            destFile.parentFile?.mkdirs()
            val input = connection.inputStream
            val output = destFile.outputStream()
            val buffer = ByteArray(8192)
            var read: Int
            var done = 0L
            try {
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                    done += read
                    if (total > 0) onProgress?.invoke(done.toFloat() / total.toFloat())
                }
                output.flush()
            } finally {
                output.close()
                input.close()
            }
            return done > 0
        } catch (t: Throwable) {
            try {
                if (destFile.exists()) destFile.delete()
            } catch (ignored: Throwable) {
            }
            return false
        } finally {
            connection?.disconnect()
        }
    }
}