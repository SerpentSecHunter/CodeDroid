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

data class Extension(
    val id         : String,
    val name       : String,
    val description: String,
    val version    : String,
    val color      : Color,
    val isInstalled: Boolean = false,
    val comingSoon : Boolean = false
)

@Composable
fun ExtensionsScreen() {
    val extensions = remember {
        listOf(
            Extension("prettier",     "Prettier",                "Auto-format kode (JS, HTML, CSS, JSON)",        "3.3.0",  Color(0xFFF7BA3E), comingSoon = true),
            Extension("material-icon","Material Icon Theme",     "Icon file sesuai tipe (kt, py, html, dll)",     "5.0.0",  Color(0xFF4CAF50), comingSoon = true),
            Extension("eslint",       "ESLint",                  "Deteksi error JS/TS secara real-time",          "9.0.0",  Color(0xFF4B32C3), comingSoon = true),
            Extension("error-lens",   "Error Lens",              "Tampilkan error inline langsung di kode",       "3.16.0", Color(0xFFF44336), comingSoon = true),
            Extension("php-intel",    "PHP Intelephense",        "Autocomplete + syntax PHP profesional",         "1.12.0", Color(0xFF6A1B9A), comingSoon = true),
            Extension("laravel-extra","Laravel Extra Intellisense","Helper Laravel (route, model, config, dll)", "1.3.0",  Color(0xFFFF5722), comingSoon = true),
            Extension("blade-tools",  "Laravel Blade Snippets",  "Snippet untuk syntax Blade template",           "2.0.0",  Color(0xFFE91E63), comingSoon = true),
            Extension("auto-close",   "Auto Close Tag",          "Otomatis tutup tag HTML/XML saat mengetik",     "0.5.15", Color(0xFF00BCD4), comingSoon = true),
            Extension("one-dark-pro", "One Dark Pro",            "Tema editor populer ala VS Code",               "3.0.0",  Color(0xFF282C34), comingSoon = true),
            Extension("auto-rename",  "Auto Rename Tag",         "Rename tag pembuka, tag penutup ikut berubah",  "0.1.10", Color(0xFFFF9800), comingSoon = true),
            Extension("indent-rainbow","Indent Rainbow",         "Warna berbeda per level indentasi",             "8.3.1",  Color(0xFF9C27B0), comingSoon = true),
            Extension("wakatime",     "WakaTime",                "Tracking waktu coding otomatis",                "24.0.0", Color(0xFF00BFA5), comingSoon = true),
            Extension("pets",         "CodeDroid Pets",          "Hewan peliharaan virtual di editor 🐱",         "1.0.0",  Color(0xFFFF80AB), comingSoon = true),
            Extension("codesnap",     "CodeSnap",                "Screenshot kode yang cantik dengan watermark",  "1.3.4",  Color(0xFF1565C0), comingSoon = true),
            Extension("img-preview",  "Image Preview",           "Preview gambar inline langsung di editor",      "0.30.0", Color(0xFF2E7D32), comingSoon = true),
        )
    }

    var installedStates by remember { mutableStateOf(extensions.associateWith { false }) }

    Column(Modifier.fillMaxSize()) {
        // Header
        Surface(tonalElevation = 2.dp) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Extension, null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Extensions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("${extensions.size} extensions tersedia",
                            fontSize = 11.sp,
                            color    = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    }
                }
                Spacer(Modifier.height(10.dp))
                Surface(
                    color  = MaterialTheme.colorScheme.primary.copy(0.08f),
                    shape  = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Info, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Extension system akan tersedia di update berikutnya.",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        HorizontalDivider()

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding    = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(extensions) { ext ->
                Card(shape = RoundedCornerShape(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color dot
                        Surface(
                            color  = ext.color.copy(0.15f),
                            shape  = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Icon(Icons.Rounded.Extension, null,
                                    tint = ext.color, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(ext.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.width(6.dp))
                                Text("v${ext.version}", fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                            }
                            Text(ext.description, fontSize = 11.sp,
                                color   = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                                maxLines = 2)
                        }
                        Spacer(Modifier.width(8.dp))
                        if (ext.comingSoon) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Soon", fontSize = 10.sp,
                                    color    = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}