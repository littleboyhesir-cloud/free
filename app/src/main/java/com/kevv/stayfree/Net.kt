package com.kevv.stayfree

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class WikiInfo(val title: String, val extract: String, val url: String?)

object Net {
    private fun get(url: String): String {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 8000
        c.readTimeout = 8000
        c.setRequestProperty("User-Agent", "StayFree/1.0 (Android app)")
        c.setRequestProperty("Accept", "application/json")
        return try {
            c.inputStream.bufferedReader().use { it.readText() }
        } finally {
            c.disconnect()
        }
    }

    /** Random motivational quote from ZenQuotes (free, no key). */
    suspend fun quote(): Pair<String, String>? = withContext(Dispatchers.IO) {
        runCatching {
            val o = JSONArray(get("https://zenquotes.io/api/random")).getJSONObject(0)
            val q = o.getString("q")
            val a = o.getString("a")
            if (a.contains("zenquotes", ignoreCase = true)) null else q to a
        }.getOrNull()
    }

    /** Article summary from Wikipedia's REST API (free, no key). */
    suspend fun wiki(title: String): WikiInfo? = withContext(Dispatchers.IO) {
        runCatching {
            val enc = URLEncoder.encode(title.replace(' ', '_'), "UTF-8")
            val o = JSONObject(get("https://en.wikipedia.org/api/rest_v1/page/summary/$enc"))
            WikiInfo(
                o.getString("title"),
                o.getString("extract"),
                o.optJSONObject("content_urls")?.optJSONObject("mobile")?.optString("page")
            )
        }.getOrNull()
    }
}
