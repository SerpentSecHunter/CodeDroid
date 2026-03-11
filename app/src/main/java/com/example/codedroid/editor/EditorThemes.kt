package com.example.codedroid.editor

import androidx.compose.ui.graphics.Color

data class EditorTheme(
    val name      : String,
    val background: Color,
    val text      : Color,
    val keyword   : Color,
    val string    : Color,
    val comment   : Color,
    val number    : Color,
    val function  : Color,
    val operator  : Color,
    val attribute : Color,
    val lineNumbers: Color,
    val selection : Color,
    val cursor    : Color
)

object EditorThemes {
    val monokai = EditorTheme("Monokai",
        background  = Color(0xFF272822), text       = Color(0xFFF8F8F2),
        keyword     = Color(0xFFF92672), string     = Color(0xFFE6DB74),
        comment     = Color(0xFF75715E), number     = Color(0xFFAE81FF),
        function    = Color(0xFFA6E22E), operator   = Color(0xFFF8F8F2),
        attribute   = Color(0xFFA6E22E), lineNumbers= Color(0xFF666666),
        selection   = Color(0xFF49483E), cursor     = Color(0xFFF8F8F2)
    )
    val darcula = EditorTheme("Darcula",
        background  = Color(0xFF313335), text       = Color(0xFFEAEAEA),
        keyword     = Color(0xFFBB86FC), string     = Color(0xFF00B8D4),
        comment     = Color(0xFF8E8E8E), number     = Color(0xFF64DD17),
        function    = Color(0xFF03DAC6), operator   = Color(0xFFEAEAEA),
        attribute   = Color(0xFF03DAC6), lineNumbers= Color(0xFF666666),
        selection   = Color(0xFF424242), cursor     = Color(0xFFEAEAEA)
    )
    val github = EditorTheme("GitHub",
        background  = Color(0xFFFFFFFF), text       = Color(0xFF1F2328),
        keyword     = Color(0xFFD73A49), string     = Color(0xFF032F62),
        comment     = Color(0xFF6E7781), number     = Color(0xFF0550AE),
        function    = Color(0xFF8250DF), operator   = Color(0xFF1F2328),
        attribute   = Color(0xFF8250DF), lineNumbers= Color(0xFF9DA4AE),
        selection   = Color(0xFFD2A8FF), cursor     = Color(0xFF1F2328)
    )
    val dracula = EditorTheme("Dracula",
        background  = Color(0xFF282A36), text       = Color(0xFFF8F8F2),
        keyword     = Color(0xFFFF79C6), string     = Color(0xFF50FA7B),
        comment     = Color(0xFF6272A4), number     = Color(0xFFBD93F9),
        function    = Color(0xFF8BE9FD), operator   = Color(0xFFF8F8F2),
        attribute   = Color(0xFF8BE9FD), lineNumbers= Color(0xFF6272A4),
        selection   = Color(0xFF44475A), cursor     = Color(0xFFF8F8F2)
    )
    val nord = EditorTheme("Nord",
        background  = Color(0xFF2E3440), text       = Color(0xFFECEFF4),
        keyword     = Color(0xFF81A1C1), string     = Color(0xFFA3BE8C),
        comment     = Color(0xFF616E88), number     = Color(0xFFB48EAD),
        function    = Color(0xFF88C0D0), operator   = Color(0xFFECEFF4),
        attribute   = Color(0xFF88C0D0), lineNumbers= Color(0xFF4C566A),
        selection   = Color(0xFF3B4252), cursor     = Color(0xFFECEFF4)
    )
    val solarized = EditorTheme("Solarized",
        background  = Color(0xFFFDF6E3), text       = Color(0xFF657B83),
        keyword     = Color(0xFFD33682), string     = Color(0xFF2AA198),
        comment     = Color(0xFF93A1A1), number     = Color(0xFFB58900),
        function    = Color(0xFF268BD2), operator   = Color(0xFF657B83),
        attribute   = Color(0xFF268BD2), lineNumbers= Color(0xFF93A1A1),
        selection   = Color(0xFFEEE8D5), cursor     = Color(0xFF657B83)
    )

    // Map untuk lookup by key
    val all: Map<String, EditorTheme> = mapOf(
        "monokai"   to monokai,
        "darcula"   to darcula,
        "github"    to github,
        "dracula"   to dracula,
        "nord"      to nord,
        "solarized" to solarized
    )

    // List untuk iterasi di Settings (WAJIB ini — jangan hapus)
    val allList: List<Pair<String, EditorTheme>> = listOf(
        "monokai"   to monokai,
        "darcula"   to darcula,
        "github"    to github,
        "dracula"   to dracula,
        "nord"      to nord,
        "solarized" to solarized
    )

    fun get(key: String): EditorTheme = all[key] ?: monokai
}