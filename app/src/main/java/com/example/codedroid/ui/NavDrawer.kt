package com.example.codedroid.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Termux-compatible professional colors
private val DRAWER_BG      = Color(0xFF000000)
private val DRAWER_ITEM_BG = Color(0xFF151515)
private val ACCENT_GREEN   = Color(0xFF4CAF50)
private val TEXT_PRIMARY   = Color(0xFFE0E0E0)
private val TEXT_SECONDARY = Color(0xFF909090)
private val BORDER_COLOR   = Color(0xFF222222)

data class DrawerItem(
    val page   : NavPage,
    val icon   : ImageVector,
    val label  : String,
    val color  : Color,
    val badge  : String? = null
)

val drawerItems = listOf(
    DrawerItem(NavPage.EDITOR,         Icons.Rounded.Code,          "Editor",            Color(0xFF64B5F6)),
    DrawerItem(NavPage.FILES,          Icons.Rounded.FolderOpen,    "File Manager",      Color(0xFFFFD54F)),
    DrawerItem(NavPage.TERMINAL,       Icons.Rounded.Terminal,      "Terminal",          ACCENT_GREEN),
    DrawerItem(NavPage.FTP,            Icons.Rounded.CloudQueue,    "FTP Client",        Color(0xFF80DEEA)),
    DrawerItem(NavPage.SNIPPETS,       Icons.Rounded.AutoAwesome,   "Snippets",          Color(0xFFCE93D8)),
    DrawerItem(NavPage.EXTENSIONS,     Icons.Rounded.Extension,     "Extensions",        Color(0xFFFF8A65), "NEW"),
    DrawerItem(NavPage.AI_PANEL,       Icons.Rounded.Psychology,    "AI Panel",          Color(0xFFA5D6A7), "NEW"),
    DrawerItem(NavPage.PYTHON_LIBRARY, Icons.Rounded.Storage,       "Python Library",    Color(0xFF80CBC4), "NEW"),
    DrawerItem(NavPage.SETTINGS,       Icons.Rounded.Settings,      "Settings",          Color(0xFFFFAB91))
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
        modifier      = modifier.width(300.dp),
        drawerShape   = RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp), // Squared for professional look
        drawerContainerColor = DRAWER_BG
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(width = 1.dp, color = BORDER_COLOR, shape = RoundedCornerShape(0.dp))
        ) {
            // High-End Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.linearGradient(listOf(ACCENT_GREEN.copy(0.2f), Color.Transparent))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text     = "</>", 
                                fontSize = 18.sp, 
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color    = ACCENT_GREEN
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text       = "CodeDroid", 
                                fontSize   = 19.sp, 
                                fontWeight = FontWeight.ExtraBold,
                                color      = TEXT_PRIMARY,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text  = "v2.2.0 - System Terminal", 
                                fontSize = 11.sp,
                                color = TEXT_SECONDARY,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(20.dp))
                    
                    // Active Session Indicator
                    Surface(
                        color  = DRAWER_ITEM_BG,
                        shape  = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.5.dp, BORDER_COLOR)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.History, 
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint     = ACCENT_GREEN
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text     = if (currentPage == NavPage.EDITOR) "File: $fileName" else "Session: ${currentPage.name}",
                                fontSize = 11.sp,
                                color    = TEXT_PRIMARY,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Navigation List
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DrawerGroupLabel("CORE NAVIGATION")
                drawerItems.take(5).forEach { item ->
                    DrawerNavItem(item, currentPage == item.page) {
                        onNavigate(item.page)
                        onCloseDrawer()
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = BORDER_COLOR, thickness = 0.5.dp)
                Spacer(Modifier.height(12.dp))

                DrawerGroupLabel("ADVANCED FEATURES")
                drawerItems.drop(5).take(3).forEach { item ->
                    DrawerNavItem(item, currentPage == item.page) {
                        onNavigate(item.page)
                        onCloseDrawer()
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = BORDER_COLOR, thickness = 0.5.dp)
                Spacer(Modifier.height(12.dp))

                DrawerGroupLabel("SYSTEM")
                drawerItems.last().let { item ->
                    DrawerNavItem(item, currentPage == item.page) {
                        onNavigate(item.page)
                        onCloseDrawer()
                    }
                }
            }

            // Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "BUILD SUCCESSFUL",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = ACCENT_GREEN,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Logged as codedroid@android",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TEXT_SECONDARY
                )
            }
        }
    }
}

@Composable
private fun DrawerGroupLabel(text: String) {
    Text(
        text     = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        color    = TEXT_SECONDARY,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 4.dp)
    )
}

@Composable
private fun DrawerNavItem(
    item      : DrawerItem,
    isSelected: Boolean,
    onClick   : () -> Unit
) {
    val bgColor = if (isSelected) ACCENT_GREEN.copy(alpha = 0.15f) else Color.Transparent
    val contentColor = if (isSelected) ACCENT_GREEN else TEXT_PRIMARY

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector        = item.icon,
            contentDescription = item.label,
            tint               = contentColor,
            modifier           = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text       = item.label,
            fontSize   = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color      = contentColor,
            fontFamily = FontFamily.Monospace,
            modifier   = Modifier.weight(1f)
        )
        
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(ACCENT_GREEN)
            )
        }

        item.badge?.let { badge ->
            Surface(
                color  = ACCENT_GREEN.copy(0.2f),
                shape  = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, ACCENT_GREEN.copy(0.4f))
            ) {
                Text(
                    text     = badge, 
                    fontSize = 8.sp, 
                    fontWeight = FontWeight.Black,
                    color = ACCENT_GREEN,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}