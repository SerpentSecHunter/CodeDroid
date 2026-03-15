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
import com.example.codedroid.data.ExtensionInfo
import com.example.codedroid.data.ExtensionsData

@Composable
fun ExtensionsScreen() {
    val context       = LocalContext.current
    val allExtensions = ExtensionsData.allExtensions
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