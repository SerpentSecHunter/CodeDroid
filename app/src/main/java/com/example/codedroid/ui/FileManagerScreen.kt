package com.example.codedroid.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
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

/**
 * --- 1. DATA MODELS ---
 */
data class ZFile(
    val file: File,
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val extension: String,
    val childCount: Int = 0
)

/**
 * --- 2. STYLE & COLORS (ZArchiver Theme) ---
 */
val ZGreen = Color(0xFF4CAF50)
val ZDarkBg = Color(0xFF0F0F0F)
val ZSurface = Color(0xFF1A1A1A)
val ZOnSurface = Color(0xFFF0F0F0)
val ZFolderColor = Color(0xFFFFCC33)

/**
 * --- 3. MAIN SCREEN ---
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(
    onOpenFile: (Uri) -> Unit,
    onInsertCode: ((String, InsertMode) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val internalPath = remember { Environment.getExternalStorageDirectory().absolutePath }
    var sdCardPaths by remember { mutableStateOf<List<String>>(emptyList()) }
    
    var currentPath by remember { mutableStateOf(internalPath) }
    var allFiles by remember { mutableStateOf<List<ZFile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    
    // Status Manager (ZArchiver Mode)
    var isManager by remember { 
        mutableStateOf(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Environment.isExternalStorageManager() else true) 
    }

    // New Folder / File States
    var showCreateDialog by remember { mutableStateOf(false) }
    var createType by remember { mutableStateOf("file") } // "file" or "folder"
    var newTargetName by remember { mutableStateOf("") }

    // --- Preview States ---
    var previewFile by remember { mutableStateOf<ZFile?>(null) }
    var showPreview by remember { mutableStateOf(false) }

    // File loading function
    fun loadFiles() {
        scope.launch {
            isLoading = true
            allFiles = withContext(Dispatchers.IO) {
                try {
                    val directory = File(currentPath)
                    if (directory.exists() && directory.isDirectory) {
                        directory.listFiles()?.map {
                            ZFile(
                                file = it,
                                name = it.name,
                                isDirectory = it.isDirectory,
                                size = if (it.isDirectory) 0 else it.length(),
                                lastModified = it.lastModified(),
                                extension = it.extension.lowercase(),
                                childCount = if (it.isDirectory) it.listFiles()?.size ?: 0 else 0
                            )
                        }?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                            ?: emptyList()
                    } else emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            }
            isLoading = false
        }
    }

    LaunchedEffect(refreshTrigger, currentPath) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            isManager = Environment.isExternalStorageManager()
        }
        sdCardPaths = getSDCardPathsRobust(context)
        loadFiles()
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(ZSurface).statusBarsPadding()) {
                // Storage Tabs
                Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    ZTab("INTERNAL", isSelected = currentPath.startsWith(internalPath), modifier = Modifier.weight(1f)) { 
                        currentPath = internalPath 
                    }
                    sdCardPaths.forEachIndexed { index, path ->
                        val label = if (sdCardPaths.size > 1) "SD CARD ${index + 1}" else "SD CARD"
                        ZTab(label, isSelected = currentPath.startsWith(path), modifier = Modifier.weight(1f)) {
                            currentPath = path
                        }
                    }
                }
                
                // Toolbar
                Row(
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSearching) {
                        IconButton(onClick = { isSearching = false; searchQuery = "" }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = ZGreen)
                        }
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search...", color = Color.Gray, fontSize = 15.sp) },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                cursorColor = ZGreen,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true
                        )
                    } else {
                        IconButton(onClick = {
                            val parent = File(currentPath).parent
                            if (parent != null && (parent.startsWith(internalPath) || sdCardPaths.any { parent.startsWith(it) })) {
                                currentPath = parent
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = ZGreen)
                        }
                        
                        val displayPath = when {
                            currentPath == internalPath -> "Internal Storage"
                            sdCardPaths.any { currentPath == it } -> "SD Card"
                            currentPath.startsWith(internalPath) -> currentPath.substringAfter(internalPath)
                            else -> sdCardPaths.find { currentPath.startsWith(it) }?.let { currentPath.substringAfter(it) } ?: currentPath
                        }
                        
                        Text(
                            text = displayPath.trimStart('/'),
                            color = ZOnSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, 
                            modifier = Modifier.weight(1f).padding(start = 4.dp),
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        
                        IconButton(onClick = { isSearching = true }) { Icon(Icons.Rounded.Search, null, tint = ZOnSurface) }
                        IconButton(onClick = { refreshTrigger++ }) { Icon(Icons.Rounded.Refresh, null, tint = ZOnSurface) }
                    }
                }
                HorizontalDivider(color = Color.White.copy(0.05f))
            }
        },
        floatingActionButton = {
            if (isManager || Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true; createType = "file" },
                    containerColor = ZGreen,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Rounded.Add, "Create New")
                }
            }
        },
        containerColor = ZDarkBg
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            val displayFiles = if (searchQuery.isBlank()) allFiles else allFiles.filter { it.name.contains(searchQuery, true) }

            if (!isManager && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Setup Permission (ZArchiver Mode)
                Column(
                    modifier = Modifier.fillMaxSize().padding(40.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Rounded.FolderSpecial, null, Modifier.size(70.dp), ZGreen)
                    Spacer(Modifier.height(20.dp))
                    Text("File Access Needed", color = ZOnSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Click OK to show all your folder contents instantly.", color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
                    Spacer(Modifier.height(30.dp))
                    Button(onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                            intent.data = Uri.parse("package:${context.packageName}")
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            context.startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = ZGreen)) {
                        Text("OK", color = Color.White)
                    }
                }
            } else if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = ZGreen)
            } else if (displayFiles.isEmpty()) {
                Text(if(isSearching) "Not found" else "Empty Folder", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(displayFiles, key = { it.file.absolutePath }) { item ->
                        ZFileRowFix(
                            item = item,
                            onClick = {
                                if (item.isDirectory) {
                                    currentPath = item.file.absolutePath
                                } else {
                                    val ext = item.extension
                                    val isMedia = ext in listOf("jpg", "jpeg", "png", "webp", "gif", "mp4", "mkv", "mp3", "wav", "pdf")
                                    if (isMedia) {
                                        previewFile = item
                                        showPreview = true
                                    } else {
                                        onOpenFile(Uri.fromFile(item.file))
                                    }
                                }
                            },
                            onDelete = { it.file.deleteRecursively(); refreshTrigger++ },
                            onRename = { old, new -> File(old.file.parent, new).let { old.file.renameTo(it) }; refreshTrigger++ }
                        )
                    }
                }
            }
        }

        // Preview Dialog
        if (showPreview && previewFile != null) {
            ZFilePreviewDialog(
                fileItem = previewFile!!,
                onDismiss = { showPreview = false; previewFile = null },
                onOpenExternal = { file -> 
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Open with..."))
                }
            )
        }

        // Create Dialog
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                containerColor = ZSurface,
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if(createType == "file") Icons.Rounded.Description else Icons.Rounded.CreateNewFolder, null, tint = ZGreen)
                        Spacer(Modifier.width(8.dp))
                        Text("Create New ${createType.replaceFirstChar { it.uppercase() }}", color = ZOnSurface) 
                    }
                },
                text = {
                    Column {
                        Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            FilterChip(selected = createType == "file", onClick = { createType = "file" }, label = { Text("File") })
                            FilterChip(selected = createType == "folder", onClick = { createType = "folder" }, label = { Text("Folder") })
                        }
                        OutlinedTextField(
                            value = newTargetName,
                            onValueChange = { newTargetName = it },
                            placeholder = { Text(if(createType == "file") "filename.kt" else "Folder Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ZGreen,
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = ZOnSurface,
                                unfocusedTextColor = ZOnSurface
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newTargetName.isNotBlank()) {
                            val newFile = File(currentPath, newTargetName)
                            try {
                                if (createType == "file") newFile.createNewFile()
                                else newFile.mkdir()
                                refreshTrigger++
                                showCreateDialog = false
                                newTargetName = ""
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) { Text("CREATE", color = ZGreen) }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) { Text("CANCEL") }
                }
            )
        }
    }
}

/**
 * --- 4. SUB-COMPONENTS ---
 */
@Composable
fun ZTab(name: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() }
            .background(if (isSelected) ZGreen.copy(alpha = 0.08f) else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            color = if (isSelected) ZGreen else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp
        )
        if (isSelected) {
            Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(2.dp).background(ZGreen))
        }
    }
}

@Composable
fun ZFileRowFix(item: ZFile, onClick: () -> Unit, onDelete: (ZFile) -> Unit, onRename: (ZFile, String) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(item.name) }

    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(14.dp, 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val meta = getZFileIconMeta(item)
        val icon = meta.first
        val color = meta.second
        
        Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name, color = ZOnSurface, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row {
                val info = if (item.isDirectory) "${item.childCount} items" else formatReadableSize(item.size)
                Text(info, color = Color.Gray, fontSize = 10.sp)
                Text(" • ", color = Color.Gray, fontSize = 10.sp)
                Text(formatReadableDate(item.lastModified), color = Color.Gray, fontSize = 10.sp)
            }
        }
        Box {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Rounded.MoreVert, null, tint = Color.Gray.copy(0.4f), modifier = Modifier.size(18.dp))
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(ZSurface)) {
                DropdownMenuItem(
                    text = { Text("Rename", color = ZOnSurface) }, 
                    onClick = { showMenu = false; showRenameDialog = true },
                    leadingIcon = { Icon(Icons.Rounded.Edit, null, tint = Color(0xFF42A5F5), modifier = Modifier.size(18.dp)) }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = Color(0xFFEF5350)) }, 
                    onClick = { showMenu = false; onDelete(item) },
                    leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp)) }
                )
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 58.dp), color = Color.White.copy(0.03f))

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            containerColor = ZSurface,
            title = { Text("Rename", color = ZOnSurface) },
            text = { 
                OutlinedTextField(
                    value = newName, 
                    onValueChange = { newName = it }, 
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ZGreen,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = ZOnSurface,
                        unfocusedTextColor = ZOnSurface
                    )
                ) 
            },
            confirmButton = { TextButton(onClick = { onRename(item, newName); showRenameDialog = false }) { Text("SAVE", color = ZGreen) } },
            dismissButton = { TextButton(onClick = { showRenameDialog = false }) { Text("CANCEL") } }
        )
    }
}

/**
 * --- 5. PREVIEW COMPONENTS ---
 */
@Composable
fun ZFilePreviewDialog(
    fileItem: ZFile,
    onDismiss: () -> Unit,
    onOpenExternal: (File) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Column(Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, null, tint = Color.White)
                    }
                    Text(
                        fileItem.name, 
                        color = Color.White, 
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )
                    IconButton(onClick = { onOpenExternal(fileItem.file) }) {
                        Icon(Icons.AutoMirrored.Rounded.OpenInNew, null, tint = ZGreen)
                    }
                }

                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    when (fileItem.extension) {
                        "jpg", "jpeg", "png", "webp", "gif" -> {
                            AsyncImage(
                                model = fileItem.file,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        "mp4", "mkv", "mp3", "wav", "ogg" -> {
                            ZMediaPlayer(fileItem.file)
                        }
                        "pdf" -> {
                            // Basic PDF info - recommending external for full reading
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Rounded.PictureAsPdf, null, Modifier.size(100.dp), Color(0xFFE57373))
                                Spacer(Modifier.height(16.dp))
                                Text("PDF Document", color = Color.White, fontSize = 20.sp)
                                Text("${formatReadableSize(fileItem.size)} • ${formatReadableDate(fileItem.lastModified)}", color = Color.Gray, fontSize = 14.sp)
                                Spacer(Modifier.height(24.dp))
                                Button(
                                    onClick = { onOpenExternal(fileItem.file) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ZGreen)
                                ) {
                                    Text("VIEW FULL DOCUMENT")
                                }
                            }
                        }
                        else -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.AutoMirrored.Rounded.InsertDriveFile, null, Modifier.size(80.dp), Color.Gray)
                                Spacer(Modifier.height(16.dp))
                                Text("No Preview Available", color = Color.White)
                                TextButton(onClick = { onOpenExternal(fileItem.file) }) {
                                    Text("Open externally", color = ZGreen)
                                }
                            }
                        }
                    }
                }
                
                // Footer info
                Surface(
                    color = Color.White.copy(0.05f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Path: ${fileItem.file.absolutePath}",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(16.dp),
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
fun ZMediaPlayer(file: File) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                setBackgroundColor(android.graphics.Color.BLACK)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * --- 6. UTILS ---
 */
fun getSDCardPathsRobust(context: Context): List<String> {
    val paths = mutableListOf<String>()
    val sm = context.getSystemService(StorageManager::class.java)
    sm.storageVolumes.forEach { vol ->
        if (vol.isRemovable && vol.state == Environment.MEDIA_MOUNTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                vol.directory?.absolutePath?.let { paths.add(it) }
            } else {
                try {
                    val path = vol.javaClass.getMethod("getPath").invoke(vol) as String
                    paths.add(path)
                } catch (e: Exception) {}
            }
        }
    }
    if (paths.isEmpty()) {
        context.getExternalFilesDirs(null).forEach { f ->
            if (f != null && Environment.isExternalStorageRemovable(f)) {
                val p = f.absolutePath.split("/Android")[0]
                if (!paths.contains(p)) paths.add(p)
            }
        }
    }
    return paths.distinct()
}

fun getZFileIconMeta(item: ZFile): Pair<ImageVector, Color> {
    if (item.isDirectory) return Icons.Rounded.Folder to ZFolderColor
    return when (item.extension) {
        "jpg", "jpeg", "png", "webp", "gif" -> Icons.Rounded.Image to Color(0xFFE91E63)
        "mp4", "mkv", "mov", "avi" -> Icons.Rounded.VideoLibrary to Color(0xFFF44336)
        "mp3", "wav", "m4a", "ogg" -> Icons.Rounded.MusicNote to Color(0xFF9C27B0)
        "pdf" -> Icons.Rounded.PictureAsPdf to Color(0xFFFF5252)
        "zip", "rar", "7z", "tar", "gz" -> Icons.AutoMirrored.Rounded.InsertDriveFile to Color(0xFFFFA000)
        "kt", "java", "py", "js", "html", "css" -> Icons.Rounded.Code to Color(0xFF8BC34A)
        else -> Icons.AutoMirrored.Rounded.InsertDriveFile to Color(0xFF90A4AE)
    }
}

fun formatReadableSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val i = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, 3)
    return String.format("%.1f %s", size / Math.pow(1024.0, i.toDouble()), units[i])
}

fun formatReadableDate(time: Long): String {
    return SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(Date(time))
}