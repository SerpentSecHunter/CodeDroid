package com.example.codedroid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuickToolbar(onInsert: (String) -> Unit, modifier: Modifier = Modifier) {
    val symbols = listOf(
        "Tab" to "\t",  "{" to "{",   "}" to "}",
        "(" to "(",     ")" to ")",   "[" to "[",
        "]" to "]",     ";" to ";",   ":" to ":",
        "=" to "=",     "\"" to "\"", "'" to "'",
        "<" to "<",     ">" to ">",   "/" to "/",
        "\\" to "\\",   "|" to "|",   "&" to "&",
        "!" to "!",     "." to ".",   "," to ",",
        "_" to "_",     "-" to "-",   "+" to "+",
        "*" to "*",     "%" to "%",   "#" to "#",
        "@" to "@",     "^" to "^",   "~" to "~"
    )

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 6.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        symbols.forEach { (label, insert) ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .let { m ->
                        m.then(
                            Modifier.height(28.dp)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = { onInsert(insert) },
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        label,
                        fontSize   = 12.sp,
                        fontFamily = if (label != "Tab") FontFamily.Monospace else FontFamily.Default,
                        color      = MaterialTheme.colorScheme.onSurface.copy(0.8f)
                    )
                }
            }
        }
    }
}