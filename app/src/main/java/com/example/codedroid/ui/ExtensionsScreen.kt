package com.example.codedroid.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codedroid.data.ExtensionManager

data class ExtensionInfo(
    val id         : String,
    val name       : String,
    val description: String,
    val detail     : String,
    val version    : String,
    val author     : String,
    val color      : Color,
    val category   : String
)

val allExtensions = listOf(
    ExtensionInfo("prettier","Prettier","Auto-format kode otomatis","Format JS, HTML, CSS, JSON, Markdown dengan style konsisten","3.3.0","Prettier",Color(0xFFF7BA3E),"Formatter"),
    ExtensionInfo("material-icon","Material Icon Theme","Icon file sesuai tipe","Tampilkan icon yang sesuai untuk setiap tipe file di file manager","5.0.0","PKief",Color(0xFF4CAF50),"Theme"),
    ExtensionInfo("eslint","ESLint","Deteksi error JavaScript","Analisis kode JS/TS secara real-time dan tampilkan error","9.0.0","Microsoft",Color(0xFF4B32C3),"Linter"),
    ExtensionInfo("error-lens","Error Lens","Error inline di kode","Tampilkan pesan error langsung di samping baris kode","3.16.0","usernamehw",Color(0xFFF44336),"UI"),
    ExtensionInfo("php-intel","PHP Intelephense","Autocomplete PHP","Autocomplete, go-to-definition, dan diagnostik untuk PHP","1.12.0","Ben Mewburn",Color(0xFF6A1B9A),"Language"),
    ExtensionInfo("laravel-extra","Laravel Extra Intellisense","Helper Laravel","Autocomplete untuk route, model, config, dan view Laravel","1.3.0","amir9480",Color(0xFFFF5722),"Framework"),
    ExtensionInfo("blade-tools","Laravel Blade Snippets","Snippet Blade template","Snippet lengkap untuk direktif Blade Laravel","2.0.0","Winnie Lin",Color(0xFFE91E63),"Snippet"),
    ExtensionInfo("auto-close","Auto Close Tag","Tutup tag otomatis","Otomatis menambahkan tag penutup HTML/XML saat mengetik","0.5.15","Jun Han",Color(0xFF00BCD4),"Editor"),
    ExtensionInfo("one-dark-pro","One Dark Pro","Tema gelap profesional","Tema editor populer ala VS Code One Dark Pro","3.0.0","binaryify",Color(0xFF282C34),"Theme"),
    ExtensionInfo("auto-rename","Auto Rename Tag","Rename tag serentak","Ubah tag pembuka, tag penutup otomatis ikut berubah","0.1.10","Jun Han",Color(0xFFFF9800),"Editor"),
    ExtensionInfo("indent-rainbow","Indent Rainbow","Warna indentasi","Warna berbeda untuk setiap level indentasi kode","8.3.1","oderwat",Color(0xFF9C27B0),"Editor"),
    ExtensionInfo("wakatime","WakaTime","Tracking waktu coding","Otomatis catat waktu coding dan tampilkan statistik","24.0.0","WakaTime",Color(0xFF00BFA5),"Productivity"),
    ExtensionInfo("pets","CodeDroid Pets","Hewan peliharaan virtual","Teman coding — kucing/anjing animasi di editor kamu 🐱","1.0.0","CodeDroid",Color(0xFFFF80AB),"Fun"),
    ExtensionInfo("codesnap","CodeSnap","Screenshot kode cantik","Buat screenshot kode dengan tampilan profesional + watermark","1.3.4","adpyke",Color(0xFF1565C0),"Utility"),
    ExtensionInfo("img-preview","Image Preview","Preview gambar inline","Lihat preview gambar langsung saat hover path di kode","0.30.0","kisstkondoros",Color(0xFF2E7D32),"Editor"),
)

@Composable
fun ExtensionsScreen() {
    val context       = LocalContext.current
    var installed     by remember { mutableStateOf(ExtensionManager.getInstalled(context)) }
    var searchQuery   by remember { mutableStateOf("") }
    var filterCategory by remember { mutableStateOf("Semua") }
    var expandedId    by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Semua") + allExtensions.map { it.category }.distinct()
    val filtered = allExtensions.filter { ext ->
        (filterCategory == "Semua" || ext.category == filterCategory) &&
        (searchQuery.isBlank() || ext.name.contains(searchQuery, true) ||
         ext.description.contains(searchQuery, true))
    }
    val installedCount = installed.size

    Column(Modifier.fillMaxSize()) {
        // Header
        Surface(tonalElevation = 2.dp) {
            Column(Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Extension, null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Extensions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("$installedCount terinstall • ${allExtensions.size} tersedia",
                            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    }
                }
                // Search
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder   = { Text("Cari extension...") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(10.dp),
                    leadingIcon   = { Icon(Icons.Rounded.Search, null, Modifier.size(18.dp)) },
                    trailingIcon  = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Close, null, Modifier.size(16.dp))
                            }
                        }
                    }
                )
                // Category filter
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories.size) { i ->
                        val cat = categories[i]
                        FilterChip(
                            selected = filterCategory == cat,
                            onClick  = { filterCategory = cat },
                            label    = { Text(cat, fontSize = 11.sp) }
                        )
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
            items(filtered, key = { it.id }) { ext ->
                val isInstalled = installed.contains(ext.id)
                val isExpanded  = expandedId == ext.id

                Card(
                    shape  = RoundedCornerShape(12.dp),
                    onClick = { expandedId = if (isExpanded) null else ext.id }
                ) {
                    Column(Modifier.fillMaxWidth().padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Icon
                            Surface(
                                color    = ext.color.copy(0.12f),
                                shape    = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(Modifier.fillMaxSize(), Alignment.Center) {
                                    Icon(Icons.Rounded.Extension, null,
                                        tint = ext.color,
                                        modifier = Modifier.size(22.dp))
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(ext.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(ext.category, fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                    }
                                }
                                Text(ext.description, fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f), maxLines = 1)
                                Text("${ext.author} • v${ext.version}", fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                            }
                            Spacer(Modifier.width(8.dp))
                            // Install/Uninstall button
                            if (isInstalled) {
                                OutlinedButton(
                                    onClick = {
                                        ExtensionManager.uninstall(context, ext.id)
                                        installed = ExtensionManager.getInstalled(context)
                                    },
                                    shape  = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp)
                                ) {
                                    Text("Hapus", fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.error)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        ExtensionManager.install(context, ext.id)
                                        installed = ExtensionManager.getInstalled(context)
                                    },
                                    shape  = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp)
                                ) {
                                    Text("Install", fontSize = 11.sp)
                                }
                            }
                        }

                        // Detail expandable
                        AnimatedVisibility(visible = isExpanded) {
                            Column(Modifier.padding(top = 12.dp)) {
                                HorizontalDivider()
                                Spacer(Modifier.height(10.dp))
                                Text(ext.detail, fontSize = 12.sp,
                                    color    = MaterialTheme.colorScheme.onSurface.copy(0.7f),
                                    lineHeight = 18.sp)
                                if (isInstalled) {
                                    Spacer(Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.CheckCircle, null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Terinstall dan aktif", fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}