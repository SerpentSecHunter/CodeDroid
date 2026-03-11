package com.example.codedroid.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codedroid.data.PluginManager

@Composable
fun PluginsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var plugins by remember { mutableStateOf(PluginManager.getAllPlugins(context)) }
    var selectedPlugin by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        Text("Plugin Manager", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(plugins) { plugin ->
                PluginCard(
                    plugin = plugin,
                    onToggle = { enabled ->
                        PluginManager.setEnabled(context, plugin.id, enabled)
                        plugins = PluginManager.getAllPlugins(context)
                    },
                    onExecute = { actionId ->
                        selectedPlugin = "${plugin.id}:$actionId"
                    }
                )
            }
        }
    }

    if (selectedPlugin != null) {
        PluginActionDialog(
            pluginAction = selectedPlugin!!,
            onDismiss = { selectedPlugin = null },
            onExecute = { result ->
                // Handle result
            }
        )
    }
}

@Composable
private fun PluginCard(
    plugin: com.example.codedroid.data.Plugin,
    onToggle: (Boolean) -> Unit,
    onExecute: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(plugin.icon, fontSize = 20.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(plugin.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(plugin.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
                Switch(checked = plugin.enabled, onCheckedChange = onToggle)
            }
            Spacer(Modifier.height(10.dp))
            if (plugin.actions.isNotEmpty()) {
                Text("Actions:", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(6.dp))
                plugin.actions.forEach { action ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onExecute(action.id) }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(action.label, fontSize = 13.sp)
                        Text(action.shortcut, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PluginActionDialog(
    pluginAction: String,
    onDismiss: () -> Unit,
    onExecute: (String) -> Unit
) {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Execute Plugin Action") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Input") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (result.isNotEmpty()) {
                    Text(result, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val (pluginId, actionId) = pluginAction.split(":")
                result = com.example.codedroid.data.PluginManager.executeAction(
                    context,
                    pluginId,
                    actionId,
                    input
                )
            }) { Text("Execute") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        shape = RoundedCornerShape(12.dp)
    )
}