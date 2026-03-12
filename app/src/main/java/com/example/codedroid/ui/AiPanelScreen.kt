package com.example.codedroid.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AiProvider(
    val id   : String,
    val name : String,
    val color: Color,
    val model: String
)

@Composable
fun AiPanelScreen() {
    val providers = listOf(
        AiProvider("chatgpt",  "ChatGPT",  Color(0xFF10A37F), "gpt-4o"),
        AiProvider("claude",   "Claude",   Color(0xFFD97706), "claude-sonnet-4-6"),
        AiProvider("deepseek", "DeepSeek", Color(0xFF2563EB), "deepseek-chat"),
        AiProvider("gemini",   "Gemini",   Color(0xFF4285F4), "gemini-2.0-flash"),
        AiProvider("grok",     "Grok",     Color(0xFF000000), "grok-2"),
        AiProvider("kimi",     "Kimi",     Color(0xFF6366F1), "moonshot-v1"),
        AiProvider("qwen",     "Qwen",     Color((0xFF7C3AED).toLong()), "qwen-turbo"),
        AiProvider("manus",    "Manus",    Color(0xFFEC4899), "manus-v1"),
    )

    var selectedProvider by remember { mutableStateOf<AiProvider?>(null) }
    var apiKey          by remember { mutableStateOf("") }
    var showKeyInput    by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        // Header
        Surface(tonalElevation = 2.dp) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.SmartToy, null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("AI Panel", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Hubungkan AI favorit kamu",
                            fontSize = 11.sp,
                            color    = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    }
                }
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(0.08f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Key, null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Masukkan API key milikmu — tersimpan aman di perangkat.",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        HorizontalDivider()

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(providers) { provider ->
                val isSelected = selectedProvider?.id == provider.id
                Card(
                    shape  = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            provider.color.copy(0.08f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    onClick = {
                        selectedProvider = if (isSelected) null else provider
                        showKeyInput     = !isSelected
                    }
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color  = provider.color.copy(0.12f),
                                shape  = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(Modifier.fillMaxSize(), Alignment.Center) {
                                    Text(provider.name.first().toString(),
                                        fontSize   = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color      = provider.color)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(provider.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(provider.model, fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                            }
                            if (isSelected) {
                                Icon(Icons.Rounded.CheckCircle, null,
                                    tint = provider.color, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Rounded.RadioButtonUnchecked, null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(0.3f),
                                    modifier = Modifier.size(20.dp))
                            }
                        }

                        if (isSelected) {
                            Spacer(Modifier.height(14.dp))
                            OutlinedTextField(
                                value         = apiKey,
                                onValueChange = { apiKey = it },
                                label         = { Text("API Key ${provider.name}") },
                                placeholder   = { Text("sk-...") },
                                singleLine    = true,
                                modifier      = Modifier.fillMaxWidth(),
                                shape         = RoundedCornerShape(10.dp),
                                trailingIcon  = {
                                    if (apiKey.isNotBlank()) {
                                        Icon(Icons.Rounded.Check, null,
                                            tint = provider.color)
                                    }
                                }
                            )
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick  = { /* TODO: simpan API key */ },
                                enabled  = apiKey.isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                shape    = RoundedCornerShape(10.dp),
                                colors   = ButtonDefaults.buttonColors(
                                    containerColor = provider.color)
                            ) {
                                Icon(Icons.Rounded.Save, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Simpan & Hubungkan")
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("Segera tersedia — fitur aktif di update berikutnya.",
                                fontSize = 11.sp,
                                color    = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                        }
                    }
                }
            }
        }
    }
}