package com.example.codedroid.data

data class AiEndpoint(
    val label        : String,
    val url          : String,
    val modelField   : String,
    val authHeader   : String,
    val authPrefix   : String,
    val extraHeaders : Map<String, String> = emptyMap(),
    val requestType  : String = "openai"   // "openai" | "anthropic" | "gemini"
)

object AiRouterConfig {

    val sources: Map<String, List<AiEndpoint>> = mapOf(

        "openai" to listOf(
            AiEndpoint("Official (OpenAI)",
                "https://api.openai.com/v1/chat/completions",
                "gpt-4o-mini", "Authorization", "Bearer "),
            AiEndpoint("OpenRouter",
                "https://openrouter.ai/api/v1/chat/completions",
                "openai/gpt-4o-mini", "Authorization", "Bearer ",
                mapOf("HTTP-Referer" to "https://codedroid.app"))
        ),

        "anthropic" to listOf(
            AiEndpoint("Official (Anthropic)",
                "https://api.anthropic.com/v1/messages",
                "claude-haiku-4-5-20251001", "x-api-key", "",
                mapOf("anthropic-version" to "2023-06-01"), "anthropic"),
            AiEndpoint("OpenRouter",
                "https://openrouter.ai/api/v1/chat/completions",
                "anthropic/claude-3-haiku", "Authorization", "Bearer ",
                mapOf("HTTP-Referer" to "https://codedroid.app"))
        ),

        "deepseek" to listOf(
            AiEndpoint("Official (DeepSeek)",
                "https://api.deepseek.com/chat/completions",
                "deepseek-chat", "Authorization", "Bearer "),
            AiEndpoint("OpenRouter",
                "https://openrouter.ai/api/v1/chat/completions",
                "deepseek/deepseek-chat", "Authorization", "Bearer ",
                mapOf("HTTP-Referer" to "https://codedroid.app"))
        ),

        "gemini" to listOf(
            AiEndpoint("Official (Google AI Studio)",
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent",
                "gemini-2.0-flash", "Authorization", "Bearer ", emptyMap(), "gemini"),
            AiEndpoint("OpenRouter",
                "https://openrouter.ai/api/v1/chat/completions",
                "google/gemini-flash-1.5", "Authorization", "Bearer ",
                mapOf("HTTP-Referer" to "https://codedroid.app"))
        ),

        "groq" to listOf(
            AiEndpoint("Official (Groq)",
                "https://api.groq.com/openai/v1/chat/completions",
                "llama-3.3-70b-versatile", "Authorization", "Bearer "),
            AiEndpoint("OpenRouter",
                "https://openrouter.ai/api/v1/chat/completions",
                "meta-llama/llama-3.3-70b-instruct", "Authorization", "Bearer ",
                mapOf("HTTP-Referer" to "https://codedroid.app"))
        ),

        "kimi" to listOf(
            AiEndpoint("Official (Moonshot AI)",
                "https://api.moonshot.cn/v1/chat/completions",
                "moonshot-v1-8k", "Authorization", "Bearer "),
            AiEndpoint("OpenRouter",
                "https://openrouter.ai/api/v1/chat/completions",
                "moonshot/moonshot-v1-8k", "Authorization", "Bearer ",
                mapOf("HTTP-Referer" to "https://codedroid.app"))
        ),

        "qwen" to listOf(
            AiEndpoint("Official (Alibaba Cloud)",
                "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions",
                "qwen-turbo", "Authorization", "Bearer "),
            AiEndpoint("OpenRouter",
                "https://openrouter.ai/api/v1/chat/completions",
                "qwen/qwen-turbo", "Authorization", "Bearer ",
                mapOf("HTTP-Referer" to "https://codedroid.app"))
        ),

        "openrouter" to listOf(
            AiEndpoint("OpenRouter (200+ model)",
                "https://openrouter.ai/api/v1/chat/completions",
                "openai/gpt-4o-mini", "Authorization", "Bearer ",
                mapOf("HTTP-Referer" to "https://codedroid.app"))
        )
    )

    val keyLinks: Map<String, String> = mapOf(
        "openai"     to "https://platform.openai.com/api-keys",
        "anthropic"  to "https://console.anthropic.com/settings/keys",
        "deepseek"   to "https://platform.deepseek.com/api_keys",
        "gemini"     to "https://aistudio.google.com/app/apikey",
        "groq"       to "https://console.groq.com/keys",
        "kimi"       to "https://platform.moonshot.cn/console/api-keys",
        "qwen"       to "https://dashscope.aliyuncs.com",
        "openrouter" to "https://openrouter.ai/keys"
    )

    private val selectedSource = mutableMapOf<String, Int>()
    fun getSelectedIndex(id: String) = selectedSource[id] ?: 0
    fun setSelectedIndex(id: String, i: Int) { selectedSource[id] = i }
    fun getSelected(id: String) = sources[id]?.getOrNull(getSelectedIndex(id))

    // Cari provider pertama yang sudah punya API key tersimpan
    fun findActiveProvider(hasKey: (String) -> Boolean): String? =
        listOf("openai","anthropic","deepseek","gemini","groq","kimi","qwen","openrouter")
            .firstOrNull { pid ->
                (0 until (sources[pid]?.size ?: 0)).any { si -> hasKey("${pid}_$si") }
            }
}