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

@Composable
fun PythonLibraryScreen() {
    val popularLibs = listOf(
        Triple("numpy",      "Komputasi numerik dan array multidimensi",           "2.0.0"),
        Triple("pandas",     "Analisis dan manipulasi data",                        "2.2.0"),
        Triple("requests",   "HTTP requests yang simpel",                           "2.32.0"),
        Triple("matplotlib", "Visualisasi data dan grafik",                         "3.9.0"),
        Triple("flask",      "Web framework ringan untuk Python",                   "3.0.0"),
        Triple("django",     "Web framework full-featured",                         "5.0.0"),
        Triple("fastapi",    "API modern, cepat, berbasis type hints",              "0.111.0"),
        Triple("pillow",     "Pemrosesan gambar (PIL Fork)",                        "10.3.0"),
        Triple("scikit-learn","Machine learning tools",                             "1.5.0"),
        Triple("tensorflow", "Machine learning & deep learning",                    "2.17.0"),
        Triple("torch",      "PyTorch — deep learning framework",                   "2.3.0"),
        Triple("opencv-python","Computer vision & image processing",               "4.10.0"),
        Triple("sqlalchemy", "Database ORM untuk Python",                          "2.0.0"),
        Triple("pytest",     "Framework testing Python",                            "8.2.0"),
        Triple("beautifulsoup4","Web scraping — parse HTML/XML",                   "4.12.0"),
        Triple("selenium",   "Otomasi browser web",                                "4.21.0"),
        Triple("paramiko",   "SSH client library",                                  "3.4.0"),
        Triple("cryptography","Kriptografi & keamanan",                            "42.0.0"),
        Triple("aiohttp",    "HTTP client/server async",                            "3.9.0"),
        Triple("pydantic",   "Data validation dengan type hints",                   "2.7.0"),
    )

    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) popularLibs
        else popularLibs.filter {
            it.first.contains(searchQuery, true) || it.second.contains(searchQuery, true)
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Header
        Surface(tonalElevation = 2.dp) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.MenuBook, null,
                        tint = Color(0xFF00BCD4), modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Python Library Browser", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("${popularLibs.size} library populer PyPI",
                            fontSize = 11.sp,
                            color    = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder   = { Text("Cari library...") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    leadingIcon   = { Icon(Icons.Rounded.Search, null) },
                    trailingIcon  = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Close, null)
                            }
                        }
                    }
                )
            }
        }
        HorizontalDivider()

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filtered) { (name, desc, version) ->
                var installing by remember { mutableStateOf(false) }
                var installed  by remember { mutableStateOf(false) }

                Card(shape = RoundedCornerShape(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color  = Color(0xFF00BCD4).copy(0.1f),
                            shape  = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Text("py", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00BCD4))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.width(6.dp))
                                Text("v$version", fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                            }
                            Text(desc, fontSize = 11.sp,
                                color   = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                                maxLines = 2)
                        }
                        Spacer(Modifier.width(8.dp))
                        if (installed) {
                            Icon(Icons.Rounded.CheckCircle, null,
                                tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                        } else {
                            OutlinedButton(
                                onClick  = { installing = true /* TODO: pip install */ },
                                enabled  = !installing,
                                shape    = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                if (installing) {
                                    CircularProgressIndicator(Modifier.size(12.dp), strokeWidth = 1.5.dp)
                                } else {
                                    Text("Install", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}