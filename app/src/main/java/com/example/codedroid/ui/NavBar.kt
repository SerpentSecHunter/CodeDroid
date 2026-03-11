package com.example.codedroid.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*

data class NavItem(
    val page  : NavPage,
    val icon  : ImageVector,
    val label : String,
    val color : Color
)

val navItems = listOf(
    NavItem(NavPage.EDITOR,   Icons.Rounded.Code,          "Editor",   Color(0xFF64B5F6)),
    NavItem(NavPage.FILES,    Icons.Rounded.Folder,     "File",     Color(0xFFFFD54F)),
    NavItem(NavPage.PREVIEW,  Icons.Rounded.Visibility,        "Preview",  Color(0xFF81C784)),
    NavItem(NavPage.FTP,      Icons.Rounded.Cloud,          "FTP",      Color(0xFF80DEEA)),
    NavItem(NavPage.SNIPPETS, Icons.Rounded.Bookmarks,      "Snippet",  Color(0xFFCE93D8)),
    NavItem(NavPage.TERMINAL, Icons.Rounded.Computer,       "Terminal", Color(0xFF00FF41)),
    NavItem(NavPage.SETTINGS, Icons.Rounded.Settings,           "Setting",  Color(0xFFFFAB91))
)

@Composable
fun ModernNavBar(
    currentPage : NavPage,
    onNavigate  : (NavPage) -> Unit,
    modifier    : Modifier = Modifier
) {
    Surface(
        modifier      = modifier.fillMaxWidth(),
        tonalElevation= 0.dp,
        shadowElevation= 20.dp,
        color         = MaterialTheme.colorScheme.surface,
        shape         = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                NavBarItem(
                    item       = item,
                    isSelected = currentPage == item.page,
                    onClick    = { onNavigate(item.page) }
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    item      : NavItem,
    isSelected: Boolean,
    onClick   : () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) item.color.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium), label = ""
    )
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) item.color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        animationSpec = spring(stiffness = Spring.StiffnessMedium), label = ""
    )
    val padH by animateDpAsState(
        targetValue = if (isSelected) 14.dp else 10.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium), label = ""
    )

    Column(
        modifier              = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null
            ) { onClick() }
            .padding(horizontal = padH, vertical = 6.dp),
        horizontalAlignment   = Alignment.CenterHorizontally
    ) {
        // Dot indicator
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(item.color)
            )
            Spacer(Modifier.height(3.dp))
        } else {
            Spacer(Modifier.height(7.dp))
        }

        Icon(
            imageVector   = item.icon,
            contentDescription = item.label,
            tint          = iconColor,
            modifier      = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text      = item.label,
            fontSize  = 9.sp,
            color     = iconColor,
            maxLines  = 1
        )
    }
}