package com.example.codedroid.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuickToolbar(onInsert: (String) -> Unit, modifier: Modifier = Modifier) {
    val symbols = listOf("Tab" to "\t", "{" to "{", "}" to "}", "(" to "(",
        ")" to ")", "[" to "[", "]" to "]", ";" to ";", ":" to ":",
        "=" to "=", "\"" to "\"", "'" to "'", "<" to "<", ">" to ">",
        "/" to "/", "\\" to "\\", "|" to "|", "&" to "&", "!" to "!")
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        symbols.forEach { (label, insert) ->
            OutlinedButton(
                onClick       = { onInsert(insert) },
                modifier      = Modifier.height(30.dp),
                contentPadding= PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                shape         = RoundedCornerShape(6.dp)
            ) { Text(label, fontSize = 12.sp) }
        }
    }
}