package com.example.codedroid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class EditorTab(
    val id      : String = System.currentTimeMillis().toString(),
    val title   : String,
    val path    : String = "",
    val modified: Boolean = false
)

@Composable
fun TabBar(
    tabs        : List<EditorTab>,
    activeIdx   : Int,
    onTabSelect : (Int) -> Unit,
    onTabClose  : (Int) -> Unit,
    modifier    : Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.surface)
    ) {
        tabs.forEachIndexed { idx, tab ->
            val isActive = idx == activeIdx
            Row(
                modifier = Modifier
                    .background(
                        if (isActive) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.surface
                    )
                    .clickable { onTabSelect(idx) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text     = (if (tab.modified) "● " else "") + tab.title,
                    fontSize = 13.sp,
                    color    = if (isActive) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 1
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Default.Close, "Tutup",
                    modifier = Modifier.size(14.dp).clickable { onTabClose(idx) },
                    tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
            VerticalDivider(modifier = Modifier.height(36.dp))
        }
    }
}