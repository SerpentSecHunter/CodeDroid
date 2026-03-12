package com.example.codedroid.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DrawerItem(
    val page   : NavPage,
    val icon   : ImageVector,
    val label  : String,
    val color  : Color,
    val badge  : String? = null
)

val drawerItems = listOf(
    DrawerItem(NavPage.EDITOR,         Icons.Rounded.Code,          "Editor",            Color(0xFF64B5F6)),
    DrawerItem(NavPage.FILES,          Icons.Rounded.Folder,        "File Manager",      Color(0xFFFFD54F)),
    DrawerItem(NavPage.TERMINAL,       Icons.Rounded.Computer,      "Terminal",          Color(0xFF00FF41)),
    DrawerItem(NavPage.FTP,            Icons.Rounded.Cloud,         "FTP Client",        Color(0xFF80DEEA)),
    DrawerItem(NavPage.SNIPPETS,       Icons.Rounded.Bookmarks,     "Snippet",           Color(0xFFCE93D8)),
    DrawerItem(NavPage.EXTENSIONS,     Icons.Rounded.Extension,     "Extensions",        Color(0xFFFF8A65), "NEW"),
    DrawerItem(NavPage.AI_PANEL,       Icons.Rounded.SmartToy,      "AI Panel",          Color(0xFFA5D6A7), "NEW"),
    DrawerItem(NavPage.PYTHON_LIBRARY, Icons.Rounded.MenuBook,      "Python Library",    Color(0xFF80CBC4), "NEW"),
    DrawerItem(NavPage.SETTINGS,       Icons.Rounded.Settings,      "Pengaturan",        Color(0xFFFFAB91))
)

@Composable
fun AppDrawer(
    currentPage    : NavPage,
    onNavigate     : (NavPage) -> Unit,
    onCloseDrawer  : () -> Unit,
    fileName       : String,
    isModified     : Boolean,
    modifier       : Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier      = modifier.width(280.dp),
        drawerShape   = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .padding(24.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("</>" , fontSize = 16.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("CodeDroid", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface)
                            Text("v2.2.0", fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    // File aktif
                    Surface(
                        color  = MaterialTheme.colorScheme.surfaceVariant,
                        shape  = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Description, null,
                                modifier = Modifier.size(16.dp),
                                tint     = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text     = (if (isModified) "● " else "") + fileName,
                                fontSize = 12.sp,
                                color    = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            // Nav items
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Group 1: Main
                DrawerGroupLabel("UTAMA")
                drawerItems.take(5).forEach { item ->
                    DrawerNavItem(item, currentPage == item.page) {
                        onNavigate(item.page)
                        onCloseDrawer()
                    }
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                // Group 2: Fitur Baru
                DrawerGroupLabel("FITUR BARU v2.2")
                drawerItems.drop(5).take(3).forEach { item ->
                    DrawerNavItem(item, currentPage == item.page) {
                        onNavigate(item.page)
                        onCloseDrawer()
                    }
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                // Group 3: Settings
                DrawerGroupLabel("LAINNYA")
                drawerItems.last().let { item ->
                    DrawerNavItem(item, currentPage == item.page) {
                        onNavigate(item.page)
                        onCloseDrawer()
                    }
                }
            }

            // Footer
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Person, null,
                    modifier = Modifier.size(16.dp),
                    tint     = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                Spacer(Modifier.width(8.dp))
                Text("Ade Pratama", fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
            }
        }
    }
}

@Composable
private fun DrawerGroupLabel(text: String) {
    Text(
        text     = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color    = MaterialTheme.colorScheme.primary.copy(0.7f),
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp, top = 4.dp)
    )
}

@Composable
private fun DrawerNavItem(
    item      : DrawerItem,
    isSelected: Boolean,
    onClick   : () -> Unit
) {
    val bgColor = if (isSelected) item.color.copy(alpha = 0.12f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color indicator
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(item.color)
            )
            Spacer(Modifier.width(10.dp))
        } else {
            Spacer(Modifier.width(13.dp))
        }

        Icon(
            imageVector        = item.icon,
            contentDescription = item.label,
            tint               = if (isSelected) item.color
                                 else MaterialTheme.colorScheme.onSurface.copy(0.55f),
            modifier           = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text       = item.label,
            fontSize   = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color      = if (isSelected) item.color
                         else MaterialTheme.colorScheme.onSurface.copy(0.8f),
            modifier   = Modifier.weight(1f)
        )
        // Badge
        item.badge?.let { badge ->
            Surface(
                color  = MaterialTheme.colorScheme.primary,
                shape  = RoundedCornerShape(6.dp)
            ) {
                Text(badge, fontSize = 8.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
            }
        }
    }
}