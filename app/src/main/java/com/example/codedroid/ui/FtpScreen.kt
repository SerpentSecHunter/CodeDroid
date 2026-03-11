package com.example.codedroid.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codedroid.network.FtpConfig
import com.example.codedroid.network.FtpHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTPFile
import java.io.File

@Composable
fun FtpScreen(onOpenFile: (File) -> Unit) {
    val scope     = rememberCoroutineScope()
    var host      by remember { mutableStateOf("") }
    var port      by remember { mutableStateOf("21") }
    var user      by remember { mutableStateOf("anonymous") }
    var pass      by remember { mutableStateOf("") }
    var connected by remember { mutableStateOf(false) }
    var loading   by remember { mutableStateOf(false) }
    var files     by remember { mutableStateOf<List<FTPFile>>(emptyList()) }
    var status    by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp),
           verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Text("FTP Client", fontSize = 18.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)

        if (!connected) {
            Card(shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(host, { host = it }, label = { Text("Host / IP") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        shape = RoundedCornerShape(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(port, { port = it }, label = { Text("Port") },
                            modifier = Modifier.width(90.dp), singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(user, { user = it }, label = { Text("Username") },
                            modifier = Modifier.weight(1f), singleLine = true,
                            shape = RoundedCornerShape(8.dp))
                    }
                    OutlinedTextField(pass, { pass = it }, label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(8.dp))
                    if (status.isNotEmpty()) {
                        Text(status, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                loading = true; status = ""
                                val result = withContext(Dispatchers.IO) {
                                    runCatching {
                                        FtpHelper.connect(FtpConfig(host,
                                            port.toIntOrNull() ?: 21, user, pass))
                                    }.getOrNull()
                                }
                                if (result?.success == true) {
                                    connected = true
                                    files = withContext(Dispatchers.IO) { FtpHelper.listFiles() }
                                } else { status = result?.message ?: "Gagal terhubung" }
                                loading = false
                            }
                        },
                        enabled  = host.isNotBlank() && !loading,
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(8.dp)
                    ) {
                        if (loading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        else Text("Hubungkan")
                    }
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Terhubung: $host", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f))
                TextButton(onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) { FtpHelper.disconnect() }
                        connected = false; files = emptyList()
                    }
                }) { Text("Putus", color = MaterialTheme.colorScheme.error) }
            }
            Text(FtpHelper.currentPath, fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

            LazyColumn(Modifier.weight(1f)) {
                items(files) { f ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable {
                                if (f.isDirectory) {
                                    scope.launch {
                                        val newPath = "${FtpHelper.currentPath}/${f.name}"
                                        withContext(Dispatchers.IO) { FtpHelper.changeDirectory(newPath) }
                                        files = withContext(Dispatchers.IO) { FtpHelper.listFiles() }
                                    }
                                } else {
                                    scope.launch {
                                        val tmp = File.createTempFile("ftp_", "_${f.name}")
                                        withContext(Dispatchers.IO) {
                                            FtpHelper.downloadFile("${FtpHelper.currentPath}/${f.name}", tmp)
                                        }
                                        onOpenFile(tmp)
                                    }
                                }
                            }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (f.isDirectory) "DIR" else "FILE", fontSize = 10.sp,
                            color = if (f.isDirectory) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.width(36.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(f.name, fontSize = 14.sp, maxLines = 1,
                            overflow = TextOverflow.Ellipsis)
                    }
                    HorizontalDivider(thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                }
            }
        }
    }
}