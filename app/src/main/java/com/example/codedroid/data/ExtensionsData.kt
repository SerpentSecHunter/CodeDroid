package com.example.codedroid.data

import androidx.compose.ui.graphics.Color

data class ExtensionInfo(
    val id         : String,
    val name       : String,
    val description: String,
    val detail     : String,
    val version    : String,
    val author     : String,
    val color      : Color,
    val category   : String,
    val rating     : Float = 4.5f,
    val installs   : String = "1K+",
    val iconUrl    : String? = null // For professional look, we can use themed color + first letter if null
)

object ExtensionsData {

    private val coreExtensions = listOf(
        // === POPULAR & ESSENTIAL ===
        ExtensionInfo("copilot","GitHub Copilot","AI Pair Programmer","Asisten AI yang melengkapi baris kode Anda secara cerdas berdasarkan konteks dan pola.","1.2.5","GitHub",Color(0xFF24292E),"AI", 4.9f, "5M+"),
        ExtensionInfo("pets","CodeDroid Pets","Virtual Coding Buddies","Teman coding — kucing, anjing, dan naga animasi yang beralu-lalang di editor Anda.","1.2.0","CodeDroid",Color(0xFFFF80AB),"Fun", 4.8f, "500K+"),
        ExtensionInfo("wakatime","WakaTime","Productivity Tracker","Otomatis catat statistik coding untuk memantau produktivitas harian Anda.","24.1.0","WakaTime",Color(0xFF00BFA5),"Stats", 4.7f, "1M+"),
        ExtensionInfo("prettier","Prettier","Code Formatter","Format kode JS, HTML, CSS, JSON, Markdown dengan standar industri.","3.3.0","Prettier",Color(0xFFF7BA3E),"Formatter", 4.9f, "10M+"),
        
        // === LANGUAGES ===
        ExtensionInfo("python", "Python", "Full Python Support", "Linter, debugger, autocomplete, dan integrasi unit test untuk Python.","2024.2.0","Microsoft",Color(0xFFFFEB3B),"Language", 4.8f, "80M+"),
        ExtensionInfo("javaext", "Java Language Support", "Enterprise Java", "Eclipse JDT, refactoring, dan Maven/Gradle support.","1.26.0","Red Hat",Color(0xFFF44336),"Language", 4.6f, "20M+"),
        ExtensionInfo("cppk", "C/C++", "C++ IntelliSense", "Saran sintaks, debugging, dan navigasi kode untuk C/C++.","1.18.0","Microsoft",Color(0xFF00599C),"Language", 4.5f, "35M+"),
        ExtensionInfo("kotlinlang", "Kotlin", "Kotlin for Android", "Dukungan penuh JetBrains untuk penulisan bahasa Kotlin.","1.9.22","JetBrains",Color(0xFF7C4DFF),"Language", 4.9f, "5M+"),
        ExtensionInfo("php-intel","PHP Intelephense","PHP Intelligence","High performance PHP support dengan indexasi super cepat.","1.12.0","Ben Mewburn",Color(0xFF6A1B9A),"Language", 4.8f, "7M+"),
        
        // === THEMES ===
        ExtensionInfo("theme-onedark","One Dark Pro","Vibrant Dark Theme","Tema paling populer dari VS Code untuk CodeDroid.","3.15.0","binaryify",Color(0xFF282C34),"Theme", 4.9f, "6M+"),
        ExtensionInfo("theme-dracula", "Dracula Official", "Iconic Dark Theme", "Tema gelap dengan kontras tinggi yang nyaman untuk mata.","2.24.2","Dracula Theme",Color(0xFFFF79C6),"Theme", 4.9f, "4M+"),
        ExtensionInfo("theme-tokyo", "Tokyo Night", "City Lights at Night", "Tema bernuansa malam di Tokyo dengan aksen neon.","0.9.8","enkia",Color(0xFF81D4FA),"Theme", 4.8f, "1M+"),
        ExtensionInfo("theme-synthwave", "SynthWave '84", "80s Retro Neon", "Tema dengan efek 'glow' neon yang futuristik.","0.1.13","Robb Owen",Color(0xFFD632BA),"Theme", 4.7f, "800K+"),
        
        // === FRAMEWORKS ===
        ExtensionInfo("react-snippets", "React Snippets", "ES7+ React/Redux", "Kumpulan snippet tercepat untuk build komponen React.","4.4.0","dsznajder",Color(0xFF61DAFB),"Framework", 4.8f, "9M+"),
        ExtensionInfo("flutter", "Flutter", "Flutter & Dart Support", "Workflow lengkap untuk pengembangan Flutter.","3.71.0","Dart Code",Color(0xFF2196F3),"Framework", 4.9f, "6M+"),
        ExtensionInfo("laravel-extra","Laravel Extra","Intellisense for Laravel", "Autocomplete untuk route, model, dan view Laravel.","1.4.2","amir9480",Color(0xFFFF5722),"Framework", 4.7f, "2M+"),
        ExtensionInfo("tailwindcss", "Tailwind CSS", "Utility-First CSS", "IntelliSense, linter, dan preview untuk Tailwind.","0.11.0","Tailwind Tabs",Color(0xFF38B2AC),"CSS", 4.9f, "5M+")
    )

    // Helper to generate 500+ Extensions
    private fun generateExtensions(): List<ExtensionInfo> {
        val result = coreExtensions.toMutableList()
        val authors = listOf("Microsoft", "Google", "Facebook", "JetBrains", "CodeDroid Team", "Community", "Vercel", "HashiCorp", "AWS", "Oracle", "NVIDIA", "Intel", "Adobe", "DigitalOcean")
        val cats = listOf("Snippet", "Utility", "Language", "UI", "Theme", "Formatter", "Git", "DevOps", "Documentation", "Database", "Security", "Cloud", "Mobile", "Testing", "Networking")
        val langs = listOf("Rust", "Go", "Ruby", "Swift", "SQL", "TypeScript", "Bash", "Assembly", "Scala", "C#", "Perl", "Lua", "Dart", "Haskell", "Zig", "Elixir", "Fortran", "COBOL", "Julia")
        
        for (i in 1..(500 - coreExtensions.size)) {
            val cat = cats.random()
            val author = authors.random()
            val language = langs.random()
            val name = when(cat) {
                "Snippet" -> "$language Ultimate Snippets v$i"
                "Utility" -> "$language Developer Kit #$i"
                "Theme" -> "Ultra $i Theme Pack"
                "UI" -> "Material Design $i Components"
                "Database" -> "SQL $i Management Pro"
                "Security" -> "Shield $i Vulnerability Scanner"
                "Language" -> "$language Compiler & Linter v$i"
                "Cloud" -> "Cloud $i Deployment Assistant"
                else -> "$cat Enterprise $i"
            }
            
            val idPrefix = when(cat) {
                "Snippet" -> "snippet"
                "Theme" -> "theme"
                "Language" -> "lang"
                "UI" -> "ui"
                else -> "ext"
            }
            
            result.add(
                ExtensionInfo(
                    id = "gen-$idPrefix-$i",
                    name = name,
                    description = "Professional $cat extension for $language development.",
                    detail = "Dirancang untuk efisiensi maksimal dalam workflow $language. Plugin ini menyediakan alat $cat tercanggih yang divalidasi oleh $author. Fitur termasuk integrasi cloud, debugging otomatis, dan optimasi performa tinggi untuk project CodeDroid Anda.",
                    version = "${(0..5).random()}.${(0..9).random()}.${(0..50).random()}",
                    author = author,
                    color = Color.hsl((0..360).random().toFloat(), 0.7f, 0.45f),
                    category = cat,
                    rating = (42..50).random() / 10f,
                    installs = when(i % 5) {
                        0 -> "${(100..999).random()}K+"
                        1 -> "${(1..9).random()}M+"
                        else -> "${(1..99).random()}K+"
                    }
                )
            )
        }
        return result
    }

    val allExtensions = generateExtensions()
}
