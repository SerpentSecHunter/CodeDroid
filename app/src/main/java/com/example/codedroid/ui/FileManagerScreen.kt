package com.example.codedroid.ui

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class InsertMode { IMAGE, AUDIO, VIDEO }

@Composable
fun FileManagerScreen(
    onOpenFile  : (File) -> Unit,
    onInsertCode: ((String, InsertMode) -> Unit)? = null
) {
    val context      = LocalContext.current
    val scope        = rememberCoroutineScope()

    var currentPath  by remember { mutableStateOf(getDefaultPath()) }
    var files        by remember { mutableStateOf<List<File>>(emptyList()) }
    var isLoading    by remember { mutableStateOf(false) }
    var viewMode     by remember { mutableStateOf("list") } // list or grid
    var searchQuery  by remember { mutableStateOf("") }
    var showSearch   by remember { mutableStateOf(false) }
    var showNewDialog by remember { mutableStateOf(false) }
    var newIsFolder  by remember { mutableStateOf(false) }
    var newName      by remember { mutableStateOf("") }

    // Media viewer states
    var imageViewFile  by remember { mutableStateOf<File?>(null) }
    var mediaPlayFile  by remember { mutableStateOf<File?>(null) }
    var insertTarget   by remember { mutableStateOf<Pair<File, InsertMode>?>(null) }

    // Storage roots
    val storageRoots = remember { getStorageRoots(context) }

    LaunchedEffect(currentPath) {
        isLoading = true
        files = withContext(Dispatchers.IO) {
            runCatching {
                File(currentPath).listFiles()
                    ?.filter { if (searchQuery.isBlank()) true else it.name.contains(searchQuery, true) }
                    ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                    ?: emptyList()
            }.getOrDefault(emptyList())
        }
        isLoading = false
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            files = withContext(Dispatchers.IO) {
                runCatching {
                    File(currentPath).listFiles()
                        ?.filter { it.name.contains(searchQuery, true) }
                        ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                        ?: emptyList()
                }.getOrDefault(emptyList())
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Top bar
        Surface(tonalElevation = 2.dp) {
            Column {
                Row(
                    modifier          = Modifier.fillMaxWidth()
                        .padding(start = 4.dp, end = 8.dp, top = 6.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val parent = File(currentPath).parentFile
                        if (parent != null && parent.canRead()) currentPath = parent.absolutePath
                    }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Kembali") }

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
                        Text(
                            text     = File(currentPath).name.ifEmpty { "Storage" },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(onClick = { showSearch = true }) {
                            Icon(Icons.Rounded.Search, "Cari")
                        }
                        IconButton(onClick = { viewMode = if (viewMode == "list") "grid" else "list" }) {
                            Icon(if (viewMode == "list") Icons.Rounded.GridView else Icons.Rounded.ViewList, "Mode")
                        }
                        IconButton(onClick = { newIsFolder = false; showNewDialog = true }) {
                            Icon(Icons.Rounded.Add, "Baru", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                // Path breadcrumb
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                        .padding(start = 16.dp, bottom = 6.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    storageRoots.forEach { root ->
                        SuggestionChip(
                            onClick  = { currentPath = root.first },
                            label    = { Text(root.second, fontSize = 10.sp) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
            }
        }

        HorizontalDivider()

        if (isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (files.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Folder kosong", color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
            }
        } else {
            if (viewMode == "grid") {
                LazyVerticalGrid(
                    columns        = GridCells.Adaptive(100.dp),
                    modifier       = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement   = Arrangement.spacedBy(8.dp)
                ) {
                    items(files, key = { it.absolutePath }) { file ->
                        FileGridItem(
                            file        = file,
                            onClick     = { onClickFile(file, onOpenFile, { imageViewFile = it }, { mediaPlayFile = it }) },
                            onLongClick = { handleLongClick(file, onInsertCode, { insertTarget = it }) }
                        )
                    }
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(files, key = { it.absolutePath }) { file ->
                        FileListItem(
                            file        = file,
                            onClick     = { onClickFile(file, onOpenFile, { imageViewFile = it }, { mediaPlayFile = it }) },
                            onLongClick = { handleLongClick(file, onInsertCode, { insertTarget = it }) }
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

    // Image viewer dialog
    imageViewFile?.let { imgFile ->
        Dialog(
            onDismissRequest = { imageViewFile = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                AsyncImage(
                    model             = imgFile,
                    contentDescription= imgFile.name,
                    modifier          = Modifier.fillMaxSize(),
                    contentScale      = ContentScale.Fit
                )
                IconButton(
                    onClick  = { imageViewFile = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                        .background(Color.Black.copy(0.5f), RoundedCornerShape(50))
                ) { Icon(Icons.Rounded.Close, null, tint = Color.White) }
                Text(
                    imgFile.name, color = Color.White, fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                        .background(Color.Black.copy(0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }

    // Media player dialog
    mediaPlayFile?.let { mFile ->
        MediaPlayerDialog(file = mFile, onDismiss = { mediaPlayFile = null })
    }

    // Insert target dialog
    insertTarget?.let { (file, mode) ->
        InsertCodeDialog(
            file   = file,
            mode   = mode,
            onInsert = { tag ->
                onInsertCode?.invoke(tag, mode)
                insertTarget = null
            },
            onDismiss = { insertTarget = null }
        )
    }

    // New file/folder dialog
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
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("kt", "py", "html", "css", "js", "txt", "java").forEach { ext ->
                                SuggestionChip(onClick = {
                                    if (!newName.contains('.')) newName += ".$ext"
                                }, label = { Text(".$ext", fontSize = 10.sp) })
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
                                val tmp = currentPath; currentPath = ""; currentPath = tmp
                                showNewDialog = false; newName = ""
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

// ── Helper functions ──────────────────────────────────────────────────────────

private fun getDefaultPath(): String =
    Environment.getExternalStorageDirectory().absolutePath

private fun getStorageRoots(context: Context): List<Pair<String, String>> {
    val roots = mutableListOf<Pair<String, String>>()
    roots.add(Environment.getExternalStorageDirectory().absolutePath to "Internal")
    context.getExternalFilesDirs(null).forEachIndexed { i, f ->
        if (i > 0 && f != null) {
            val sdPath = f.absolutePath.split("/Android")[0]
            roots.add(sdPath to "SD Card")
        }
    }
    return roots
}

private fun onClickFile(
    file       : File,
    onOpenFile : (File) -> Unit,
    showImage  : (File) -> Unit,
    showMedia  : (File) -> Unit
) {
    if (file.isDirectory) return
    val ext = file.extension.lowercase()
    when {
        ext in listOf("jpg","jpeg","png","gif","webp","bmp") -> showImage(file)
        ext in listOf("mp3","wav","ogg","m4a","aac","flac") -> showMedia(file)
        ext in listOf("mp4","mkv","avi","mov","webm","3gp") -> showMedia(file)
        else -> onOpenFile(file)
    }
}

private fun handleLongClick(
    file       : File,
    onInsert   : ((String, InsertMode) -> Unit)?,
    setInsert  : (Pair<File, InsertMode>) -> Unit
) {
    if (onInsert == null) return
    val ext = file.extension.lowercase()
    val mode = when {
        ext in listOf("jpg","jpeg","png","gif","webp","bmp") -> InsertMode.IMAGE
        ext in listOf("mp3","wav","ogg","m4a","aac","flac") -> InsertMode.AUDIO
        ext in listOf("mp4","mkv","avi","mov","webm","3gp") -> InsertMode.VIDEO
        else -> return
    }
    setInsert(file to mode)
}

// ── FileListItem ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileListItem(file: File, onClick: () -> Unit, onLongClick: () -> Unit) {
    val ext  = file.extension.lowercase()
    val (icon, color) = fileIconAndColor(file)

    Row(
        modifier = Modifier.fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail for images
        if (ext in listOf("jpg","jpeg","png","gif","webp","bmp")) {
            AsyncImage(
                model  = file,
                contentDescription = null,
                modifier= Modifier.size(42.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier         = Modifier.size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
        }

        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(file.name, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    if (file.isDirectory) "${file.listFiles()?.size ?: 0} item"
                    else formatFileSize(file.length()),
                    fontSize = 11.sp,
                    color    = MaterialTheme.colorScheme.onSurface.copy(0.45f)
                )
        }

        // Badge untuk media
        if (ext in listOf("mp3","wav","ogg","m4a","mp4","mkv","avi","mov")) {
            Icon(Icons.Rounded.PlayCircle, null, tint = color.copy(0.6f),
                modifier = Modifier.size(18.dp))
        }
    }
}

// ── FileGridItem ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileGridItem(file: File, onClick: () -> Unit, onLongClick: () -> Unit) {
    val ext  = file.extension.lowercase()
    val (icon, color) = fileIconAndColor(file)

    Card(
        modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape    = RoundedCornerShape(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (ext in listOf("jpg","jpeg","png","gif","webp","bmp")) {
                AsyncImage(
                    model  = file,
                    contentDescription = null,
                    modifier= Modifier.fillMaxWidth().height(80.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier         = Modifier.fillMaxWidth().height(80.dp)
                        .background(color.copy(0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
                }
            }
            Text(
                file.name, fontSize = 10.sp, maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}

// ── fileIconAndColor ──────────────────────────────────────────────────────────

@Composable
private fun fileIconAndColor(file: File): Pair<androidx.compose.ui.graphics.vector.ImageVector, Color> {
    if (file.isDirectory) return Icons.Rounded.Folder to Color(0xFFFFD54F)
    return when (file.extension.lowercase()) {
        "kt","kts"        -> Icons.Rounded.Code          to Color(0xFF7C4DFF)
        "java"            -> Icons.Rounded.Code          to Color(0xFFFF6D00)
        "py"              -> Icons.Rounded.Code          to Color(0xFF00BCD4)
        "html","htm"      -> Icons.Rounded.Code          to Color(0xFFE64A19)
        "css","scss"      -> Icons.Rounded.Palette         to Color(0xFF1565C0)
        "js","ts"         -> Icons.Rounded.Code    to Color(0xFFF9A825)
        "json"            -> Icons.Rounded.Code    to Color(0xFF558B2F)
        "xml"             -> Icons.Rounded.Code          to Color(0xFF00897B)
        "md"              -> Icons.Rounded.Description   to Color(0xFF6D4C41)
        "txt"             -> Icons.Rounded.Description   to Color(0xFF607D8B)
        "pdf"             -> Icons.Rounded.Description  to Color(0xFFD32F2F)
        "zip","rar","7z","tar","gz" -> Icons.Rounded.Folder to Color(0xFF795548)
        "jpg","jpeg","png","gif","webp","bmp" -> Icons.Rounded.Image to Color(0xFF00ACC1)
        "mp3","wav","ogg","m4a","aac","flac" -> Icons.Rounded.MusicNote to Color(0xFF7B1FA2)
        "mp4","mkv","avi","mov","webm","3gp" -> Icons.Rounded.VideoLibrary to Color(0xFFC62828)
        "apk"             -> Icons.Rounded.PhoneAndroid       to Color(0xFF4CAF50)
        else              -> Icons.Rounded.Description to Color(0xFF90A4AE)
    }
}

// ── MediaPlayerDialog ─────────────────────────────────────────────────────────

@Composable
private fun MediaPlayerDialog(file: File, onDismiss: () -> Unit) {
    val ext = file.extension.lowercase()
    val isVideo = ext in listOf("mp4","mkv","avi","mov","webm","3gp")
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var progress by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(0) }

    LaunchedEffect(file) {
        if (!isVideo) {
            try {
                val mp = MediaPlayer().apply {
                    setDataSource(file.absolutePath)
                    prepare()
                }
                mediaPlayer = mp
                duration = mp.duration
                mp.start()
                isPlaying = true
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
            modifier = Modifier.fillMaxWidth(0.95f),
            shape    = RoundedCornerShape(20.dp),
            color    = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier            = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text(file.name, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    IconButton(onClick = { mediaPlayer?.release(); onDismiss() }) {
                        Icon(Icons.Rounded.Close, null)
                    }
                }
                Box(
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isVideo) Icons.Rounded.VideoLibrary else Icons.Rounded.MusicNote,
                        null, tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    if (!isVideo) {
                        // Waveform visual placeholder
                        Text("♪ Playing...", color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp))
                    }
                }
                // Controls (audio only — video needs VideoView)
                if (!isVideo) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            mediaPlayer?.seekTo(0)
                        }) { Icon(Icons.Rounded.SkipPrevious, null, Modifier.size(28.dp)) }
                        FilledIconButton(
                            onClick = {
                                if (isPlaying) { mediaPlayer?.pause(); isPlaying = false }
                                else { mediaPlayer?.start(); isPlaying = true }
                            },
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                null, Modifier.size(28.dp))
                        }
                        IconButton(onClick = {
                            mediaPlayer?.seekTo((mediaPlayer?.duration ?: 0) - 1)
                        }) { Icon(Icons.Rounded.SkipNext, null, Modifier.size(28.dp)) }
                    }
                }
                if (isVideo) {
                    Text("Video playback — gunakan media player eksternal",
                        fontSize = 12.sp,
                        color    = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    OutlinedButton(onClick = {
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                setDataAndType(Uri.fromFile(file), "video/*")
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }) { Text("Buka di Media Player") }
                }
            }
        }
    }
}

// ── InsertCodeDialog ──────────────────────────────────────────────────────────

@Composable
private fun InsertCodeDialog(
    file     : File,
    mode     : InsertMode,
    onInsert : (String) -> Unit,
    onDismiss: () -> Unit
) {
    val path = file.absolutePath
    val suggestions = when (mode) {
        InsertMode.IMAGE -> listOf(
            "<img src=\"$path\" />" to "HTML img tag",
            "![${file.name}]($path)" to "Markdown image",
            "background: url('$path');" to "CSS background",
            "\"imagePath\": \"$path\"" to "JSON property"
        )
        InsertMode.AUDIO -> listOf(
            "<audio src=\"$path\" controls></audio>" to "HTML audio",
            "MediaPlayer.create(context, Uri.parse(\"$path\"))" to "Android MediaPlayer",
            "\"audioPath\": \"$path\"" to "JSON property"
        )
        InsertMode.VIDEO -> listOf(
            "<video src=\"$path\" controls></video>" to "HTML video",
            "Uri.parse(\"$path\")" to "Android Uri",
            "\"videoPath\": \"$path\"" to "JSON property"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AddCircle, null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sisipkan ke Editor", fontSize = 15.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Pilih format kode untuk: ${file.name}", fontSize = 12.sp,
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
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

fun formatFileSize(bytes: Long): String = when {
    bytes < 1_024L         -> "$bytes B"
    bytes < 1_048_576L     -> "%.1f KB".format(bytes / 1_024.0)
    bytes < 1_073_741_824L -> "%.1f MB".format(bytes / 1_048_576.0)
    else                   -> "%.2f GB".format(bytes / 1_073_741_824.0)
}
