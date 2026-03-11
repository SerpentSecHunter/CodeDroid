package com.example.codedroid.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    LazyColumn(Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {

        item {
            SectionLabel("Tampilan")
            Card(shape = RoundedCornerShape(12.dp)) {
                Column {
                    // Tema Aplikasi
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        SettingLabel(Icons.Default.Palette, "Tema Aplikasi",
                            when (themeMode) { "dark" -> "Gelap"; "light" -> "Terang"; else -> "Otomatis" })
                        Row {
                            listOf("🌙" to "dark", "☀️" to "light", "🔄" to "auto").forEach { (e, m) ->
                                TextButton(onClick = { onThemeChange(m) },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = if (themeMode == m)
                                            MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )) { Text(e, fontSize = 18.sp) }
                            }
                        }
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), 0.5.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    // Tema Editor
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ColorLens, null, Modifier.size(20.dp),
                                MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Text("Tema Editor", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(Modifier.height(10.dp))
                        EditorThemes.allList.chunked(3).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()) {
                                row.forEach { (key, theme) ->
                                    FilterChip(
                                        selected = editorTheme == key,
                                        onClick  = { onEditorTheme(key) },
                                        label    = { Text(theme.name, fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }
        }

        item {
            SectionLabel("Editor")
            Card(shape = RoundedCornerShape(12.dp)) {
                Column {
                    // Font Size
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        SettingLabel(Icons.Default.FormatSize, "Ukuran Font", "${fontSize}sp")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (fontSize > 8) onFontSize(fontSize - 1) },
                                Modifier.size(32.dp)) { Icon(Icons.Default.Remove, null, Modifier.size(14.dp)) }
                            Text("$fontSize", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                            IconButton(onClick = { if (fontSize < 28) onFontSize(fontSize + 1) },
                                Modifier.size(32.dp)) { Icon(Icons.Default.Add, null, Modifier.size(14.dp)) }
                        }
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), 0.5.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    // Tab size
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        SettingLabel(Icons.Default.SpaceBar, "Ukuran Tab", "${tabSize} spasi")
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(2, 4, 8).forEach { s ->
                                FilterChip(tabSize == s, { onTabSize(s) },
                                    { Text("$s", fontSize = 12.sp) })
                            }
                        }
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), 0.5.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    SettingToggle("Word Wrap", "Bungkus teks panjang",
                        Icons.Default.WrapText, wordWrap, onWordWrap)
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), 0.5.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    SettingToggle("Nomor Baris", "Tampilkan nomor baris di kiri",
                        Icons.Default.FormatListNumbered, showLineNums, onShowLineNums)
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), 0.5.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    SettingToggle("Auto Save", "Simpan otomatis saat mengetik",
                        Icons.Default.Save, autoSave, onAutoSave)
                }
            }
        }

        item {
            SectionLabel("Tentang")
            Card(shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("CodeDroid", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                    Text("Versi 2.0.0", fontSize = 13.sp)
                    Text("Developer: Ade Pratama", fontSize = 13.sp)
                    Text("Email: luarnegriakun702@gmail.com", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp))
}

@Composable
private fun SettingLabel(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(20.dp), MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun SettingToggle(title: String, subtitle: String,
    icon: ImageVector, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        Arrangement.SpaceBetween, Alignment.CenterVertically) {
        SettingLabel(icon, title, subtitle)
        Switch(checked = value, onCheckedChange = onChange)
    }
}