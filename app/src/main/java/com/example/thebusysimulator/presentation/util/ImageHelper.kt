package com.example.thebusysimulator.presentation.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageHelper {
    private const val AVATAR_DIR = "avatars"
    private const val CHAT_IMAGES_DIR = "chat_images"
    
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
     * Save chat image from URI to app's internal storage and return the file path
     */
    suspend fun saveChatImageToInternalStorage(context: Context, uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val chatImagesDir = File(context.filesDir, CHAT_IMAGES_DIR)
                if (!chatImagesDir.exists()) {
                    chatImagesDir.mkdirs()
                }
                
                val fileName = "chat_image_${System.currentTimeMillis()}.jpg"
                val file = File(chatImagesDir, fileName)
                
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
     * Get file URI from file path using FileProvider
     */
    fun getFileUri(context: Context, filePath: String): Uri? {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Get content URI from file path for image loading
     */
    fun getImageUri(context: Context, filePath: String?): Uri? {
        if (filePath == null) return null
        return try {
            if (filePath.startsWith("/")) {
                // Internal storage file path
                getFileUri(context, filePath)
            } else {
                // Already a URI string
                Uri.parse(filePath)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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

