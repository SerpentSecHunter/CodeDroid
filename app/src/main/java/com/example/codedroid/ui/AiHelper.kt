package com.example.codedroid.ui

import android.content.Context
import com.example.codedroid.data.AiRouterConfig
import com.example.codedroid.data.ApiKeyVault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

// Fungsi utama kirim pesan — dipakai oleh AiPanelScreen DAN EditorScreen
suspend fun sendAiMessage(
    ctx       : Context,
    prov      : AiProviderInfo,
    srcIdx    : Int,
    userMsg   : String,
    messages  : MutableList<ChatMsg>,
    setLoading: (Boolean) -> Unit,
    setError  : (String) -> Unit,
    systemPrompt: String = "Kamu adalah asisten coding CodeDroid. Jawab dalam Bahasa Indonesia jika pertanyaan dalam Bahasa Indonesia. Berikan jawaban jelas dan kode yang bisa langsung dipakai."
) {
    val keyId    = "${prov.id}_$srcIdx"
    val apiKey   = ApiKeyVault.get(ctx, keyId)
    val endpoint = AiRouterConfig.sources[prov.id]?.getOrNull(srcIdx)

    if (apiKey.isBlank()) {
        setError("API key belum diatur — tap ✏️ untuk masukkan key"); return
    }
    if (endpoint == null) { setError("Sumber API tidak valid"); return }

    setError(""); setLoading(true)
    messages.add(ChatMsg("user", userMsg))

    try {
        val reply = withContext(Dispatchers.IO) {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .build()

            val body: String
            val request: Request

            when (endpoint.requestType) {

                "anthropic" -> {
                    val msgs = JSONArray()
                    messages.dropLast(1).takeLast(20).forEach {
                        msgs.put(JSONObject().put("role", it.role).put("content", it.content))
                    }
                    msgs.put(JSONObject().put("role", "user").put("content", userMsg))
                    body = JSONObject()
                        .put("model", endpoint.modelField)
                        .put("max_tokens", 2048)
                        .put("system", systemPrompt)
                        .put("messages", msgs)
                        .toString()
                    val b = Request.Builder()
                        .url(endpoint.url)
                        .post(body.toRequestBody("application/json".toMediaType()))
                        .addHeader(endpoint.authHeader, endpoint.authPrefix + apiKey)
                        .addHeader("Content-Type", "application/json")
                    endpoint.extraHeaders.forEach { (k, v) -> b.addHeader(k, v) }
                    request = b.build()
                    val res  = client.newCall(request).execute()
                    val json = JSONObject(res.body!!.string())
                    if (!res.isSuccessful) throw Exception(
                        json.optJSONObject("error")?.optString("message") ?: "Error ${res.code}")
                    json.getJSONArray("content").getJSONObject(0).getString("text")
                }

                "gemini" -> {
                    val contents = JSONArray()
                    messages.takeLast(20).forEach { m ->
                        contents.put(JSONObject()
                            .put("role", if (m.role == "assistant") "model" else "user")
                            .put("parts", JSONArray().put(JSONObject().put("text", m.content))))
                    }
                    contents.put(JSONObject().put("role", "user")
                        .put("parts", JSONArray().put(JSONObject().put("text", userMsg))))
                    body = JSONObject().put("contents", contents).toString()
                    val url = if ("key=" in endpoint.url) endpoint.url
                              else "${endpoint.url}?key=$apiKey"
                    request = Request.Builder().url(url)
                        .post(body.toRequestBody("application/json".toMediaType()))
                        .addHeader("Content-Type", "application/json")
                        .build()
                    val res  = client.newCall(request).execute()
                    val json = JSONObject(res.body!!.string())
                    if (!res.isSuccessful) throw Exception(
                        json.optJSONObject("error")?.optString("message") ?: "Error ${res.code}")
                    json.getJSONArray("candidates").getJSONObject(0)
                        .getJSONObject("content").getJSONArray("parts")
                        .getJSONObject(0).getString("text")
                }

                else -> {
                    // OpenAI-compatible
                    val msgs = JSONArray()
                    msgs.put(JSONObject().put("role","system").put("content", systemPrompt))
                    messages.takeLast(20).forEach {
                        msgs.put(JSONObject().put("role", it.role).put("content", it.content))
                    }
                    msgs.put(JSONObject().put("role", "user").put("content", userMsg))
                    body = JSONObject()
                        .put("model", endpoint.modelField)
                        .put("messages", msgs)
                        .put("max_tokens", 2048)
                        .toString()
                    val b = Request.Builder()
                        .url(endpoint.url)
                        .post(body.toRequestBody("application/json".toMediaType()))
                        .addHeader(endpoint.authHeader, endpoint.authPrefix + apiKey)
                        .addHeader("Content-Type", "application/json")
                    endpoint.extraHeaders.forEach { (k, v) -> b.addHeader(k, v) }
                    request = b.build()
                    val res  = client.newCall(request).execute()
                    val json = JSONObject(res.body!!.string())
                    if (!res.isSuccessful) throw Exception(
                        json.optJSONObject("error")?.optString("message") ?: "Error ${res.code}")
                    json.getJSONArray("choices").getJSONObject(0)
                        .getJSONObject("message").getString("content")
                }
            }
        }
        messages.add(ChatMsg("assistant", reply))

    } catch (e: Exception) {
        messages.removeLastOrNull()
        val hint = when {
            e.message?.contains("401") == true ||
            e.message?.contains("403") == true -> "❌ API key salah atau tidak valid"
            e.message?.contains("429") == true -> "⚠️ Rate limit, tunggu sebentar lalu coba lagi"
            e.message?.contains("timeout") == true ||
            e.message?.contains("connect") == true -> "📶 Koneksi internet bermasalah"
            e.message?.contains("404") == true -> "❌ Model tidak ditemukan. Coba ganti sumber API"
            else -> "❌ ${e.message?.take(100) ?: "Error tidak diketahui"}"
        }
        setError(hint)
    } finally {
        setLoading(false)
    }
}

// Cari provider aktif (yang sudah punya API key tersimpan)
fun findActiveProvider(ctx: Context): Pair<AiProviderInfo, Int>? {
    for (prov in AI_PROVIDERS) {
        val sources = AiRouterConfig.sources[prov.id] ?: continue
        for (i in sources.indices) {
            if (ApiKeyVault.hasKey(ctx, "${prov.id}_$i")) {
                return prov to i
            }
        }
    }
    return null
}

// Fungsi khusus Vibe Coding (murni mengubah kode tanpa chat history)
suspend fun vibeCodeRequest(
    ctx: Context,
    prov: AiProviderInfo,
    srcIdx: Int,
    currentCode: String,
    prompt: String,
    onLoading: (Boolean) -> Unit
): String {
    val keyId    = "${prov.id}_$srcIdx"
    val apiKey   = ApiKeyVault.get(ctx, keyId)
    val endpoint = AiRouterConfig.sources[prov.id]?.getOrNull(srcIdx)

    if (apiKey.isBlank()) throw Exception("API key belum diatur")
    if (endpoint == null) throw Exception("Sumber API tidak valid")

    onLoading(true)

    val systemPrompt = """
        Kamu adalah sistem Vibe Coding eksklusif terintegrasi di CodeDroid Android IDE.
        Aturan absolut:
        1. Kembalikan HANYA teks kode final yang sudah dimodifikasi sesuai instruksi.
        2. DILARANG menggunakan markdown code blocks (seperti ```kotlin atau ```).
        3. DILARANG memberikan penjelasan, komentar tambahan, basa-basi, atau teks apapun selain KODE itu sendiri.
        4. Jika diminta memperbaiki sebagian, kembalikan kodenya secara utuh (kecuali diminta sebaliknya).
    """.trimIndent()

    val combinedPrompt = """
        $prompt
        
        <kode_sekarang>
        $currentCode
        </kode_sekarang>
    """.trimIndent()

    try {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .build()

            val body: String
            val request: Request

            when (endpoint.requestType) {
                "anthropic" -> {
                    val msgs = JSONArray().put(JSONObject().put("role", "user").put("content", combinedPrompt))
                    body = JSONObject()
                        .put("model", endpoint.modelField)
                        .put("max_tokens", 2048) // Kurangi agar tidak limit di OpenRouter free
                        .put("system", systemPrompt)
                        .put("messages", msgs)
                        .toString()
                    val b = Request.Builder()
                        .url(endpoint.url)
                        .post(body.toRequestBody("application/json".toMediaType()))
                        .addHeader(endpoint.authHeader, endpoint.authPrefix + apiKey)
                        .addHeader("Content-Type", "application/json")
                    endpoint.extraHeaders.forEach { (k, v) -> b.addHeader(k, v) }
                    request = b.build()
                }
                "gemini" -> {
                    val contents = JSONArray().put(JSONObject().put("role", "user")
                        .put("parts", JSONArray().put(JSONObject().put("text", "$systemPrompt\n\n$combinedPrompt"))))
                    body = JSONObject().put("contents", contents).toString()
                    val url = if ("key=" in endpoint.url) endpoint.url else "${endpoint.url}?key=$apiKey"
                    request = Request.Builder().url(url)
                        .post(body.toRequestBody("application/json".toMediaType()))
                        .addHeader("Content-Type", "application/json")
                        .build()
                }
                else -> { // OpenAI
                    val msgs = JSONArray()
                        .put(JSONObject().put("role", "system").put("content", systemPrompt))
                        .put(JSONObject().put("role", "user").put("content", combinedPrompt))
                    body = JSONObject()
                        .put("model", endpoint.modelField)
                        .put("messages", msgs)
                        .put("max_tokens", 2048) // Kurangi agar tidak limit di OpenRouter free
                        .toString()
                    val b = Request.Builder()
                        .url(endpoint.url)
                        .post(body.toRequestBody("application/json".toMediaType()))
                        .addHeader(endpoint.authHeader, endpoint.authPrefix + apiKey)
                        .addHeader("Content-Type", "application/json")
                    endpoint.extraHeaders.forEach { (k, v) -> b.addHeader(k, v) }
                    request = b.build()
                }
            }

            val res = client.newCall(request).execute()
            val resBody = res.body?.string() ?: ""
            val json = JSONObject(resBody)

            if (!res.isSuccessful) {
                val errMsg = json.optJSONObject("error")?.optString("message") ?: "HTTP ${res.code}"
                throw Exception("Gagal ({$res.code}): $errMsg")
            }

            var resultCode = when (endpoint.requestType) {
                "anthropic" -> json.getJSONArray("content").getJSONObject(0).getString("text")
                "gemini" -> json.getJSONArray("candidates").getJSONObject(0)
                        .getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
                else -> json.getJSONArray("choices").getJSONObject(0)
                        .getJSONObject("message").getString("content")
            }

            // Fallback safety: hilangkan markdown balasan nakal (```) jika terdeteksi
            if (resultCode.startsWith("```")) {
                val firstNewLine = resultCode.indexOf('\n')
                if (firstNewLine != -1) {
                    resultCode = resultCode.substring(firstNewLine + 1)
                }
            }
            if (resultCode.endsWith("```")) {
                resultCode = resultCode.dropLast(3)
            }
            if (resultCode.endsWith("```\n")) {
                resultCode = resultCode.dropLast(4)
            }
            
            resultCode.trim()
        }
    } finally {
        onLoading(false)
    }
}