package com.example.codedroid.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codedroid.data.ExtensionManager
import com.example.codedroid.data.ExtensionInfo
import com.example.codedroid.data.ExtensionsData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionsScreen() {
    val context       = LocalContext.current
    val allExtensions = ExtensionsData.allExtensions
    var installed     by remember { mutableStateOf(ExtensionManager.getInstalled(context)) }
    var searchQuery   by remember { mutableStateOf("") }
    var filterCategory by remember { mutableStateOf("All") }
    
    val categories = listOf("All") + allExtensions.map { it.category }.distinct().sorted()
    val filtered = allExtensions.filter { ext ->
        (filterCategory == "All" || ext.category == filterCategory) &&
        (searchQuery.isBlank() || ext.name.contains(searchQuery, true) ||
         ext.description.contains(searchQuery, true) || ext.author.contains(searchQuery, true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.RocketLaunch, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Extension Marketplace", 
                            fontSize = 20.sp, 
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            "${installed.size} plugins installed • ${allExtensions.size}+ total",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, author or category...", fontSize = 14.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Rounded.Search, null, Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Close, null, Modifier.size(18.dp))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f)
                    ),
                    singleLine = true
                )

                Spacer(Modifier.height(12.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = filterCategory == cat
                        Surface(
                            modifier = Modifier.clickable { filterCategory = cat },
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
                            shape = RoundedCornerShape(10.dp),
                            border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.1f)) else null
                        ) {
                            Text(
                                text = cat,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filtered, key = { it.id }) { ext ->
                ExtensionCard(
                    ext = ext,
                    isInstalled = installed.contains(ext.id),
                    onAction = {
                        if (installed.contains(ext.id)) ExtensionManager.uninstall(context, ext.id)
                        else ExtensionManager.install(context, ext.id)
                        installed = ExtensionManager.getInstalled(context)
                    }
                )
            }
        }
    }
}

@Composable
fun ExtensionCard(
    ext: ExtensionInfo,
    isInstalled: Boolean,
    onAction: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.2f)
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ext.color.copy(alpha = 0.15f))
                        .border(1.dp, ext.color.copy(0.3f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when(ext.category) {
                        "Language"   -> Icons.Rounded.Translate
                        "Theme"      -> Icons.Rounded.Palette
                        "AI"         -> Icons.Rounded.Psychology
                        "Snippet"    -> Icons.Rounded.Code
                        "Framework"  -> Icons.Rounded.DynamicForm
                        "Stats"      -> Icons.Rounded.BarChart
                        "Formatter"  -> Icons.Rounded.AutoFixNormal
                        "Utility"    -> Icons.Rounded.Build
                        else         -> Icons.Rounded.Extension
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(icon, null, tint = ext.color, modifier = Modifier.size(24.dp))
                        Text(
                            text = ext.name.take(1).uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = ext.color.copy(0.6f)
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ext.name, 
                        fontSize = 15.sp, 
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = ext.description, 
                        fontSize = 12.sp, 
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = ext.author, 
                            fontSize = 10.sp, 
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(" • ", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.3f))
                        Icon(Icons.Rounded.Star, null, Modifier.size(10.dp), tint = Color(0xFFFFB300))
                        Spacer(Modifier.width(2.dp))
                        Text(ext.rating.toString(), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        Text(" • ", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.3f))
                        Text(ext.installs, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    }
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = { onAction() },
                    shape = RoundedCornerShape(8.dp),
                    colors = if (isInstalled) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(0.1f),
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    } else {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    modifier = Modifier.height(34.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(
                        text = if (isInstalled) "Uninstall" else "Install", 
                        fontSize = 11.sp, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.1f))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "VERSION: ${ext.version}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = ext.detail,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.8f),
                        lineHeight = 18.sp
                    )
                    
                    if (isInstalled) {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xFF4CAF50).copy(0.1f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Rounded.VerifiedUser, null, Modifier.size(12.dp), tint = Color(0xFF4CAF50))
                            Spacer(Modifier.width(4.dp))
                            Text("Active in System Editor", fontSize = 10.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}