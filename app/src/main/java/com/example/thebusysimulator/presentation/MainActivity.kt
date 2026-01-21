package com.example.thebusysimulator.presentation

import android.Manifest
import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.compose.rememberNavController
import com.example.thebusysimulator.presentation.di.AppContainer
import com.example.thebusysimulator.presentation.navigation.NavGraph
import com.example.thebusysimulator.presentation.ui.theme.TheBusySimulatorTheme
import com.example.thebusysimulator.presentation.util.PermissionHelper
import com.example.thebusysimulator.data.datasource.FakeCallSettingsDataSource
import com.example.thebusysimulator.data.datasource.LanguageDataSource
import com.example.thebusysimulator.presentation.util.LanguageManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    companion object {
        const val REQUEST_OVERLAY_PERMISSION = 1001
        const val REQUEST_CAMERA_PERMISSION = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContainer.init(this)

        // Apply saved language
        val languageDataSource = LanguageDataSource(this)
        runBlocking {
            val languageCode = languageDataSource.languageCode.first()
            LanguageManager.setLanguage(this@MainActivity, languageCode)
        }

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val settingsDataSource = remember { FakeCallSettingsDataSource(context) }
            var themeMode by remember { mutableStateOf("system") }

            LaunchedEffect(Unit) {
                settingsDataSource.themeMode.collect { mode ->
                    themeMode = mode
                }
            }

            TheBusySimulatorTheme(themeMode = themeMode) {
                var permissionCheckKey by remember { mutableStateOf(0) }
                var hasOverlayPermission by remember {
                    mutableStateOf(PermissionHelper.canDrawOverlays(context))
                }
                var hasCameraPermission by remember {
                    mutableStateOf(PermissionHelper.hasCameraPermission(context))
                }

                // State to show settings dialog when permission is denied
                var showPermissionDeniedDialog by remember { mutableStateOf(false) }
                var permissionDeniedCount by remember { mutableStateOf(0) }

                // Camera permission launcher
                val cameraPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    hasCameraPermission = isGranted
                    if (!isGranted) {
                        permissionDeniedCount++
                        // Show dialog if permission is denied
                        showPermissionDeniedDialog = true
                    }
                    permissionCheckKey++
                }

                // Overlay permission launcher
                val overlayPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) {
                    hasOverlayPermission = PermissionHelper.canDrawOverlays(context)
                    permissionCheckKey++
                }

                // Re-check permissions when key changes
                LaunchedEffect(permissionCheckKey) {
                    hasOverlayPermission = PermissionHelper.canDrawOverlays(context)
                    hasCameraPermission = PermissionHelper.hasCameraPermission(context)
                }

                // KHÔNG tự động request quyền camera khi khởi động
                // Camera chỉ cần cho video call trong Fake Message
                // Sẽ request khi user thực sự cần (trong FakeVideoCallActivity)

                // Observe lifecycle to re-check permission on resume
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            permissionCheckKey++
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                // Navigation setup
                val navController = rememberNavController()
                val fakeCallViewModel = remember { AppContainer.createFakeCallViewModel() }
                val fakeMessageViewModel = remember { AppContainer.createFakeMessageViewModel() }
                val messageViewModel = remember { AppContainer.createMessageViewModel() }

                NavGraph(
                    navController = navController,
                    fakeCallViewModel = fakeCallViewModel,
                    fakeMessageViewModel = fakeMessageViewModel,
                    messageViewModel = messageViewModel,
                    hasOverlayPermission = hasOverlayPermission,
                    hasCameraPermission = hasCameraPermission,
                    onRequestOverlayPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val intent = android.content.Intent(
                                android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                android.net.Uri.parse("package:${context.packageName}")
                            )
                            overlayPermissionLauncher.launch(intent)
                        }
                    },
                    onRequestCameraPermission = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )

                // Show Permission Denied Dialog
                if (showPermissionDeniedDialog) {
                    PermissionDeniedDialog(
                        onDismiss = { showPermissionDeniedDialog = false },
                        onOpenSettings = {
                            showPermissionDeniedDialog = false
                            val intent = android.content.Intent(
                                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                android.net.Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionDeniedDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            androidx.compose.material3.Text(
                text = "Cần quyền Camera",
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        },
        text = {
            androidx.compose.material3.Text(
                text = "Ứng dụng cần quyền truy cập Camera để thực hiện cuộc gọi video giả. " +
                        "Vui lòng mở Cài đặt và cấp quyền Camera cho ứng dụng.",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = onOpenSettings
            ) {
                androidx.compose.material3.Text("Mở Cài đặt")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = onDismiss
            ) {
                androidx.compose.material3.Text("Để sau")
            }
        }
    )
}

