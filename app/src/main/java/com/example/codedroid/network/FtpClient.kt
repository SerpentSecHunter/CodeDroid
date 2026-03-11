package com.example.codedroid.network

import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import java.io.File
import java.io.FileOutputStream

data class FtpConfig(
    val host    : String,
    val port    : Int    = 21,
    val user    : String = "anonymous",
    val password: String = ""
)

data class FtpResult(
    val success: Boolean,
    val message: String
)

object FtpHelper {
    private var client: FTPClient? = null
    var currentPath = "/"
    val isConnected get() = client?.isConnected == true

    fun connect(config: FtpConfig): FtpResult {
        return try {
            client = FTPClient().apply {
                connect(config.host, config.port)
                login(config.user, config.password)
                enterLocalPassiveMode()
                setFileType(FTP.BINARY_FILE_TYPE)
                currentPath = printWorkingDirectory() ?: "/"
            }
            FtpResult(true, "Terhubung ke ${config.host}")
        } catch (e: Exception) {
            FtpResult(false, "Gagal: ${e.message}")
        }
    }

    fun disconnect() {
        try { client?.logout(); client?.disconnect() } catch (_: Exception) {}
        client = null
        currentPath = "/"
    }

    fun listFiles(): List<FTPFile> = try {
        client?.listFiles(currentPath)?.toList() ?: emptyList()
    } catch (_: Exception) { emptyList() }

    fun changeDirectory(path: String): Boolean = try {
        if (client?.changeWorkingDirectory(path) == true) {
            currentPath = client?.printWorkingDirectory() ?: path
            true
        } else false
    } catch (_: Exception) { false }

    fun downloadFile(remotePath: String, localFile: File): Boolean = try {
        localFile.parentFile?.mkdirs()
        FileOutputStream(localFile).use { out ->
            client?.retrieveFile(remotePath, out)
        }
        true
    } catch (_: Exception) { false }
}