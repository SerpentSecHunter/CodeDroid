package com.example.codedroid.ui

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

enum class InsertMode { IMAGE, AUDIO, VIDEO }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileManagerScreen(
    onOpenFile  : (File) -> Unit,
    onInsertCode: ((String, InsertMode) -> Unit)? = null
) {
    val context     = LocalContext.current
    val scope       = rememberCoroutineScope()

    var currentPath by remember { mutableStateOf(
        Environment.getExternalStorageDirectory().absolutePath) }
    var files       by remember { mutableStateOf<List<File>>(emptyList()) }
    var isLoading   by remember { mutableStateOf(false) }
    var viewMode    by remember { mutableStateOf("list") }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch  by remember { mutableStateOf(false) }
    var showNewDialog by remember { mutableStateOf(false) }
    var newIsFolder by remember { mutableStateOf(false) }
    var newName     by remember { mutableStateOf("") }

    // Media viewer states
    var imageViewFile by remember { mutableStateOf<File?>(null) }
    var mediaPlayFile by remember { mutableStateOf<File?>(null) }
    var insertTarget  by remember { mutableStateOf<Pair<File, InsertMode>?>(null) }

    // Storage roots
    val storageRoots = remember { getStorageRoots(context) }

    // Load files setiap kali currentPath berubah
    fun loadFiles() {
        scope.launch {
            isLoading = true
            files = withContext(Dispatchers.IO) {
                runCatching {
                    File(currentPath).listFiles()
                        ?.filter {
                            if (searchQuery.isBlank()) true
                            else it.name.contains(searchQuery, ignoreCase = true)
                        }
                        ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                        ?: emptyList()
                }.getOrDefault(emptyList())
            }
            isLoading = false
        }
    }

    LaunchedEffect(currentPath) { loadFiles() }
    LaunchedEffect(searchQuery) { loadFiles() }

    Column(Modifier.fillMaxSize()) {
        // ── Top bar ──────────────────────────────────────────────
        Surface(tonalElevation = 2.dp) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val parent = File(currentPath).parentFile
                        if (parent != null && parent.canRead()) {
                            currentPath = parent.absolutePath
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Kembali")
                    }

                    if (showSearch) {
                        OutlinedTextField(
                            value         = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder   = { Text("Cari file...") },
                            singleLine    = true,
                            modifier      = Modifier.weight(1f).height(46.dp),
                            shape         = RoundedCornerShape(12.dp)
                        )
                        IconButton(onClick = { showSearch = false; searchQuery = "" }) {
                            Icon(Icons.Rounded.Close, "Tutup")
                        }
                    } else {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = File(currentPath).name.ifEmpty { "Storage" },
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines   = 1,
                                overflow   = TextOverflow.Ellipsis
                            )
                            Text(
                                text  = currentPath,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.4f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { showSearch = true }) {
                            Icon(Icons.Rounded.Search, "Cari")
                        }
                        IconButton(onClick = {
                            viewMode = if (viewMode == "list") "grid" else "list"
                        }) {
                            Icon(
                                if (viewMode == "list") Icons.Rounded.GridView
                                else Icons.Rounded.List,
                                "Mode"
                            )
                        }
                        IconButton(onClick = { newIsFolder = false; showNewDialog = true }) {
                            Icon(Icons.Rounded.Add, "Baru",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { newIsFolder = true; showNewDialog = true }) {
                            Icon(Icons.Rounded.CreateNewFolder, "Folder Baru",
                                tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }

                // Storage root chips
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(start = 12.dp, bottom = 6.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    storageRoots.forEach { (path, label) ->
                        val isActive = currentPath.startsWith(path)
                        FilterChip(
                            selected = isActive,
                            onClick  = { currentPath = path },
                            label    = { Text(label, fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    if (label == "Internal") Icons.Rounded.PhoneAndroid
                                    else Icons.Rounded.SdCard,
                                    null, Modifier.size(14.dp)
                                )
                            }
                        )
                    }
                }
            }
        }

        HorizontalDivider()

        // ── Content ───────────────────────────────────────────────
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            files.isEmpty() -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.FolderOpen, null,
                            modifier = Modifier.size(48.dp),
                            tint     = MaterialTheme.colorScheme.onSurface.copy(0.2f))
                        Spacer(Modifier.height(8.dp))
                        Text("Folder kosong",
                            color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                    }
                }
            }
            viewMode == "grid" -> {
                LazyVerticalGrid(
                    columns        = GridCells.Adaptive(100.dp),
                    modifier       = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement   = Arrangement.spacedBy(8.dp)
                ) {
                    items(files, key = { it.absolutePath }) { file ->
                        FileGridItem(
                            file = file,
                            onClick = {
                                handleFileClick(
                                    file, onOpenFile,
                                    { imageViewFile = it },
                                    { mediaPlayFile = it },
                                    { currentPath  = it.absolutePath }
                                )
                            },
                            onLongClick = {
                                handleLongClick(file, onInsertCode) { insertTarget = it }
                            }
                        )
                    }
                }
            }
            else -> {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(files, key = { it.absolutePath }) { file ->
                        FileListItem(
                            file = file,
                            onClick = {
                                handleFileClick(
                                    file, onOpenFile,
                                    { imageViewFile = it },
                                    { mediaPlayFile = it },
                                    { currentPath  = it.absolutePath }
                                )
                            },
                            onLongClick = {
                                handleLongClick(file, onInsertCode) { insertTarget = it }
                            }
                        )
                        HorizontalDivider(
                            modifier  = Modifier.padding(start = 58.dp),
                            thickness = 0.4.dp,
                            color     = MaterialTheme.colorScheme.onSurface.copy(0.06f)
                        )
                    }
                }
            }
        }
    }

    // ── Image Viewer ──────────────────────────────────────────────
    imageViewFile?.let { img ->
        Dialog(
            onDismissRequest = { imageViewFile = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(Modifier.fillMaxSize().background(Color.Black).clickable { imageViewFile = null }) {
                AsyncImage(
                    model              = img,
                    contentDescription = img.name,
                    modifier           = Modifier.fillMaxSize(),
                    contentScale       = ContentScale.Fit
                )
                // Info bar
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                    color    = Color.Black.copy(0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(img.name, color = Color.White, fontSize = 13.sp,
                                fontWeight = FontWeight.Medium)
                            Text(formatFileSize(img.length()), color = Color.White.copy(0.6f),
                                fontSize = 11.sp)
                        }
                        IconButton(onClick = { imageViewFile = null }) {
                            Icon(Icons.Rounded.Close, null, tint = Color.White)
                        }
                    }
                }
            }
        }
    }

    // ── Media Player ──────────────────────────────────────────────
    mediaPlayFile?.let { mFile ->
        MediaPlayerDialog(file = mFile, onDismiss = { mediaPlayFile = null })
    }

    // ── Insert Code Dialog ────────────────────────────────────────
    insertTarget?.let { (file, mode) ->
        InsertCodeDialog(
            file      = file,
            mode      = mode,
            onInsert  = { tag -> onInsertCode?.invoke(tag, mode); insertTarget = null },
            onDismiss = { insertTarget = null }
        )
    }

    // ── New File/Folder Dialog ─────────────────────────────────────
    if (showNewDialog) {
        AlertDialog(
            onDismissRequest = { showNewDialog = false; newName = "" },
            title = { Text(if (newIsFolder) "Folder Baru" else "File Baru") },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value         = newName,
                        onValueChange = { newName = it },
                        label         = { Text("Nama") },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth()
                    )
                    if (!newIsFolder) {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("kt","py","html","css","js","php","java","txt","json","xml","md").forEach { ext ->
                                SuggestionChip(
                                    onClick = { newName = newName.substringBefore('.') + ".$ext" },
                                    label   = { Text(".$ext", fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotBlank()) {
                        scope.launch(Dispatchers.IO) {
                            val f = File(currentPath, newName)
                            if (newIsFolder) f.mkdirs()
                            else { f.parentFile?.mkdirs(); f.createNewFile() }
                            withContext(Dispatchers.Main) {
                                if (!newIsFolder) onOpenFile(f)
                                showNewDialog = false
                                newName = ""
                                loadFiles()
                            }
                        }
                    }
                }) { Text("Buat") }
            },
            dismissButton = {
                TextButton(onClick = { showNewDialog = false; newName = "" }) { Text("Batal") }
            }
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────

private fun getStorageRoots(context: Context): List<Pair<String, String>> {
    val roots = mutableListOf<Pair<String, String>>()
    roots.add(Environment.getExternalStorageDirectory().absolutePath to "Internal")
    try {
        context.getExternalFilesDirs(null).forEachIndexed { i, f ->
            if (i > 0 && f != null) {
                val sdPath = f.absolutePath.split("/Android")[0]
                if (File(sdPath).exists()) roots.add(sdPath to "SD Card")
            }
        }
    } catch (_: Exception) {}
    return roots
}

private fun handleFileClick(
    file      : File,
    onOpenFile: (File) -> Unit,
    showImage : (File) -> Unit,
    showMedia : (File) -> Unit,
    openFolder: (File) -> Unit
) {
    when {
        file.isDirectory -> openFolder(file)  // ← MASUK KE FOLDER
        file.extension.lowercase() in listOf("jpg","jpeg","png","gif","webp","bmp") -> showImage(file)
        file.extension.lowercase() in listOf("mp3","wav","ogg","m4a","aac","flac") -> showMedia(file)
        file.extension.lowercase() in listOf("mp4","mkv","avi","mov","webm","3gp") -> showMedia(file)
        else -> onOpenFile(file)
    }
}

private fun handleLongClick(
    file     : File,
    onInsert : ((String, InsertMode) -> Unit)?,
    setTarget: (Pair<File, InsertMode>) -> Unit
) {
    if (onInsert == null || file.isDirectory) return
    val mode = when (file.extension.lowercase()) {
        in listOf("jpg","jpeg","png","gif","webp","bmp") -> InsertMode.IMAGE
        in listOf("mp3","wav","ogg","m4a","aac","flac") -> InsertMode.AUDIO
        in listOf("mp4","mkv","avi","mov","webm","3gp") -> InsertMode.VIDEO
        else -> return
    }
    setTarget(file to mode)
}

// ── FileListItem ──────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileListItem(file: File, onClick: () -> Unit, onLongClick: () -> Unit) {
    val ext = file.extension.lowercase()
    val (icon, color) = getFileIconColor(file)
    val isImage = ext in listOf("jpg","jpeg","png","gif","webp","bmp")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail / icon
        if (isImage) {
            AsyncImage(
                model             = file,
                contentDescription= null,
                modifier          = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)),
                contentScale      = ContentScale.Crop
            )
        } else {
            Box(
                modifier         = Modifier.size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(file.name, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (file.isDirectory) {
                        val count = file.listFiles()?.size ?: 0
                        "$count item"
                    } else formatFileSize(file.length()),
                    fontSize = 11.sp,
                    color    = MaterialTheme.colorScheme.onSurface.copy(0.45f)
                )
                if (!file.isDirectory) {
                    Text(
                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            .format(Date(file.lastModified())),
                        fontSize = 11.sp,
                        color    = MaterialTheme.colorScheme.onSurface.copy(0.3f)
                    )
                }
            }
        }

        // Play icon untuk media
        if (ext in listOf("mp3","wav","ogg","m4a","mp4","mkv","avi")) {
            Icon(Icons.Rounded.PlayCircle, null,
                tint     = color.copy(0.7f),
                modifier = Modifier.size(20.dp).padding(start = 4.dp))
        }
        // Arrow untuk folder
        if (file.isDirectory) {
            Icon(Icons.Rounded.ChevronRight, null,
                tint     = MaterialTheme.colorScheme.onSurface.copy(0.3f),
                modifier = Modifier.size(20.dp))
        }
    }
}

// ── FileGridItem ──────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileGridItem(file: File, onClick: () -> Unit, onLongClick: () -> Unit) {
    val ext = file.extension.lowercase()
    val (icon, color) = getFileIconColor(file)
    val isImage = ext in listOf("jpg","jpeg","png","gif","webp","bmp")

    Card(
        modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape    = RoundedCornerShape(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.fillMaxWidth().height(80.dp)) {
                if (isImage) {
                    AsyncImage(
                        model  = file,
                        contentDescription = null,
                        modifier= Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        Modifier.fillMaxSize().background(color.copy(0.1f)),
                        Alignment.Center
                    ) {
                        Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
                    }
                }
                // Overlay badge
                if (ext in listOf("mp3","wav","ogg","m4a","mp4","mkv")) {
                    Icon(Icons.Rounded.PlayCircle, null,
                        tint     = Color.White.copy(0.9f),
                        modifier = Modifier.size(24.dp).align(Alignment.Center))
                }
            }
            Text(
                file.name, fontSize = 10.sp,
                maxLines = 2, overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}

// ── getFileIconColor ──────────────────────────────────────────────

@Composable
fun getFileIconColor(file: File): Pair<ImageVector, Color> {
    if (file.isDirectory) return Icons.Rounded.Folder to Color(0xFFFFD54F)
    return when (file.extension.lowercase()) {
        "kt","kts"    -> Icons.Rounded.Code          to Color(0xFF7C4DFF)
        "java"        -> Icons.Rounded.Code          to Color(0xFFFF6D00)
        "py"          -> Icons.Rounded.Code          to Color(0xFF00BCD4)
        "html","htm"  -> Icons.Rounded.Code          to Color(0xFFE64A19)
        "css","scss"  -> Icons.Rounded.Palette       to Color(0xFF1565C0)
        "js","ts"     -> Icons.Rounded.Code          to Color(0xFFF9A825)
        "php"         -> Icons.Rounded.Code          to Color(0xFF6A1B9A)
        "json"        -> Icons.Rounded.Code          to Color(0xFF558B2F)
        "xml"         -> Icons.Rounded.Code          to Color(0xFF00897B)
        "md"          -> Icons.Rounded.Description   to Color(0xFF6D4C41)
        "txt"         -> Icons.Rounded.Description   to Color(0xFF607D8B)
        "pdf"         -> Icons.Rounded.Description   to Color(0xFFD32F2F)
        "zip","rar","7z","gz" -> Icons.Rounded.Folder to Color(0xFF795548)
        "jpg","jpeg","png","gif","webp","bmp" -> Icons.Rounded.Image to Color(0xFF00ACC1)
        "mp3","wav","ogg","m4a","aac","flac" -> Icons.Rounded.MusicNote to Color(0xFF7B1FA2)
        "mp4","mkv","avi","mov","webm","3gp" -> Icons.Rounded.VideoLibrary to Color(0xFFC62828)
        "apk"         -> Icons.Rounded.PhoneAndroid  to Color(0xFF4CAF50)
        else          -> Icons.Rounded.Description   to Color(0xFF90A4AE)
    }
}

// ── formatFileSize ────────────────────────────────────────────────

fun formatFileSize(bytes: Long): String = when {
    bytes < 1_024L         -> "$bytes B"
    bytes < 1_048_576L     -> "%.1f KB".format(bytes / 1_024.0)
    bytes < 1_073_741_824L -> "%.1f MB".format(bytes / 1_048_576.0)
    else                   -> "%.2f GB".format(bytes / 1_073_741_824.0)
}

// ── MediaPlayerDialog ─────────────────────────────────────────────

@Composable
fun MediaPlayerDialog(file: File, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val ext     = file.extension.lowercase()
    val isVideo = ext in listOf("mp4","mkv","avi","mov","webm","3gp")
    val isAudio = ext in listOf("mp3","wav","ogg","m4a","aac","flac")

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying   by remember { mutableStateOf(false) }
    var progress    by remember { mutableStateOf(0f) }
    var duration    by remember { mutableStateOf(0) }
    var currentMs   by remember { mutableStateOf(0) }

    LaunchedEffect(file) {
        if (isAudio) {
            try {
                val mp = MediaPlayer().apply {
                    setDataSource(file.absolutePath)
                    prepare()
                    start()
                }
                mediaPlayer = mp
                duration    = mp.duration
                isPlaying   = true
                // Update progress
                while (mp.isPlaying) {
                    currentMs = mp.currentPosition
                    progress  = if (duration > 0) mp.currentPosition.toFloat() / duration else 0f
                    kotlinx.coroutines.delay(500)
                }
            } catch (_: Exception) {}
        }
    }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer?.release() }
    }

    Dialog(
        onDismissRequest = { mediaPlayer?.release(); onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier       = Modifier.fillMaxWidth(0.92f),
            shape          = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier            = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(file.name, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(formatFileSize(file.length()), fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    }
                    IconButton(onClick = { mediaPlayer?.release(); onDismiss() }) {
                        Icon(Icons.Rounded.Close, null)
                    }
                }

                // Art / visual
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isAudio) Color(0xFF7B1FA2).copy(0.1f)
                            else Color(0xFFC62828).copy(0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isAudio) Icons.Rounded.MusicNote else Icons.Rounded.VideoLibrary,
                        null,
                        tint     = if (isAudio) Color(0xFF7B1FA2) else Color(0xFFC62828),
                        modifier = Modifier.size(64.dp)
                    )
                }

                if (isAudio) {
                    // Progress bar
                    Column(Modifier.fillMaxWidth()) {
                        Slider(
                            value         = progress,
                            onValueChange = { v ->
                                progress = v
                                mediaPlayer?.seekTo((v * duration).toInt())
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text(formatMs(currentMs), fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                            Text(formatMs(duration), fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                        }
                    }

                    // Controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { mediaPlayer?.seekTo(0); progress = 0f }) {
                            Icon(Icons.Rounded.SkipPrevious, null, Modifier.size(28.dp))
                        }
                        FilledIconButton(
                            onClick  = {
                                if (isPlaying) { mediaPlayer?.pause(); isPlaying = false }
                                else { mediaPlayer?.start(); isPlaying = true }
                            },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                null, Modifier.size(28.dp)
                            )
                        }
                        IconButton(onClick = {
                            mediaPlayer?.seekTo((mediaPlayer?.duration ?: 0))
                        }) {
                            Icon(Icons.Rounded.SkipNext, null, Modifier.size(28.dp))
                        }
                    }
                }

                if (isVideo) {
                    Text("Buka di media player untuk video",
                        fontSize = 12.sp,
                        color    = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    Button(onClick = {
                        try {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(Uri.fromFile(file), "video/*")
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                            )
                        } catch (_: Exception) {}
                        onDismiss()
                    }) { Text("Buka Media Player") }
                }
            }
        }
    }
}

private fun formatMs(ms: Int): String {
    val s = ms / 1000
    return "%d:%02d".format(s / 60, s % 60)
}

// ── InsertCodeDialog ──────────────────────────────────────────────

@Composable
fun InsertCodeDialog(
    file     : File,
    mode     : InsertMode,
    onInsert : (String) -> Unit,
    onDismiss: () -> Unit
) {
    val path = file.absolutePath
    val suggestions = when (mode) {
        InsertMode.IMAGE -> listOf(
            "<img src=\"$path\" alt=\"${file.name}\" />" to "HTML img",
            "![${file.nameWithoutExtension}]($path)" to "Markdown",
            "background-image: url('$path');" to "CSS background",
            "Uri.parse(\"$path\")" to "Android Uri"
        )
        InsertMode.AUDIO -> listOf(
            "<audio src=\"$path\" controls></audio>" to "HTML audio",
            "MediaPlayer.create(this, Uri.parse(\"$path\"))" to "Android MediaPlayer",
            "\"audio\": \"$path\"" to "JSON"
        )
        InsertMode.VIDEO -> listOf(
            "<video src=\"$path\" controls></video>" to "HTML video",
            "Uri.parse(\"$path\")" to "Android Uri",
            "\"video\": \"$path\"" to "JSON"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Code, null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sisipkan ke Editor")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("File: ${file.name}", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                suggestions.forEach { (code, label) ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onInsert(code) },
                        shape    = RoundedCornerShape(10.dp),
                        colors   = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(4.dp))
                            Text(code, fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines   = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}