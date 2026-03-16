package com.example.codedroid.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.Signature
import android.os.Build
import android.util.Base64
import java.security.MessageDigest
import android.os.Debug

/**
 * Security utilities to prevent modding and tampering.
 */
@Suppress("SpellCheckingInspection", "SdCardPath")
object SecurityCheck {

    // WARNING: Ganti dengan SHA-256 asli Anda saat mau merilis aplikasi ke publik!
    // Jika masih "YOUR_SHA256...", sistem keamanan akan dalam mode "Development" (longgar).
    private const val OFFICIAL_SIGNATURE_HASH = "YOUR_SHA256_HASH_HERE"
    private const val EXPECTED_PKG_NAME = "com.example.codedroid"

    private const val FLAG_SIGN_CERTS = 0x08000000 
    private const val FLAG_GET_SIGS   = 0x00000040

    /**
     * Cek keamanan utama.
     */
    fun isSecurityCompromised(context: Context): Boolean {
        // Jika nilai hash masih default, artinya sedang dalam pengembangan.
        // Kita izinkan semua lingkungan (Root, Debug, Emulator) agar dev tidak terblokir.
        if (OFFICIAL_SIGNATURE_HASH.startsWith("YOUR_SHA256")) return false
        
        // --- MODE PRODUKSI (SANGAT KETAT) ---
        
        // 1. Cek Modifikasi Signature
        if (!verifySignature(context)) return true
        
        // 2. Cek Nama Paket & Cloning
        if (isTampered(context)) return true
        
        // 3. Cek Debugger & Debuggable APK
        if (isDebuggable(context) || Debug.isDebuggerConnected()) return true
        
        // 4. Cek Root & Emulator
        if (isRooted() || isEmulator()) return true
        
        return false
    }

    private fun isDebuggable(context: Context): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    private fun isRooted(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) return true

        val paths = arrayOf(
            "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su",
            "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su",
            "/system/sd/xbin/su", "/working/emulator/bin/su",
            "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su",
            "/system/xbin/magisk", "/system/bin/magisk"
        )
        for (path in paths) {
            if (java.io.File(path).exists()) return true
        }
        return false
    }

    private fun isTampered(context: Context): Boolean {
        // Cek Nama Paket
        if (context.packageName != EXPECTED_PKG_NAME) return true
        
        // Cek Parallel Space
        val dataDir = context.filesDir.path
        if (dataDir.contains("/com.lbe.parallel/") || 
            dataDir.contains("/com.parallel.space/") ||
            dataDir.contains("/com.dualspace") ||
            dataDir.contains("/com.vphonegaga") ) return true
            
        // Cek Hooking Frameworks
        return try {
            Class.forName("de.robv.android.xposed.XposedBridge")
            true
        } catch (_: Exception) {
            try {
                Class.forName("com.saurik.substrate.MS")
                true
            } catch (_: Exception) { 
                checkMapsForHook()
            }
        }
    }

    private fun checkMapsForHook(): Boolean {
        return try {
            val maps = java.io.File("/proc/self/maps").readText()
            maps.contains("frida-agent") || maps.contains("re.frida.server") || 
            maps.contains("libsubstrate.so") || maps.contains("libmagisk")
        } catch (_: Exception) { false }
    }

    private fun isEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.contains("generic")
                || Build.MODEL.contains("Emulator")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("vbox86p")
    }

    fun verifySignature(context: Context): Boolean {
        if (OFFICIAL_SIGNATURE_HASH.startsWith("YOUR_SHA256")) return true

        return try {
            val sigs = getSignatures(context) ?: return false
            var matched = false
            for (sig in sigs) {
                if (getSHA256(sig.toByteArray()) == OFFICIAL_SIGNATURE_HASH) {
                    matched = true
                    break
                }
            }
            matched
        } catch (_: Exception) { false }
    }

    @Suppress("DEPRECATION")
    private fun getSignatures(context: Context): Array<Signature>? {
        val pkgName = context.packageName
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.packageManager.getPackageInfo(pkgName, FLAG_SIGN_CERTS).signingInfo?.apkContentsSigners
        } else {
            context.packageManager.getPackageInfo(pkgName, FLAG_GET_SIGS).signatures
        }
    }

    private fun getSHA256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(data)
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }
}
