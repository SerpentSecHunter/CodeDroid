package com.example.codedroid.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codedroid.data.AiRouterConfig
import com.example.codedroid.data.ApiKeyVault
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────
// Data
// ─────────────────────────────────────────────────────────────────

data class AiProviderInfo(
    val id    : String,
    val name  : String,
    val color : Color,
    val emoji : String
)

val AI_PROVIDERS = listOf(
    AiProviderInfo("openai",     "ChatGPT",    Color(0xFF10A37F), "🤖"),
    AiProviderInfo("anthropic",  "Claude",     Color(0xFFD97706), "🧠"),
    AiProviderInfo("deepseek",   "DeepSeek",   Color(0xFF2563EB), "🔍"),
    AiProviderInfo("gemini",     "Gemini",     Color(0xFF4285F4), "✨"),
    AiProviderInfo("groq",       "Groq",       Color(0xFF6366F1), "⚡"),
    AiProviderInfo("kimi",       "Kimi",       Color(0xFF8B5CF6), "🌙"),
    AiProviderInfo("qwen",       "Qwen",       Color(0xFF7C3AED), "🌐"),
    AiProviderInfo("openrouter", "OpenRouter", Color(0xFFEC4899), "🔀"),
)

data class ChatMsg(val role: String, val content: String)

// ─────────────────────────────────────────────────────────────────
// AiPanelScreen
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiPanelScreen() {
    val ctx       = LocalContext.current
    val scope     = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var selProv    by remember { mutableStateOf(AI_PROVIDERS[0]) }
    var selSrcIdx  by remember(selProv) {
        mutableStateOf(AiRouterConfig.getSelectedIndex(selProv.id))
    }
    val sources    = remember(selProv) { AiRouterConfig.sources[selProv.id] ?: emptyList() }
    val selSrc     = sources.getOrNull(selSrcIdx)

    var apiKey     by remember(selProv, selSrcIdx) {
        mutableStateOf(ApiKeyVault.get(ctx, "${selProv.id}_$selSrcIdx"))
    }
    var showKey    by remember { mutableStateOf(false) }
    var editKey    by remember { mutableStateOf(false) }
    var userInput  by remember { mutableStateOf("") }
    var isLoading  by remember { mutableStateOf(false) }
    var errorMsg   by remember { mutableStateOf("") }
    val messages   = remember { mutableStateListOf<ChatMsg>() }

    val hasKey = ApiKeyVault.hasKey(ctx, "${selProv.id}_$selSrcIdx")

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(Modifier.fillMaxSize()) {

        // ── Header ────────────────────────────────────────────────
        Surface(tonalElevation = 2.dp) {
            Column(Modifier.fillMaxWidth().padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {

                // Provider chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    items(AI_PROVIDERS) { p ->
                        val pHasAnyKey = (0 until (AiRouterConfig.sources[p.id]?.size ?: 0)).any {
                            ApiKeyVault.hasKey(ctx, "${p.id}_$it") }
                        FilterChip(
                            selected = selProv.id == p.id,
                            onClick  = {
                                selProv   = p
                                selSrcIdx = AiRouterConfig.getSelectedIndex(p.id)
                                errorMsg  = ""; editKey = false
                            },
                            label = { Text("${p.emoji} ${p.name}", fontSize = 11.sp) },
                            leadingIcon = if (pHasAnyKey) {{
                                Icon(Icons.Rounded.CheckCircle, null, Modifier.size(10.dp))
                            }} else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = selProv.color.copy(.12f),
                                selectedLabelColor     = selProv.color
                            )
                        )
                    }
                }

                // Source selector
                if (sources.size > 1) {
                    var expandSrc by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expandSrc, { expandSrc = it }) {
                        OutlinedTextField(
                            value         = selSrc?.label ?: "",
                            onValueChange = {},
                            readOnly      = true,
                            label         = { Text("Sumber API", fontSize = 10.sp) },
                            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expandSrc) },
                            modifier      = Modifier.menuAnchor().fillMaxWidth(),
                            shape         = RoundedCornerShape(8.dp),
                            textStyle     = LocalTextStyle.current.copy(fontSize = 12.sp)
                        )
                        ExposedDropdownMenu(expandSrc, { expandSrc = false }) {
                            sources.forEachIndexed { idx, src ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(src.label, fontSize = 12.sp)
                                            Text("model: ${src.modelField}", fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(.5f))
                                        }
                                    },
                                    leadingIcon = if (ApiKeyVault.hasKey(ctx, "${selProv.id}_$idx")) {{
                                        Icon(Icons.Rounded.CheckCircle, null,
                                            tint = selProv.color, modifier = Modifier.size(14.dp))
                                    }} else null,
                                    onClick = {
                                        selSrcIdx = idx
                                        AiRouterConfig.setSelectedIndex(selProv.id, idx)
                                        apiKey = ApiKeyVault.get(ctx, "${selProv.id}_$idx")
                                        expandSrc = false; editKey = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Key row
                if (editKey) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value         = apiKey,
                            onValueChange = { apiKey = it },
                            label         = { Text("API Key", fontSize = 10.sp) },
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
                            if (apiKey.isNotBlank())
                                ApiKeyVault.save(ctx, "${selProv.id}_$selSrcIdx", apiKey)
                            editKey = false
                        }) { Icon(Icons.Rounded.Check, null, tint = selProv.color) }
                        IconButton(onClick = { editKey = false }) {
                            Icon(Icons.Rounded.Close, null)
                        }
                    }
                } else {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Surface(
                            color    = if (hasKey) selProv.color.copy(.08f)
                                       else MaterialTheme.colorScheme.errorContainer.copy(.5f),
                            shape    = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(Modifier.padding(9.dp, 6.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Icon(if (hasKey) Icons.Rounded.LockOpen else Icons.Rounded.Key,
                                    null, Modifier.size(13.dp),
                                    if (hasKey) selProv.color else MaterialTheme.colorScheme.error)
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    if (hasKey) "Key aktif • ${selSrc?.label ?: ""}"
                                    else "Belum ada API key",
                                    fontSize = 11.sp,
                                    color = if (hasKey) selProv.color else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        // Edit key
                        IconButton(onClick = {
                            apiKey = ApiKeyVault.get(ctx, "${selProv.id}_$selSrcIdx")
                            editKey = true
                        }) { Icon(Icons.Rounded.Edit, null, Modifier.size(17.dp)) }
                        // Buka situs key
                        IconButton(onClick = {
                            AiRouterConfig.keyLinks[selProv.id]?.let {
                                ctx.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(it))
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            }
                        }) {
                            Icon(Icons.Rounded.OpenInBrowser, null,
                                tint = selProv.color, modifier = Modifier.size(17.dp))
                        }
                        // Clear chat
                        if (messages.isNotEmpty()) {
                            IconButton(onClick = { messages.clear(); errorMsg = "" }) {
                                Icon(Icons.Rounded.DeleteSweep, null, Modifier.size(17.dp))
                            }
                        }
                    }
                }

                // Error banner
                if (errorMsg.isNotBlank()) {
                    Surface(color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)) {
                        Row(Modifier.padding(9.dp, 6.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.ErrorOutline, null, Modifier.size(13.dp),
                                MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(5.dp))
                            Text(errorMsg, fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f))
                            IconButton(onClick = { errorMsg = "" }, Modifier.size(20.dp)) {
                                Icon(Icons.Rounded.Close, null, Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }
        }
        HorizontalDivider()

        // ── Chat ──────────────────────────────────────────────────
        if (messages.isEmpty() && !isLoading) {
            Box(Modifier.weight(1f), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(selProv.emoji, fontSize = 38.sp)
                    Text(selProv.name, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = selProv.color)
                    Text(selSrc?.label ?: "", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(.4f))
                    if (!hasKey) {
                        OutlinedButton(onClick = {
                            AiRouterConfig.keyLinks[selProv.id]?.let {
                                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            }
                        }) {
                            Icon(Icons.Rounded.OpenInBrowser, null, Modifier.size(15.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Dapatkan API Key", fontSize = 12.sp)
                        }
                    }
                    if (hasKey) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            listOf(
                                "Bantu debug kode ini", "Jelaskan cara kerja OOP",
                                "Buatkan fungsi sorting Python",
                                "Review kode saya", "Cara optimasi query SQL"
                            ).forEach { s ->
                                SuggestionChip(onClick = { userInput = s },
                                    label = { Text(s, fontSize = 11.sp) })
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(state = listState, modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(messages) { msg ->
                    val isUser = msg.role == "user"
                    Row(Modifier.fillMaxWidth(),
                        if (isUser) Arrangement.End else Arrangement.Start) {
                        if (!isUser) {
                            Surface(color = selProv.color.copy(.15f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.size(28.dp)) {
                                Box(Modifier.fillMaxSize(), Alignment.Center) {
                                    Text(selProv.emoji, fontSize = 12.sp)
                                }
                            }
                            Spacer(Modifier.width(5.dp))
                        }
                        Surface(
                            color = if (isUser) MaterialTheme.colorScheme.primary.copy(.1f)
                                    else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(
                                topStart = if (isUser) 14.dp else 3.dp,
                                topEnd   = if (isUser) 3.dp  else 14.dp,
                                bottomStart = 14.dp, bottomEnd = 14.dp),
                            modifier = Modifier.widthIn(max = 290.dp)
                        ) {
                            SelectionContainer {
                                Text(msg.content, fontSize = 13.sp, lineHeight = 19.sp,
                                    modifier = Modifier.padding(10.dp),
                                    fontFamily = if (!isUser && msg.content.contains("```"))
                                        FontFamily.Monospace else FontFamily.Default)
                            }
                        }
                    }
                }
                if (isLoading) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp,
                                color = selProv.color)
                            Spacer(Modifier.width(6.dp))
                            Text("${selProv.name} sedang merespons...", fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(.5f))
                        }
                    }
                }
            }
        }

        HorizontalDivider()

        // ── Input ─────────────────────────────────────────────────
        Row(Modifier.fillMaxWidth().padding(10.dp).imePadding(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            OutlinedTextField(
                value = userInput, onValueChange = { userInput = it },
                placeholder = { Text("Tanya ${selProv.name}...") },
                modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp),
                maxLines = 5, enabled = !isLoading
            )
            FloatingActionButton(
                onClick = {
                    if (userInput.isNotBlank() && hasKey && !isLoading) {
                        val msg = userInput.trim(); userInput = ""
                        scope.launch {
                            sendAiMessage(ctx, selProv, selSrcIdx, msg, messages,
                                { isLoading = it }, { errorMsg = it })
                        }
                    }
                },
                modifier = Modifier.size(48.dp),
                containerColor = if (hasKey && userInput.isNotBlank()) selProv.color
                                 else MaterialTheme.colorScheme.surfaceVariant
            ) { Icon(Icons.Rounded.Send, null, Modifier.size(20.dp)) }
        }
    }
}