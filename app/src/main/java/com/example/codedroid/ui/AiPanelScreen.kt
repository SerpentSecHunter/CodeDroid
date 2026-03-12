package com.example.codedroid.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codedroid.data.ApiKeyVault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class AiProviderInfo(
    val id      : String,
    val name    : String,
    val color   : Color,
    val model   : String,
    val endpoint: String,
    val hint    : String
)

data class ChatMessage(val role: String, val content: String)

val aiProviders = listOf(
    AiProviderInfo("openai",   "ChatGPT",  Color(0xFF10A37F), "gpt-4o-mini",
        "https://api.openai.com/v1/chat/completions",
        "Dapatkan API key di platform.openai.com"),
    AiProviderInfo("anthropic","Claude",   Color(0xFFD97706), "claude-haiku-4-5-20251001",
        "https://api.anthropic.com/v1/messages",
        "Dapatkan API key di console.anthropic.com"),
    AiProviderInfo("deepseek", "DeepSeek", Color(0xFF2563EB), "deepseek-chat",
        "https://api.deepseek.com/chat/completions",
        "Dapatkan API key di platform.deepseek.com"),
    AiProviderInfo("gemini",   "Gemini",   Color(0xFF4285F4), "gemini-2.0-flash",
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent",
        "Dapatkan API key di aistudio.google.com"),
    AiProviderInfo("groq",     "Grok",     Color(0xFF6366F1), "llama-3.3-70b-versatile",
        "https://api.groq.com/openai/v1/chat/completions",
        "Dapatkan API key di console.groq.com"),
    AiProviderInfo("openrouter","Kimi",    Color(0xFF8B5CF6), "moonshot/moonshot-v1-8k",
        "https://openrouter.ai/api/v1/chat/completions",
        "Gunakan OpenRouter untuk akses Kimi — openrouter.ai"),
    AiProviderInfo("qwen",     "Qwen",     Color(0xFF7C3AED), "qwen-turbo",
        "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions",
        "Dapatkan API key di dashscope.aliyuncs.com"),
    AiProviderInfo("manus",    "Manus",    Color(0xFFEC4899), "gpt-4o",
        "https://api.openai.com/v1/chat/completions",
        "Gunakan OpenAI API key untuk Manus"),
)

@Composable
fun AiPanelScreen() {
    val context   = LocalContext.current
    val scope     = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var selectedProvider by remember { mutableStateOf(aiProviders[0]) }
    var apiKey      by remember(selectedProvider) {
        mutableStateOf(ApiKeyVault.get(context, selectedProvider.id))
    }
    var showKey     by remember { mutableStateOf(false) }
    var showKeyInput by remember { mutableStateOf(false) }
    var userInput   by remember { mutableStateOf("") }
    var isLoading   by remember { mutableStateOf(false) }
    val messages    = remember { mutableStateListOf<ChatMessage>() }
    var errorMsg    by remember { mutableStateOf("") }
    val hasKey      = ApiKeyVault.hasKey(context, selectedProvider.id)

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(Modifier.fillMaxSize()) {
        // ── Header ────────────────────────────────────────────────
        Surface(tonalElevation = 2.dp) {
            Column(Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Provider selector
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(aiProviders.size) { i ->
                        val p = aiProviders[i]
                        val isSelected = selectedProvider.id == p.id
                        val hasK = ApiKeyVault.hasKey(context, p.id)
                        FilterChip(
                            selected = isSelected,
                            onClick  = { selectedProvider = p; errorMsg = "" },
                            label    = { Text(p.name, fontSize = 11.sp) },
                            leadingIcon = {
                                if (hasK) Icon(Icons.Rounded.CheckCircle, null,
                                    Modifier.size(12.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = p.color.copy(0.15f),
                                selectedLabelColor     = p.color
                            )
                        )
                    }
                }
                // API Key row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showKeyInput) {
                        OutlinedTextField(
                            value         = apiKey,
                            onValueChange = { apiKey = it },
                            label         = { Text("API Key ${selectedProvider.name}", fontSize = 11.sp) },
                            singleLine    = true,
                            modifier      = Modifier.weight(1f),
                            shape         = RoundedCornerShape(8.dp),
                            visualTransformation = if (showKey) VisualTransformation.None
                                                   else PasswordVisualTransformation(),
                            trailingIcon  = {
                                IconButton(onClick = { showKey = !showKey }) {
                                    Icon(if (showKey) Icons.Rounded.Visibility
                                         else Icons.Rounded.VisibilityOff, null, Modifier.size(16.dp))
                                }
                            }
                        )
                        IconButton(onClick = {
                            ApiKeyVault.save(context, selectedProvider.id, apiKey)
                            showKeyInput = false
                        }) { Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary) }
                    } else {
                        Surface(
                            color  = if (hasKey) MaterialTheme.colorScheme.secondary.copy(0.1f)
                                     else MaterialTheme.colorScheme.error.copy(0.08f),
                            shape  = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (hasKey) Icons.Rounded.Lock else Icons.Rounded.Key,
                                    null, Modifier.size(14.dp),
                                    if (hasKey) MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.error
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (hasKey) "API Key tersimpan ✓"
                                    else selectedProvider.hint,
                                    fontSize = 11.sp,
                                    color    = if (hasKey) MaterialTheme.colorScheme.secondary
                                               else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        IconButton(onClick = { showKeyInput = true; apiKey = ApiKeyVault.get(context, selectedProvider.id) }) {
                            Icon(Icons.Rounded.Edit, null, Modifier.size(18.dp))
                        }
                    }
                    if (hasKey && messages.isNotEmpty()) {
                        IconButton(onClick = { messages.clear() }) {
                            Icon(Icons.Rounded.DeleteSweep, null, Modifier.size(18.dp))
                        }
                    }
                }
                if (errorMsg.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(errorMsg, fontSize = 11.sp, color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                    }
                }
            }
        }
        HorizontalDivider()

        // ── Chat area ─────────────────────────────────────────────
        if (messages.isEmpty() && !isLoading) {
            Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.SmartToy, null,
                        tint = selectedProvider.color.copy(0.3f), modifier = Modifier.size(48.dp))
                    Text("Chat dengan ${selectedProvider.name}",
                        color = MaterialTheme.colorScheme.onSurface.copy(0.4f), fontSize = 14.sp)
                    if (!hasKey) {
                        Text("Masukkan API key untuk mulai",
                            color = MaterialTheme.colorScheme.onSurface.copy(0.3f), fontSize = 12.sp)
                    }
                    // Suggestion chips
                    if (hasKey) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            listOf(
                                "Bantu saya debug kode ini",
                                "Jelaskan konsep OOP",
                                "Buatkan fungsi sorting Python"
                            ).forEach { suggestion ->
                                SuggestionChip(
                                    onClick = { userInput = suggestion },
                                    label   = { Text(suggestion, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                state    = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    val isUser = msg.role == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        if (!isUser) {
                            Surface(
                                color  = selectedProvider.color.copy(0.15f),
                                shape  = RoundedCornerShape(50),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(Modifier.fillMaxSize(), Alignment.Center) {
                                    Text(selectedProvider.name.first().toString(),
                                        fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                        color = selectedProvider.color)
                                }
                            }
                            Spacer(Modifier.width(6.dp))
                        }
                        Surface(
                            color  = if (isUser) MaterialTheme.colorScheme.primary.copy(0.15f)
                                     else MaterialTheme.colorScheme.surfaceVariant,
                            shape  = RoundedCornerShape(
                                topStart    = if (isUser) 16.dp else 4.dp,
                                topEnd      = if (isUser) 4.dp  else 16.dp,
                                bottomStart = 16.dp, bottomEnd = 16.dp
                            ),
                            modifier = Modifier.widthIn(max = 300.dp)
                        ) {
                            Text(msg.content, fontSize = 13.sp,
                                fontFamily = if (!isUser && msg.content.contains("```"))
                                    FontFamily.Monospace else FontFamily.Default,
                                lineHeight = 20.sp,
                                modifier   = Modifier.padding(10.dp))
                        }
                    }
                }
                if (isLoading) {
                    item {
                        Row(Modifier.fillMaxWidth(), Arrangement.Start) {
                            Surface(
                                color  = selectedProvider.color.copy(0.15f),
                                shape  = RoundedCornerShape(50), modifier = Modifier.size(28.dp)
                            ) {
                                Box(Modifier.fillMaxSize(), Alignment.Center) {
                                    Text(selectedProvider.name.first().toString(),
                                        fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                        color = selectedProvider.color)
                                }
                            }
                            Spacer(Modifier.width(6.dp))
                            Surface(color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)) {
                                Row(Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    repeat(3) {
                                        CircularProgressIndicator(Modifier.size(6.dp),
                                            color = selectedProvider.color, strokeWidth = 1.5.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider()

        // ── Input area ────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .imePadding(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value         = userInput,
                onValueChange = { userInput = it },
                placeholder   = { Text("Tanya ${selectedProvider.name}...") },
                modifier      = Modifier.weight(1f),
                shape         = RoundedCornerShape(16.dp),
                maxLines      = 5,
                enabled       = !isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (userInput.isNotBlank() && hasKey && !isLoading) {
                        val msg = userInput; userInput = ""
                        scope.launch {
                            sendAiMessage(context, selectedProvider, msg, messages,
                                { isLoading = it }, { errorMsg = it })
                        }
                    }
                })
            )
            FloatingActionButton(
                onClick = {
                    if (userInput.isNotBlank() && hasKey && !isLoading) {
                        val msg = userInput; userInput = ""
                        scope.launch {
                            sendAiMessage(context, selectedProvider, msg, messages,
                                { isLoading = it }, { errorMsg = it })
                        }
                    }
                },
                modifier       = Modifier.size(48.dp),
                containerColor = if (hasKey && userInput.isNotBlank())
                    selectedProvider.color else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(Icons.Rounded.Send, null, Modifier.size(20.dp))
            }
        }
    }
}

private suspend fun sendAiMessage(
    context   : android.content.Context,
    provider  : AiProviderInfo,
    userMsg   : String,
    messages  : MutableList<ChatMessage>,
    setLoading: (Boolean) -> Unit,
    setError  : (String) -> Unit
) {
    val apiKey = ApiKeyVault.get(context, provider.id)
    if (apiKey.isBlank()) { setError("API key belum diatur"); return }
    setError(""); setLoading(true)
    messages.add(ChatMessage("user", userMsg))
    try {
        val reply = withContext(Dispatchers.IO) {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val body: String
            val request: Request

            when (provider.id) {
                "anthropic" -> {
                    val msgs = JSONArray().apply {
                        messages.dropLast(0).forEach { m ->
                            put(JSONObject().put("role", m.role).put("content", m.content))
                        }
                    }
                    body = JSONObject().apply {
                        put("model", provider.model)
                        put("max_tokens", 1024)
                        put("system", "Kamu adalah asisten coding yang membantu. Jawab dalam Bahasa Indonesia jika pertanyaannya dalam Bahasa Indonesia.")
                        put("messages", msgs)
                    }.toString()
                    request = Request.Builder()
                        .url(provider.endpoint)
                        .post(body.toRequestBody("application/json".toMediaType()))
                        .addHeader("x-api-key", apiKey)
                        .addHeader("anthropic-version", "2023-06-01")
                        .addHeader("Content-Type", "application/json")
                        .build()
                    val res = client.newCall(request).execute()
                    val json = JSONObject(res.body!!.string())
                    if (!res.isSuccessful) throw Exception(json.optString("error","Gagal"))
                    json.getJSONArray("content").getJSONObject(0).getString("text")
                }
                "gemini" -> {
                    val parts = JSONArray().apply {
                        messages.forEach { m ->
                            put(JSONObject()
                                .put("role", if (m.role == "assistant") "model" else "user")
                                .put("parts", JSONArray().put(JSONObject().put("text", m.content))))
                        }
                    }
                    body = JSONObject().put("contents", parts).toString()
                    request = Request.Builder()
                        .url("${provider.endpoint}?key=$apiKey")
                        .post(body.toRequestBody("application/json".toMediaType()))
                        .addHeader("Content-Type", "application/json")
                        .build()
                    val res = client.newCall(request).execute()
                    val json = JSONObject(res.body!!.string())
                    if (!res.isSuccessful) throw Exception(json.optString("error","Gagal"))
                    json.getJSONArray("candidates").getJSONObject(0)
                        .getJSONObject("content").getJSONArray("parts")
                        .getJSONObject(0).getString("text")
                }
                else -> {
                    // OpenAI-compatible (ChatGPT, DeepSeek, Groq, OpenRouter, Qwen, Manus)
                    val msgs = JSONArray().apply {
                        put(JSONObject().put("role","system").put("content",
                            "Kamu adalah asisten coding yang membantu. Jawab dalam Bahasa Indonesia jika pertanyaannya dalam Bahasa Indonesia."))
                        messages.forEach { m ->
                            put(JSONObject().put("role", m.role).put("content", m.content))
                        }
                    }
                    body = JSONObject().apply {
                        put("model", provider.model)
                        put("messages", msgs)
                        put("max_tokens", 1024)
                    }.toString()
                    request = Request.Builder()
                        .url(provider.endpoint)
                        .post(body.toRequestBody("application/json".toMediaType()))
                        .addHeader("Authorization", "Bearer $apiKey")
                        .addHeader("Content-Type", "application/json")
                        .build()
                    val res = client.newCall(request).execute()
                    val json = JSONObject(res.body!!.string())
                    if (!res.isSuccessful) {
                        val errMsg = json.optJSONObject("error")?.optString("message") ?: "Gagal"
                        throw Exception(errMsg)
                    }
                    json.getJSONArray("choices").getJSONObject(0)
                        .getJSONObject("message").getString("content")
                }
            }
        }
        messages.add(ChatMessage("assistant", reply))
    } catch (e: Exception) {
        messages.removeLastOrNull()
        val hint = when {
            e.message?.contains("401") == true -> "API key salah atau tidak valid."
            e.message?.contains("429") == true -> "Rate limit tercapai. Coba beberapa saat lagi."
            e.message?.contains("network") == true ||
            e.message?.contains("connect") == true -> "Tidak ada koneksi internet."
            else -> e.message ?: "Error tidak diketahui"
        }
        setError("❌ $hint")
    } finally {
        setLoading(false)
    }
}