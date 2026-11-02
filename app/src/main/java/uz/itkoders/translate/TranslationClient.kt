package uz.itkoders.translate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import org.json.JSONObject

object TranslationClient {
    private val client = OkHttpClient()

    suspend fun toUzbek(text: String): String = withContext(Dispatchers.IO) {
        val q = URLEncoder.encode(text.take(450), "UTF-8")
        val url = "https://api.mymemory.translated.net/get?q=$q&langpair=autodetect%7Cuz"
        val request = Request.Builder().url(url).header("Accept", "application/json").build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Tarjima serveri javob bermadi: HTTP ${response.code}")
            val json = JSONObject(response.body?.string().orEmpty())
            val status = json.optInt("responseStatus", 200)
            if (status != 200) error(json.optString("responseDetails", "Tarjima amalga oshmadi"))
            json.getJSONObject("responseData").getString("translatedText")
        }
    }
}
