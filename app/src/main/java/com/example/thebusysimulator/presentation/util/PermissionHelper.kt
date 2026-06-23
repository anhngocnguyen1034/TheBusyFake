package com.example.thebusysimulator.presentation.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {
    /**
     * @deprecated Không còn sử dụng overlay permission vì app dùng Full Screen Intent (an toàn với Google Play)
     * Giữ lại function này để tương thích với code cũ, nhưng luôn trả về false
     */
    @Deprecated("App không còn sử dụng SYSTEM_ALERT_WINDOW permission")
    fun canDrawOverlays(context: Context): Boolean {
        // App không còn sử dụng overlay - dùng Full Screen Intent thay thế
        return false
        /* Code cũ (đã không dùng):
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true // Pre-Marshmallow, permission is granted by default
        }
        */
    }

    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    @Suppress("DEPRECATION")
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+): Cần runtime permission
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android < 13: Notification permission được cấp tự động
            true
        }
    }

    fun hasAllPermissions(context: Context): Boolean {
        // Chỉ cần camera permission, không cần overlay permission nữa
        return hasCameraPermission(context)
        // Code cũ: return canDrawOverlays(context) && hasCameraPermission(context)
    }

    /**
     * @deprecated Không còn cần request overlay permission vì app dùng Full Screen Intent
     */
    @Deprecated("App không còn sử dụng SYSTEM_ALERT_WINDOW permission")
    fun requestOverlayPermission(activity: Activity, requestCode: Int) {
        // Không làm gì - app không còn sử dụng overlay permission
        // Code cũ đã được comment để tham khảo:
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${activity.packageName}")
            )
            activity.startActivityForResult(intent, requestCode)
        }
        */
    }

    fun requestCameraPermission(activity: Activity, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA),
                requestCode
            )
        }
    }

    fun requestNotificationPermission(activity: Activity, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+): Cần request runtime permission
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                requestCode
            )
        }
        // Android < 13: Không cần request, permission được cấp tự động
    }

    fun requestAllPermissions(activity: Activity, cameraRequestCode: Int, overlayRequestCode: Int) {
        // Request camera permission first (runtime permission)
        if (!hasCameraPermission(activity)) {
            requestCameraPermission(activity, cameraRequestCode)
        }
        // Không còn request overlay permission - app dùng Full Screen Intent thay thế
        // Code cũ:
        // if (!canDrawOverlays(activity)) {
        //     requestOverlayPermission(activity, overlayRequestCode)
        // }
    }

    // region "Permanently denied" detection
    // Phát hiện trường hợp người dùng đã từ chối quá số lần khiến hệ thống không
    // hiện dialog xin quyền nữa -> để app tự hiện dialog dẫn người dùng vào Settings.

    private const val PERMISSION_PREFS = "permission_prefs"
    private fun requestedKey(permission: String) = "requested_$permission"

    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Đánh dấu là đã từng request quyền [permission] (lưu vào SharedPreferences).
     * Phải gọi ngay trước khi launch system permission request để [isPermanentlyDenied]
     * có thể phân biệt "chưa từng hỏi" với "đã hỏi và bị từ chối vĩnh viễn".
     */
    fun markPermissionRequested(context: Context, permission: String) {
        context.getSharedPreferences(PERMISSION_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(requestedKey(permission), true)
            .apply()
    }

    private fun hasRequestedBefore(context: Context, permission: String): Boolean {
        return context.getSharedPreferences(PERMISSION_PREFS, Context.MODE_PRIVATE)
            .getBoolean(requestedKey(permission), false)
    }

    /**
     * Trả về true khi quyền [permission] đã bị từ chối vĩnh viễn:
     * - Chưa được cấp, VÀ
     * - Đã từng request ít nhất một lần, VÀ
     * - Hệ thống không còn hiện rationale (tức sẽ không hiện dialog xin quyền nữa).
     *
     * Trong trường hợp này nên hiện dialog dẫn người dùng vào Settings thay vì
     * gọi launcher (vì launcher sẽ trả về denied ngay mà không hiện gì).
     */
    fun isPermanentlyDenied(activity: Activity, permission: String): Boolean {
        if (isPermissionGranted(activity, permission)) return false
        if (!hasRequestedBefore(activity, permission)) return false
        return !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    /** Mở màn hình thông tin ứng dụng trong Settings để người dùng tự cấp quyền. */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
    // endregion
}
