package com.example.codedroid.ui

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.documentfile.provider.DocumentFile
import coil.compose.AsyncImage
import com.example.codedroid.viewmodel.InsertMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class FileEntry(
    val uri: Uri,
    val name: String,
    val isDirectory: Boolean,
    val size: Long = 0,
    val lastModified: Long = 0,
    val extension: String = ""
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(
    onOpenFile  : (Uri) -> Unit,
    onInsertCode: ((String, InsertMode) -> Unit)? = null
) {
    val context     = LocalContext.current
    val scope       = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Dapatkan path app-specific
    val appFilesPath = remember { context.getExternalFilesDir(null)?.absolutePath ?: "" }
    
    // States untuk SAF
    var internalStorageUri by remember { mutableStateOf<Uri?>(null) }
    var sdCardUri by remember { mutableStateOf<Uri?>(null) }
    var currentPath by remember { mutableStateOf(appFilesPath) } 
    var isSafMode   by remember { mutableStateOf(false) }
    
    // Periksa storage yang sudah diberikan izin sebelumnya
    LaunchedEffect(Unit) {
        context.contentResolver.persistedUriPermissions.forEach { perm ->
            val uri = perm.uri
            if (uri.toString().contains("primary")) internalStorageUri = uri
            else if (!uri.toString().contains("primary")) sdCardUri = uri
        }
        internalStorageUri?.let { currentPath = it.toString(); isSafMode = true }
    }

    var files       by remember { mutableStateOf<List<FileEntry>>(emptyList()) }
    var isLoading   by remember { mutableStateOf(false) }
    var viewMode    by remember { mutableStateOf("list") } 
    var searchQuery by remember { mutableStateOf("") }
    var showSearch  by remember { mutableStateOf(false) }
    var showNewDialog by remember { mutableStateOf(false) }
    var newIsFolder by remember { mutableStateOf(false) }
    var newName     by remember { mutableStateOf("") }
    var newNameError by remember { mutableStateOf("") }
    var refreshKey  by remember { mutableStateOf(0) }

    // Media viewer states
    var imageViewFile by remember { mutableStateOf<FileEntry?>(null) }
    var audioViewFile by remember { mutableStateOf<FileEntry?>(null) }
    var videoViewFile by remember { mutableStateOf<FileEntry?>(null) }
    var insertTarget  by remember { mutableStateOf<Pair<FileEntry, InsertMode>?>(null) }

    // Operation states
    var deleteTarget by remember { mutableStateOf<FileEntry?>(null) }
    var renameTarget by remember { mutableStateOf<FileEntry?>(null) }
    var renameNewName by remember { mutableStateOf("") }
    var renameError   by remember { mutableStateOf("") }

    // Launcher SAF
    var pendingStorageType by remember { mutableStateOf<String?>(null) } 
    val safPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            if (pendingStorageType == "INTERNAL") internalStorageUri = it
            else if (pendingStorageType == "SD") sdCardUri = it
            currentPath = it.toString()
            isSafMode = true
        }
    }

    fun loadFiles() {
        scope.launch {
            isLoading = true
            files = withContext(Dispatchers.IO) {
                runCatching {
                    val list = if (isSafMode) {
                        val uri = Uri.parse(currentPath)
                        val dir = DocumentFile.fromTreeUri(context, uri)
                        dir?.listFiles()?.map {
                            FileEntry(
                                uri = it.uri,
                                name = it.name ?: "",
                                isDirectory = it.isDirectory,
                                size = it.length(),
                                lastModified = it.lastModified(),
                                extension = it.name?.substringAfterLast('.', "") ?: ""
                            )
                        }
                    } else {
                        val dir = File(currentPath)
                        dir.listFiles()?.map {
                            FileEntry(
                                uri = Uri.fromFile(it),
                                name = it.name,
                                isDirectory = it.isDirectory,
                                size = it.length(),
                                lastModified = it.lastModified(),
                                extension = it.extension
                            )
                        }
                    }
                    list?.filter {
                        if (searchQuery.isBlank()) true
                        else it.name.contains(searchQuery, ignoreCase = true)
                    }?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                    ?: emptyList()
                }.getOrElse { 
                    it.printStackTrace()
                    emptyList() 
                }
            }
            isLoading = false
        }
    }

    LaunchedEffect(currentPath, refreshKey) { loadFiles() }
    LaunchedEffect(searchQuery) { if (searchQuery.isNotBlank()) loadFiles() }

    Column(Modifier.fillMaxSize()) {
        Surface(tonalElevation = 2.dp) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (isSafMode) {
                            val uri = Uri.parse(currentPath)
                            val isRoot = (uri == internalStorageUri || uri == sdCardUri)
                            if (isRoot) {
                                currentPath = appFilesPath
                                isSafMode = false
                            } else {
                                val doc = DocumentFile.fromSingleUri(context, uri)
                                val parent = doc?.parentFile
                                if (parent != null) {
                                    currentPath = parent.uri.toString()
                                } else {
                                    currentPath = appFilesPath
                                    isSafMode = false
                                }
                            }
                        } else {
                            val parentFile = File(currentPath).parentFile
                            if (parentFile != null && parentFile.absolutePath.startsWith(appFilesPath)) {
                                currentPath = parentFile.absolutePath
                            } else if (parentFile != null && parentFile.absolutePath == "/") {
                                scope.launch { snackbarHostState.showSnackbar("Sudah di folder awal") }
                            }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Kembali")
                    }

                    if (showSearch) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari...") },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                        IconButton(onClick = { showSearch = false; searchQuery = "" }) {
                            Icon(Icons.Rounded.Close, null)
                        }
                    } else {
                        Column(Modifier.weight(1f)) {
                            val displayName = if (isSafMode) {
                                val uri = Uri.parse(currentPath)
                                val doc = if (uri == internalStorageUri || uri == sdCardUri) {
                                    DocumentFile.fromTreeUri(context, uri)
                                } else {
                                    DocumentFile.fromSingleUri(context, uri)
                                }
                                doc?.name ?: "Penyimpanan Utama"
                            } else {
                                File(currentPath).name.ifEmpty { "Penyimpanan Utama" }
                            }
                            Text(displayName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(currentPath, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                        }
                        IconButton(onClick = { refreshKey++ }) { Icon(Icons.Rounded.Refresh, "Muat Ulang") }
                        IconButton(onClick = { showSearch = true }) { Icon(Icons.Rounded.Search, "Cari") }
                        IconButton(onClick = { viewMode = if (viewMode == "list") "grid" else "list" }) {
                            Icon(if (viewMode == "list") Icons.Rounded.GridView else Icons.AutoMirrored.Rounded.List, "Ganti Tampilan")
                        }
                        IconButton(onClick = { newIsFolder = false; showNewDialog = true }) { Icon(Icons.Rounded.Add, "Tambah File", tint = MaterialTheme.colorScheme.primary) }
                        IconButton(onClick = { newIsFolder = true; showNewDialog = true }) { Icon(Icons.Rounded.CreateNewFolder, "Tambah Folder", tint = MaterialTheme.colorScheme.secondary) }
                    }
                }

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()).padding(start = 12.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isSafMode && currentPath == internalStorageUri?.toString(),
                        onClick = {
                            if (internalStorageUri != null) {
                                currentPath = internalStorageUri.toString()
                                isSafMode = true
                            } else {
                                pendingStorageType = "INTERNAL"
                                safPicker.launch(null)
                            }
                        },
                        label = { Text("Penyimpanan Internal") },
                        leadingIcon = { Icon(Icons.Rounded.Storage, null, Modifier.size(16.dp)) }
                    )

                    FilterChip(
                        selected = isSafMode && currentPath == sdCardUri?.toString(),
                        onClick = {
                            if (sdCardUri != null) {
                                currentPath = sdCardUri.toString()
                                isSafMode = true
                            } else {
                                pendingStorageType = "SD"
                                safPicker.launch(null)
                            }
                        },
                        label = { Text("Kartu SD") },
                        leadingIcon = { Icon(Icons.Rounded.SdCard, null, Modifier.size(16.dp)) }
                    )

                    FilterChip(
                        selected = !isSafMode && currentPath.startsWith(appFilesPath),
                        onClick = { currentPath = appFilesPath; isSafMode = false },
                        label = { Text("File Aplikasi") },
                        leadingIcon = { Icon(Icons.Rounded.FolderOpen, null, Modifier.size(16.dp)) }
                    )
                }
            }
        }

        HorizontalDivider()

        Box(Modifier.weight(1f)) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            } else if (files.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.FolderOpen, null, Modifier.size(64.dp), tint = Color.Gray.copy(0.3f))
                        Text("Folder kosong", color = Color.Gray)
                    }
                }
            } else {
                if (viewMode == "list") {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(files, key = { it.uri.toString() }) { file ->
                            FileListItem(
                                file = file,
                                onClick = {
                                    handleFileClick(
                                        file, onOpenFile,
                                        { imageViewFile = it },
                                        { audioViewFile = it },
                                        { videoViewFile = it },
                                        { currentPath = it.uri.toString() }
                                    )
                                },
                                onLongClick = {
                                    handleLongClick(file, onInsertCode) { insertTarget = it }
                                },
                                onRename = {
                                    renameTarget = file
                                    renameNewName = file.name
                                },
                                onDelete = {
                                    deleteTarget = file
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 58.dp),
                                thickness = 0.4.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.06f)
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(100.dp),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(files, key = { it.uri.toString() }) { file ->
                            FileGridItem(
                                file = file,
                                onClick = {
                                    handleFileClick(
                                        file, onOpenFile,
                                        { imageViewFile = it },
                                        { audioViewFile = it },
                                        { videoViewFile = it },
                                        { currentPath = it.uri.toString() }
                                    )
                                },
                                onLongClick = {
                                    handleLongClick(file, onInsertCode) { insertTarget = it }
                                },
                                onRename = {
                                    renameTarget = file
                                    renameNewName = file.name
                                },
                                onDelete = {
                                    deleteTarget = file
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Dialogs ──────────────────────────────────────────────────────

    imageViewFile?.let { img ->
        Dialog(onDismissRequest = { imageViewFile = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(Modifier.fillMaxSize().background(Color.Black).clickable { imageViewFile = null }) {
                AsyncImage(model = img.uri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                Surface(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(), color = Color.Black.copy(0.6f)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(img.name, color = Color.White, fontSize = 14.sp)
                            Text(formatFileSize(img.size), color = Color.White.copy(0.6f), fontSize = 11.sp)
                        }
                        IconButton(onClick = { imageViewFile = null }) { Icon(Icons.Rounded.Close, null, tint = Color.White) }
                    }
                }
            }
        }
    }

    audioViewFile?.let { mFile -> MediaPlayerDialog(file = mFile, onDismiss = { audioViewFile = null }) }
    videoViewFile?.let { vFile -> VideoPlayerDialog(file = vFile, onDismiss = { videoViewFile = null }) }

    insertTarget?.let { (file, mode) ->
        InsertCodeDialog(file = file, mode = mode, onInsert = { tag -> onInsertCode?.invoke(tag, mode); insertTarget = null }, onDismiss = { insertTarget = null })
    }

    if (showNewDialog) {
        AlertDialog(
            onDismissRequest = { showNewDialog = false; newName = ""; newNameError = "" },
            title = { Text(if (newIsFolder) "Buat Folder" else "Buat File") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it; newNameError = "" },
                        label = { Text("Nama") },
                        isError = newNameError.isNotEmpty(),
                        supportingText = if (newNameError.isNotEmpty()) { { Text(newNameError) } } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val trimName = newName.trim()
                    if (trimName.isEmpty()) { newNameError = "Nama kosong"; return@Button }
                    val path = currentPath
                    val isSaf = isSafMode
                    showNewDialog = false
                    newName = ""
                    scope.launch {
                        val ok = withContext(Dispatchers.IO) {
                            runCatching {
                                if (isSaf) {
                                    val parent = DocumentFile.fromTreeUri(context, Uri.parse(path))
                                    if (newIsFolder) parent?.createDirectory(trimName) != null
                                    else parent?.createFile("application/octet-stream", trimName) != null
                                } else {
                                    val target = File(path, trimName)
                                    if (newIsFolder) target.mkdirs() else target.createNewFile()
                                }
                            }.getOrElse { false }
                        }
                        if (ok) refreshKey++ else snackbarHostState.showSnackbar("Gagal membuat")
                    }
                }) { Text("Buat") }
            },
            dismissButton = { TextButton(onClick = { showNewDialog = false }) { Text("Batal") } }
        )
    }

    renameTarget?.let { file ->
        AlertDialog(
            onDismissRequest = { renameTarget = null; renameNewName = ""; renameError = "" },
            title = { Text("Ubah Nama") },
            text = {
                OutlinedTextField(
                    value = renameNewName,
                    onValueChange = { renameNewName = it; renameError = "" },
                    label = { Text("Nama Baru") },
                    isError = renameError.isNotEmpty(),
                    supportingText = if (renameError.isNotEmpty()) { { Text(renameError) } } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    val trimName = renameNewName.trim()
                    if (trimName.isEmpty()) { renameError = "Nama tidak boleh kosong"; return@Button }
                    if (trimName == file.name) { renameTarget = null; return@Button }
                    val isSaf = isSafMode
                    val targetFile = file
                    renameTarget = null
                    scope.launch {
                        val ok = withContext(Dispatchers.IO) {
                            runCatching {
                                if (isSaf) {
                                    val doc = DocumentFile.fromSingleUri(context, targetFile.uri)
                                    doc?.renameTo(trimName) == true
                                } else {
                                    val f = File(targetFile.uri.path ?: "")
                                    val dest = File(f.parentFile, trimName)
                                    f.renameTo(dest)
                                }
                            }.getOrElse { false }
                        }
                        if (ok) {
                            refreshKey++
                            snackbarHostState.showSnackbar("Berhasil mengubah nama")
                        } else {
                            snackbarHostState.showSnackbar("Gagal mengubah nama")
                        }
                        renameNewName = ""
                    }
                }) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { renameTarget = null }) { Text("Batal") } }
        )
    }

    deleteTarget?.let { file ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Hapus ${if (file.isDirectory) "Folder" else "File"}") },
            text = { Text("Apakah Anda yakin ingin menghapus '${file.name}'? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                Button(
                    onClick = {
                        val isSaf = isSafMode
                        val targetFile = file
                        deleteTarget = null
                        scope.launch {
                            val ok = withContext(Dispatchers.IO) {
                                runCatching {
                                    if (isSaf) {
                                        val doc = DocumentFile.fromSingleUri(context, targetFile.uri)
                                        doc?.delete() == true
                                    } else {
                                        val f = File(targetFile.uri.path ?: "")
                                        if (f.isDirectory) f.deleteRecursively() else f.delete()
                                    }
                                }.getOrElse { false }
                            }
                            if (ok) {
                                refreshKey++
                                snackbarHostState.showSnackbar("Berhasil menghapus")
                            } else {
                                snackbarHostState.showSnackbar("Gagal menghapus")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Hapus", color = MaterialTheme.colorScheme.onError) }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Batal") } }
        )
    }

    Box(Modifier.fillMaxSize(), Alignment.BottomCenter) { SnackbarHost(snackbarHostState) }
}

// ── Helpers ───────────────────────────────────────────────────────

private fun handleFileClick(
    file: FileEntry, 
    onOpenFile: (Uri) -> Unit, 
    showImg: (FileEntry) -> Unit, 
    showAudio: (FileEntry) -> Unit, 
    showVideo: (FileEntry) -> Unit,
    openFolder: (FileEntry) -> Unit
) {
    if (file.isDirectory) openFolder(file)
    else {
        val ext = file.extension.lowercase()
        when {
            ext in listOf("jpg","jpeg","png","gif","webp") -> showImg(file)
            ext in listOf("mp3","wav","m4a","ogg") -> showAudio(file)
            ext in listOf("mp4","mkv","webm","3gp") -> showVideo(file)
            else -> onOpenFile(file.uri)
        }
    }
}

private fun handleLongClick(file: FileEntry, onInsert: ((String, InsertMode) -> Unit)?, setTarget: (Pair<FileEntry, InsertMode>) -> Unit) {
    if (onInsert == null || file.isDirectory) return
    val mode = when (file.extension.lowercase()) {
        in listOf("jpg","jpeg","png") -> InsertMode.IMAGE
        in listOf("mp3","wav") -> InsertMode.AUDIO
        in listOf("mp4","mkv") -> InsertMode.VIDEO
        else -> return
    }
    setTarget(file to mode)
}

@Composable
fun getFileIconColor(file: FileEntry): Pair<ImageVector, Color> {
    if (file.isDirectory) return Icons.Rounded.Folder to Color(0xFFFFD54F)
    return when (file.extension.lowercase()) {
        "kt","java","py","js","html","css" -> Icons.Rounded.Code to Color(0xFF2196F3)
        "jpg","png","jpeg" -> Icons.Rounded.Image to Color(0xFFE91E63)
        "mp3","wav" -> Icons.Rounded.MusicNote to Color(0xFF9C27B0)
        "mp4","mkv" -> Icons.Rounded.VideoLibrary to Color(0xFFF44336)
        else -> Icons.Rounded.Description to Color(0xFF90A4AE)
    }
}

fun formatFileSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes/1024} KB"
    else -> "${bytes/(1024*1024)} MB"
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileListItem(
    file: FileEntry, 
    onClick: () -> Unit, 
    onLongClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val (icon, color) = getFileIconColor(file)
    Row(
        Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(40.dp).background(color.copy(0.1f), RoundedCornerShape(8.dp)), Alignment.Center) { Icon(icon, null, tint=color) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(file.name, fontSize=14.sp, fontWeight=FontWeight.Medium, maxLines=1, overflow=TextOverflow.Ellipsis)
            Text(if (file.isDirectory) "Folder" else formatFileSize(file.size), fontSize=11.sp, color=Color.Gray)
        }
        Row {
            IconButton(onClick = onRename, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.Edit, "Edit", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary.copy(0.7f))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.Delete, "Hapus", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error.copy(0.7f))
            }
            if (file.isDirectory) {
                Icon(Icons.Rounded.ChevronRight, null, tint=Color.Gray, modifier = Modifier.align(Alignment.CenterVertically))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileGridItem(
    file: FileEntry, 
    onClick: () -> Unit, 
    onLongClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val (icon, color) = getFileIconColor(file)
    Card(
        Modifier.combinedClickable(onClick=onClick, onLongClick=onLongClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f))
    ) {
        Box(Modifier.padding(8.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(icon, null, Modifier.size(42.dp), tint=color)
                Spacer(Modifier.height(4.dp))
                Text(file.name, fontSize=11.sp, maxLines=1, overflow=TextOverflow.Ellipsis, textAlign = TextAlign.Center)
            }
            Row(Modifier.align(Alignment.TopEnd)) {
                Box(Modifier.size(24.dp).clickable { onRename() }, Alignment.Center) {
                    Icon(Icons.Rounded.Edit, null, Modifier.size(14.dp), MaterialTheme.colorScheme.primary)
                }
                Box(Modifier.size(24.dp).clickable { onDelete() }, Alignment.Center) {
                    Icon(Icons.Rounded.Delete, null, Modifier.size(14.dp), MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun MediaPlayerDialog(file: FileEntry, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    
    DisposableEffect(Unit) {
        val mp = MediaPlayer().apply {
            setDataSource(context, file.uri)
            prepare()
            start()
            setOnCompletionListener { isPlaying = false }
        }
        mediaPlayer = mp
        isPlaying = true
        onDispose { 
            mp.stop()
            mp.release() 
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), tonalElevation = 6.dp) {
            Column(Modifier.padding(24.dp).width(280.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.MusicNote, null, Modifier.size(64.dp), MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                Text(file.name, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("Memutar Audio", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FilledIconButton(onClick = { if (isPlaying) mediaPlayer?.pause() else mediaPlayer?.start(); isPlaying = !isPlaying }, modifier = Modifier.size(56.dp)) {
                        Icon(if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null)
                    }
                }
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Tutup") }
            }
        }
    }
}

@Composable
fun VideoPlayerDialog(file: FileEntry, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(file.uri))
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            AndroidView(factory = { ctx -> PlayerView(ctx).apply { player = exoPlayer; useController = true } }, modifier = Modifier.fillMaxSize())
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(0.4f), RoundedCornerShape(50))) {
                Icon(Icons.Rounded.Close, null, tint = Color.White)
            }
            Text(file.name, color = Color.White, fontSize = 12.sp, modifier = Modifier.align(Alignment.TopStart).padding(16.dp).background(Color.Black.copy(0.4f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp))
        }
    }
}

@Composable
fun InsertCodeDialog(file: FileEntry, mode: InsertMode, onInsert: (String) -> Unit, onDismiss: () -> Unit) {
    val path = file.uri.toString()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sisipkan Link") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val options = when (mode) {
                    InsertMode.IMAGE -> listOf(
                        "HTML" to "<img src='$path' alt='${file.name}' style='max-width:100%'>",
                        "Markdown" to "![${file.name}]($path)"
                    )
                    InsertMode.AUDIO -> listOf(
                        "HTML" to "<audio controls src='$path'>Browser tidak mendukung audio.</audio>",
                        "Markdown" to "[🎵 ${file.name}]($path)"
                    )
                    InsertMode.VIDEO -> listOf(
                        "HTML" to "<video controls src='$path' style='max-width:100%'>Browser tidak mendukung video.</video>",
                        "Markdown" to "[🎥 ${file.name}]($path)"
                    )
                }
                
                options.forEach { (label, code) ->
                    Column {
                        Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Surface(
                            onClick = { onInsert(code) },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(code, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}