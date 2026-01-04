package com.example.thebusysimulator.presentation.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageHelper {
    private const val AVATAR_DIR = "avatars"
    
    /**
     * Copy image from URI to app's internal storage and return the file path
     */
    suspend fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val avatarDir = File(context.filesDir, AVATAR_DIR)
                if (!avatarDir.exists()) {
                    avatarDir.mkdirs()
                }
                
                val fileName = "avatar_${System.currentTimeMillis()}.jpg"
                val file = File(avatarDir, fileName)
                
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                
                file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    /**
     * Get file URI from file path
     */
    fun getFileUri(context: Context, filePath: String): Uri {
        val file = File(filePath)
        return Uri.fromFile(file)
    }
    
    /**
     * Delete avatar file
     */
    suspend fun deleteAvatarFile(context: Context, filePath: String?) {
        withContext(Dispatchers.IO) {
            try {
                filePath?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

