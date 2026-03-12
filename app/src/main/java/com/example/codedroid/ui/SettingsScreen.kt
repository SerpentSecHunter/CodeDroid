package com.example.codedroid.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.WrapText
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codedroid.editor.EditorThemes

@Composable
fun SettingsScreen(
    themeMode      : String,  onThemeChange  : (String)  -> Unit,
    fontSize       : Int,     onFontSize     : (Int)     -> Unit,
    wordWrap       : Boolean, onWordWrap     : (Boolean) -> Unit,
    showLineNums   : Boolean, onShowLineNums : (Boolean) -> Unit,
    autoSave       : Boolean, onAutoSave     : (Boolean) -> Unit,
    tabSize        : Int,     onTabSize      : (Int)     -> Unit,
    editorTheme    : String,  onEditorTheme  : (String)  -> Unit
) {
    LazyColumn(
        modifier       = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── APPEARANCE ────────────────────────────────────────────
        item {
            SettingSection(title = "🎨 Tampilan") {
                // App Theme
                SettingRow(
                    icon  = Icons.Rounded.Palette,
                    title = "Tema Aplikasi",
                    color = Color(0xFF64B5F6)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "dark"  to "🌙",
                            "light" to "☀️",
                            "auto"  to "🔄"
                        ).forEach { (mode, emoji) ->
                            FilterChip(
                                selected = themeMode == mode,
                                onClick  = { onThemeChange(mode) },
                                label    = { Text(emoji + " " + when(mode){
                                    "dark" -> "Gelap"; "light" -> "Terang"; else -> "Auto"
                                }, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                HorizontalDivider(Modifier.padding(horizontal = 8.dp),
                    0.5.dp, MaterialTheme.colorScheme.outline.copy(0.3f))

                // Editor Theme
                Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.ColorLens, null, Modifier.size(18.dp),
                            Color(0xFFCE93D8))
                        Spacer(Modifier.width(12.dp))
                        Text("Tema Editor", fontSize = 14.sp, fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                    EditorThemes.allList.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            row.forEach { (key, theme) ->
                                FilterChip(
                                    selected = editorTheme == key,
                                    onClick  = { onEditorTheme(key) },
                                    label    = { Text(theme.name, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    leadingIcon = {
                                        if (editorTheme == key) {
                                            Icon(Icons.Rounded.Check, null, Modifier.size(14.dp))
                                        }
                                    }
                                )
                            }
                            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }

        // ── EDITOR ────────────────────────────────────────────────
        item {
            SettingSection(title = "✏️ Editor") {
                // Font size
                SettingRow(Icons.Rounded.FormatSize, "Ukuran Font",
                    "${fontSize}sp", Color(0xFF81C784)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick  = { if (fontSize > 8) onFontSize(fontSize - 1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Rounded.Remove, null, Modifier.size(14.dp))
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("$fontSize", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                color    = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                textAlign = TextAlign.Center)
                        }
                        IconButton(
                            onClick  = { if (fontSize < 28) onFontSize(fontSize + 1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Rounded.Add, null, Modifier.size(14.dp))
                        }
                    }
                }

                HorizontalDivider(Modifier.padding(horizontal = 8.dp),
                    0.5.dp, MaterialTheme.colorScheme.outline.copy(0.3f))

                // Tab size
                SettingRow(Icons.Rounded.SpaceBar, "Ukuran Tab",
                    "$tabSize spasi", Color(0xFFFFCC02)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(2, 4, 8).forEach { s ->
                            FilterChip(
                                selected = tabSize == s,
                                onClick  = { onTabSize(s) },
                                label    = { Text("$s", fontSize = 12.sp) }
                            )
                        }
                    }
                }

                HorizontalDivider(Modifier.padding(horizontal = 8.dp),
                    0.5.dp, MaterialTheme.colorScheme.outline.copy(0.3f))

                SettingToggleRow(
                    Icons.AutoMirrored.Filled.WrapText, "Word Wrap",
                    "Bungkus teks panjang ke baris baru", Color(0xFF80DEEA),
                    wordWrap, onWordWrap
                )
                HorizontalDivider(Modifier.padding(horizontal = 8.dp),
                    0.5.dp, MaterialTheme.colorScheme.outline.copy(0.3f))
                SettingToggleRow(
                    Icons.Rounded.FormatListNumbered, "Nomor Baris",
                    "Tampilkan nomor baris di kiri editor", Color(0xFFFFAB91),
                    showLineNums, onShowLineNums
                )
                HorizontalDivider(Modifier.padding(horizontal = 8.dp),
                    0.5.dp, MaterialTheme.colorScheme.outline.copy(0.3f))
                SettingToggleRow(
                    Icons.Rounded.Save, "Auto Save",
                    "Simpan file otomatis saat mengetik", Color(0xFFA5D6A7),
                    autoSave, onAutoSave
                )
            }
        }

        // ── ABOUT ─────────────────────────────────────────────────
        item {
            SettingSection(title = "ℹ️ Tentang") {
                Column(Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color  = MaterialTheme.colorScheme.primary.copy(0.1f),
                            shape  = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Text("</>", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("CodeDroid", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Text("Versi 2.3.0", fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(0.3f))
                    InfoRow("Developer", "Ade Pratama")
                    InfoRow("Email", "luarnegriakun702@gmail.com")
                    InfoRow("Min SDK", "Android 8.0 (API 26)")
                    InfoRow("Build", "Kotlin 2.0 + Jetpack Compose + Material3")
                    HorizontalDivider(thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(0.3f))
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(0.08f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("⭐ Beri bintang di GitHub jika CodeDroid membantu!",
                                fontSize = 12.sp,
                                color    = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSection(
    title  : String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold,
            color    = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp))
        Card(
            shape  = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingRow(
    icon    : ImageVector,
    title   : String,
    subtitle: String = "",
    color   : Color  = MaterialTheme.colorScheme.primary,
    content : @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Surface(
                color  = color.copy(0.12f),
                shape  = RoundedCornerShape(8.dp),
                modifier = Modifier.size(34.dp)
            ) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Icon(icon, null, Modifier.size(16.dp), color)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                if (subtitle.isNotBlank()) {
                    Text(subtitle, fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.45f))
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        content()
    }
}

@Composable
private fun SettingToggleRow(
    icon    : ImageVector,
    title   : String,
    subtitle: String,
    color   : Color,
    value   : Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Surface(
                color  = color.copy(0.12f),
                shape  = RoundedCornerShape(8.dp),
                modifier = Modifier.size(34.dp)
            ) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Icon(icon, null, Modifier.size(16.dp), color)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(subtitle, fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.45f))
            }
        }
        Switch(checked = value, onCheckedChange = onChange)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
        Text(value, fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(0.8f))
    }
}