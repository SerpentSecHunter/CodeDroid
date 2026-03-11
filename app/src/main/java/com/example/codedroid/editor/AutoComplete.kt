package com.example.codedroid.editor

object AutoComplete {

    private val kotlinKeywords = listOf(
        "fun","val","var","class","object","interface","enum","sealed","data",
        "when","if","else","for","while","return","import","package","override",
        "suspend","launch","async","Flow","StateFlow","remember","mutableStateOf",
        "String","Int","Long","Float","Double","Boolean","Unit","List","Map","Set"
    )

    private val pythonKeywords = listOf(
        "def","class","import","from","return","if","elif","else","for","while",
        "in","and","or","not","with","try","except","finally","raise","pass",
        "True","False","None","print","len","range","str","int","float","list","dict"
    )

    private val jsKeywords = listOf(
        "const","let","var","function","class","return","if","else","for","while",
        "switch","case","break","try","catch","async","await","import","export",
        "null","undefined","true","false","console","Array","Object","Promise"
    )

    private val javaKeywords = listOf(
        "public","private","protected","static","final","class","interface",
        "extends","implements","new","return","if","else","for","while","switch",
        "String","int","long","boolean","void","Object","List","ArrayList","Map"
    )

    private val htmlTags = listOf(
        "div","span","p","a","img","input","button","form","table","ul","ol","li",
        "h1","h2","h3","header","footer","nav","main","section","article","canvas"
    )

    private val cssProps = listOf(
        "display","position","flex","width","height","margin","padding","color",
        "background","font-size","border","overflow","transition","animation",
        "transform","opacity","z-index","align-items","justify-content","gap"
    )

    fun getSuggestions(prefix: String, language: String): List<String> {
        if (prefix.length < 2) return emptyList()
        val words = when (language.lowercase()) {
            "kotlin","kt"               -> kotlinKeywords
            "python","py"               -> pythonKeywords
            "javascript","js",
            "typescript","ts"           -> jsKeywords
            "java"                      -> javaKeywords
            "html","htm"                -> htmlTags
            "css","scss"                -> cssProps
            else                        -> emptyList()
        }
        return words
            .filter { it.startsWith(prefix, ignoreCase = true) && it != prefix }
            .take(8)
            .sortedBy { it.length }
    }

    fun getCurrentWord(text: String, cursorPos: Int): String {
        if (cursorPos <= 0 || text.isEmpty()) return ""
        val safePos = cursorPos.coerceAtMost(text.length)
        var start   = safePos
        while (start > 0 && (text[start - 1].isLetterOrDigit() || text[start - 1] == '_'))
            start--
        return text.substring(start, safePos)
    }
}