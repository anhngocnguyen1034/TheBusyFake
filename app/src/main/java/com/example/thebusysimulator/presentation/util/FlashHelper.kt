package com.example.thebusysimulator.presentation.util

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Helper class để điều khiển flash nháy khi có cuộc gọi đến
 */
class FlashHelper(private val context: Context) {
    private val cameraManager: CameraManager? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        } else {
            null
        }
    }

    private var flashJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val _isFlashing = MutableStateFlow(false)
    val isFlashing: StateFlow<Boolean> = _isFlashing.asStateFlow()

    /**
     * Kiểm tra xem thiết bị có hỗ trợ flash không
     */
    fun hasFlash(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                cameraManager?.let { manager ->
                    val cameraIds = manager.cameraIdList
                    cameraIds.any { id ->
                        manager.getCameraCharacteristics(id)
                            .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                    }
                } ?: false
            } catch (e: Exception) {
                Log.e("FlashHelper", "Error checking flash availability", e)
                false
            }
        } else {
            false
        }
    }

    /**
     * Bắt đầu nháy flash với pattern: on 500ms, off 500ms
     */
    fun startFlashing() {
        if (!hasFlash()) {
            Log.w("FlashHelper", "Flash not available on this device")
            return
        }

        if (_isFlashing.value) {
            Log.d("FlashHelper", "Flash is already flashing")
            return
        }

        _isFlashing.value = true
        flashJob = coroutineScope.launch {
            try {
                while (isActive && _isFlashing.value) {
                    setFlashlight(true)
                    delay(500) // Flash on for 500ms
                    if (isActive && _isFlashing.value) {
                        setFlashlight(false)
                        delay(500) // Flash off for 500ms
                    }
                }
            } catch (e: Exception) {
                Log.e("FlashHelper", "Error in flash loop", e)
            } finally {
                // Đảm bảo flash được tắt khi dừng
                setFlashlight(false)
            }
        }
        Log.d("FlashHelper", "Flash started")
    }

    /**
     * Dừng nháy flash
     */
    fun stopFlashing() {
        if (!_isFlashing.value) {
            return
        }

        _isFlashing.value = false
        flashJob?.cancel()
        flashJob = null
        
        // Tắt flash ngay lập tức
        setFlashlight(false)
        Log.d("FlashHelper", "Flash stopped")
    }

    /**
     * Bật/tắt flash trực tiếp
     */
    private fun setFlashlight(enabled: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                cameraManager?.let { manager ->
                    val cameraIds = manager.cameraIdList
                    for (id in cameraIds) {
                        val characteristics = manager.getCameraCharacteristics(id)
                        val hasFlash = characteristics.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                        val lensFacing = characteristics.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)
                        
                        // Ưu tiên camera sau (back camera) vì thường có flash tốt hơn
                        if (hasFlash && lensFacing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK) {
                            manager.setTorchMode(id, enabled)
                            return
                        }
                    }
                    // Nếu không tìm thấy camera sau, thử camera trước
                    for (id in cameraIds) {
                        val characteristics = manager.getCameraCharacteristics(id)
                        val hasFlash = characteristics.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                        if (hasFlash) {
                            manager.setTorchMode(id, enabled)
                            return
                        }
                    }
                }
            } catch (e: CameraAccessException) {
                Log.e("FlashHelper", "Camera access error", e)
            } catch (e: Exception) {
                Log.e("FlashHelper", "Error setting flashlight", e)
            }
        }
    }

    /**
     * Cleanup khi không cần dùng nữa
     */
    fun release() {
        stopFlashing()
        coroutineScope.cancel()
    }
}

