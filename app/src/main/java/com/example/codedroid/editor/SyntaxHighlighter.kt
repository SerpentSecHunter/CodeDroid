package com.example.codedroid.editor

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

object SyntaxHighlighter {

    fun highlight(code: String, language: String, theme: EditorTheme): AnnotatedString {
        if (code.isEmpty()) return buildAnnotatedString { append(code) }
        val rules = when (language.lowercase()) {
            "kotlin", "kt"             -> kotlinRules(theme)
            "java"                     -> javaRules(theme)
            "python", "py"             -> pythonRules(theme)
            "javascript", "js",
            "typescript", "ts"         -> jsRules(theme)
            "html", "htm"              -> htmlRules(theme)
            "css", "scss"              -> cssRules(theme)
            "json"                     -> jsonRules(theme)
            "xml"                      -> xmlRules(theme)
            "bash", "sh"               -> bashRules(theme)
            "markdown", "md"           -> markdownRules(theme)
            else                       -> return buildAnnotatedString { append(code) }
        }
        return buildAnnotatedString {
            append(code)
            for ((regex, style) in rules) {
                try {
                    regex.findAll(code).forEach { match ->
                        val s = match.range.first
                        val e = match.range.last + 1
                        if (s >= 0 && e <= code.length && s < e) addStyle(style, s, e)
                    }
                } catch (_: Exception) { }
            }
        }
    }

    fun detectLanguage(fileName: String): String = when (
        fileName.substringAfterLast('.').lowercase()) {
        "kt", "kts"              -> "kotlin"
        "java"                   -> "java"
        "py"                     -> "python"
        "js", "jsx", "ts", "tsx" -> "javascript"
        "html", "htm"            -> "html"
        "css", "scss"            -> "css"
        "json"                   -> "json"
        "xml", "svg"             -> "xml"
        "sh", "bash"             -> "bash"
        "md", "markdown"         -> "markdown"
        else                     -> "text"
    }

    fun getLanguageLabel(lang: String): String = when (lang.lowercase()) {
        "kotlin"     -> "Kotlin"
        "java"       -> "Java"
        "python"     -> "Python"
        "javascript" -> "JavaScript"
        "typescript" -> "TypeScript"
        "html"       -> "HTML"
        "css"        -> "CSS"
        "json"       -> "JSON"
        "xml"        -> "XML"
        "bash"       -> "Bash"
        "markdown"   -> "Markdown"
        else         -> "Plain Text"
    }

    private fun kotlinRules(t: EditorTheme) = listOf(
        Regex("//[^\n]*") to SpanStyle(color = t.comment, fontStyle = FontStyle.Italic),
        Regex("/\\*[\\s\\S]*?\\*/") to SpanStyle(color = t.comment, fontStyle = FontStyle.Italic),
        Regex("\"\"\"[\\s\\S]*?\"\"\"|\"(?:[^\"\\\\]|\\\\.)*\"|'(?:[^'\\\\]|\\\\.)*'") to SpanStyle(color = t.string),
        Regex("\\b(fun|val|var|class|object|interface|enum|sealed|data|when|if|else|for|while|do|return|import|package|is|as|in|by|companion|override|open|final|abstract|private|protected|public|internal|suspend|inline|const|init|constructor|operator|typealias|lateinit)\\b") to SpanStyle(color = t.keyword, fontWeight = FontWeight.Bold),
        Regex("\\b(String|Int|Long|Float|Double|Boolean|Unit|Any|List|MutableList|Map|MutableMap|Set|Flow|StateFlow|MutableStateFlow|Context|Activity|ViewModel)\\b") to SpanStyle(color = t.attribute),
        Regex("\\b(true|false|null|this|super)\\b") to SpanStyle(color = t.number),
        Regex("\\b\\d+\\.?\\d*\\b") to SpanStyle(color = t.number),
        Regex("@[A-Za-z][A-Za-z0-9]*") to SpanStyle(color = t.function),
    )

    private fun javaRules(t: EditorTheme) = listOf(
        Regex("//[^\n]*") to SpanStyle(color = t.comment, fontStyle = FontStyle.Italic),
        Regex("/\\*[\\s\\S]*?\\*/") to SpanStyle(color = t.comment, fontStyle = FontStyle.Italic),
        Regex("\"(?:[^\"\\\\]|\\\\.)*\"") to SpanStyle(color = t.string),
        Regex("\\b(public|private|protected|static|final|class|interface|extends|implements|new|return|if|else|for|while|switch|case|break|continue|try|catch|finally|throw|import|void|this|super)\\b") to SpanStyle(color = t.keyword, fontWeight = FontWeight.Bold),
        Regex("\\b(String|int|long|float|double|boolean|void|Object|List|ArrayList|Map|HashMap)\\b") to SpanStyle(color = t.attribute),
        Regex("\\b(true|false|null)\\b") to SpanStyle(color = t.number),
        Regex("\\b\\d+\\.?\\d*\\b") to SpanStyle(color = t.number),
    )

    private fun pythonRules(t: EditorTheme) = listOf(
        Regex("#[^\n]*") to SpanStyle(color = t.comment, fontStyle = FontStyle.Italic),
        Regex("\"\"\"[\\s\\S]*?\"\"\"|'[^']*'|\"(?:[^\"\\\\]|\\\\.)*\"") to SpanStyle(color = t.string),
        Regex("\\b(def|class|import|from|return|if|elif|else|for|while|in|and|or|not|with|try|except|finally|raise|pass|break|continue|lambda|yield|async|await)\\b") to SpanStyle(color = t.keyword, fontWeight = FontWeight.Bold),
        Regex("\\b(str|int|float|bool|list|dict|set|tuple|print|len|range|None|True|False)\\b") to SpanStyle(color = t.attribute),
        Regex("\\b\\d+\\.?\\d*\\b") to SpanStyle(color = t.number),
    )

    private fun jsRules(t: EditorTheme) = listOf(
        Regex("//[^\n]*") to SpanStyle(color = t.comment, fontStyle = FontStyle.Italic),
        Regex("/\\*[\\s\\S]*?\\*/") to SpanStyle(color = t.comment, fontStyle = FontStyle.Italic),
        Regex("`[^`]*`|\"(?:[^\"\\\\]|\\\\.)*\"|'(?:[^'\\\\]|\\\\.)*'") to SpanStyle(color = t.string),
        Regex("\\b(const|let|var|function|class|return|if|else|for|while|switch|case|break|continue|try|catch|new|import|export|default|async|await|this|typeof|instanceof)\\b") to SpanStyle(color = t.keyword, fontWeight = FontWeight.Bold),
        Regex("\\b(Array|Object|String|Number|Boolean|Promise|Map|Set|console|undefined|null)\\b") to SpanStyle(color = t.attribute),
        Regex("\\b(true|false|null|undefined)\\b") to SpanStyle(color = t.number),
        Regex("\\b\\d+\\.?\\d*\\b") to SpanStyle(color = t.number),
    )

    private fun htmlRules(t: EditorTheme) = listOf(
        Regex("<!--[\\s\\S]*?-->") to SpanStyle(color = t.comment, fontStyle = FontStyle.Italic),
        Regex("\"[^\"]*\"|'[^']*'") to SpanStyle(color = t.string),
        Regex("</?[a-zA-Z][a-zA-Z0-9]*") to SpanStyle(color = t.keyword, fontWeight = FontWeight.Bold),
        Regex("\\b[a-zA-Z-]+(?=\\s*=)") to SpanStyle(color = t.attribute),
        Regex("[<>/=]") to SpanStyle(color = t.operator),
    )

    private fun cssRules(t: EditorTheme) = listOf(
        Regex("/\\*[\\s\\S]*?\\*/") to SpanStyle(color = t.comment, fontStyle = FontStyle.Italic),
        Regex("\"[^\"]*\"|'[^']*'") to SpanStyle(color = t.string),
        Regex("[.#][a-zA-Z][a-zA-Z0-9_-]*") to SpanStyle(color = t.function),
        Regex("\\b(display|position|flex|width|height|margin|padding|color|background|font|border|overflow|transition|animation|transform)\\b") to SpanStyle(color = t.keyword),
        Regex("#[0-9a-fA-F]{3,8}|\\b\\d+\\.?\\d*(px|em|rem|%|vh|vw)?\\b") to SpanStyle(color = t.number),
    )

    private fun jsonRules(t: EditorTheme) = listOf(
        Regex("\"(?:[^\"\\\\]|\\\\.)*\"\\s*:") to SpanStyle(color = t.keyword, fontWeight = FontWeight.Bold),
        Regex(":\\s*\"(?:[^\"\\\\]|\\\\.)*\"") to SpanStyle(color = t.string),
        Regex("\\b(true|false|null)\\b") to SpanStyle(color = t.attribute),
        Regex("\\b-?\\d+\\.?\\d*\\b") to SpanStyle(color = t.number),
    )

    private fun xmlRules(t: EditorTheme) = listOf(
        Regex("<!--[\\s\\S]*?-->") to SpanStyle(color = t.comment, fontStyle = FontStyle.Italic),
        Regex("\"[^\"]*\"|'[^']*'") to SpanStyle(color = t.string),
        Regex("<[?!]?/?[a-zA-Z][a-zA-Z0-9.:_-]*") to SpanStyle(color = t.keyword, fontWeight = FontWeight.Bold),
        Regex("\\b[a-zA-Z:][a-zA-Z0-9.:_-]*(?=\\s*=)") to SpanStyle(color = t.attribute),
    )

    private fun bashRules(t: EditorTheme) = listOf(
        Regex("#[^\n]*") to SpanStyle(color = t.comment, fontStyle = FontStyle.Italic),
        Regex("\"(?:[^\"\\\\]|\\\\.)*\"|'[^']*'") to SpanStyle(color = t.string),
        Regex("\\b(if|then|else|elif|fi|for|while|do|done|case|esac|function|return|exit)\\b") to SpanStyle(color = t.keyword, fontWeight = FontWeight.Bold),
        Regex("\\b(echo|ls|cd|mkdir|rm|cp|mv|grep|sudo|git|curl|wget|chmod)\\b") to SpanStyle(color = t.function),
        Regex("\\$\\{?[a-zA-Z_][a-zA-Z0-9_]*\\}?") to SpanStyle(color = t.attribute),
        Regex("\\b\\d+\\b") to SpanStyle(color = t.number),
    )

    private fun markdownRules(t: EditorTheme) = listOf(
        Regex("^#{1,6}\\s.+", RegexOption.MULTILINE) to SpanStyle(color = t.keyword, fontWeight = FontWeight.Bold),
        Regex("```[\\s\\S]*?```|`[^`]+`") to SpanStyle(color = t.string),
        Regex("\\*\\*[^*]+\\*\\*") to SpanStyle(fontWeight = FontWeight.Bold),
        Regex("\\*[^*]+\\*") to SpanStyle(fontStyle = FontStyle.Italic),
        Regex("^[-*+]\\s", RegexOption.MULTILINE) to SpanStyle(color = t.number),
    )
}