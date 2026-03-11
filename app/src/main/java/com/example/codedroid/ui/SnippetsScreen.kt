package com.example.codedroid.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codedroid.data.Snippet
import com.example.codedroid.data.SnippetManager

@Composable
fun SnippetsScreen(onInsert: (String) -> Unit) {
    val context   = LocalContext.current
    var snippets  by remember { mutableStateOf(SnippetManager.getAll(context)) }
    var showDialog by remember { mutableStateOf(false) }
    var editItem  by remember { mutableStateOf<Snippet?>(null) }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Snippet", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            FloatingActionButton(
                onClick = { editItem = null; showDialog = true },
                modifier = Modifier.size(40.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) { Icon(Icons.Default.Add, "Tambah", Modifier.size(20.dp)) }
        }

        if (snippets.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Belum ada snippet",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { showDialog = true }) { Text("Buat snippet pertama") }
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(snippets, key = { it.id }) { s ->
                    Card(shape = RoundedCornerShape(10.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Row(Modifier.fillMaxWidth(),
                                Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(s.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(s.language.uppercase(), fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary)
                                }
                                Row {
                                    IconButton(onClick = { editItem = s; showDialog = true },
                                        Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Edit, null, Modifier.size(15.dp))
                                    }
                                    IconButton(onClick = {
                                        SnippetManager.delete(context, s.id)
                                        snippets = SnippetManager.getAll(context)
                                    }, Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, null, Modifier.size(15.dp),
                                            MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Surface(color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)) {
                                Text(s.code.take(100) + if (s.code.length > 100) "..." else "",
                                    fontSize = 11.sp, fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(8.dp))
                            }
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { onInsert(s.code) },
                                modifier = Modifier.fillMaxWidth().height(34.dp),
                                shape = RoundedCornerShape(8.dp)) {
                                Text("Sisipkan ke Editor", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            var title by remember { mutableStateOf(editItem?.title ?: "") }
            var code  by remember { mutableStateOf(editItem?.code ?: "") }
            var lang  by remember { mutableStateOf(editItem?.language ?: "text") }
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(if (editItem == null) "Snippet Baru" else "Edit Snippet") },
                text  = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(title, { title = it }, label = { Text("Judul") },
                            singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(lang, { lang = it }, label = { Text("Bahasa") },
                            singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(code, { code = it }, label = { Text("Kode") },
                            modifier = Modifier.fillMaxWidth().height(140.dp))
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (title.isNotBlank() && code.isNotBlank()) {
                            SnippetManager.save(context,
                                editItem?.copy(title = title, code = code, language = lang)
                                    ?: Snippet(title = title, code = code, language = lang))
                            snippets   = SnippetManager.getAll(context)
                            showDialog = false
                        }
                    }) { Text("Simpan") }
                },
                dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Batal") } }
            )
        }
    }
}
