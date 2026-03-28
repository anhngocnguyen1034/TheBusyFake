package com.example.thebusysimulator.presentation

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.thebusysimulator.presentation.ui.theme.TheBusySimulatorTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.example.thebusysimulator.data.datasource.FakeCallSettingsDataSource
import com.example.thebusysimulator.presentation.util.FlashHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import com.example.thebusysimulator.presentation.di.AppContainer
import androidx.compose.ui.platform.LocalContext
import com.example.thebusysimulator.R

class FakeVideoCallActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var flashHelper: FlashHelper? = null
    private val settingsScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        turnScreenOnAndKeyguard()
        enableEdgeToEdge()

        val callerName = intent.getStringExtra("caller_name") ?: "Người lạ"
        val callerNumber = intent.getStringExtra("caller_number") ?: "0909 *** ***"

        // Setup Audio & Vibration
        setupAudioAndVibration()
        
        // Initialize flash helper
        flashHelper = FlashHelper(this)
        
        startRinging()
        
        // Start flash if enabled
        startFlashIfEnabled()

        setContent {
            TheBusySimulatorTheme(themeMode = "system") {
                FakeVideoCallScreen(
                    callerName = callerName,
                    callerNumber = callerNumber,
                    onAccept = {
                        stopRinging()
                    },
                    onDecline = {
                        stopRinging()
                        finish()
                    },
                    lifecycleOwner = this
                )
            }
        }
    }

    private fun setupAudioAndVibration() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun turnScreenOnAndKeyguard() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
    }

    private fun startRinging() {
        requestAudioFocus()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) { e.printStackTrace() }
        startVibrationIfEnabled()
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                ).build()
            audioFocusRequest?.let { audioManager?.requestAudioFocus(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(null, AudioManager.STREAM_RING, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        }
    }

    private fun startVibration() {
        val pattern = longArrayOf(0, 1000, 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun startVibrationIfEnabled() {
        settingsScope.launch {
            try {
                val settingsDataSource = FakeCallSettingsDataSource(applicationContext)
                val vibrationEnabled = settingsDataSource.vibrationEnabled.first()
                if (vibrationEnabled) {
                    startVibration()
                }
            } catch (e: Exception) {
                android.util.Log.e("FakeVideoCallActivity", "Error starting vibration", e)
            }
        }
    }

    private fun startFlashIfEnabled() {
        settingsScope.launch {
            try {
                val settingsDataSource = FakeCallSettingsDataSource(applicationContext)
                val flashEnabled = settingsDataSource.flashEnabled.first()
                if (flashEnabled) {
                    flashHelper?.startFlashing()
                }
            } catch (e: Exception) {
                android.util.Log.e("FakeVideoCallActivity", "Error starting flash", e)
            }
        }
    }

    private fun stopRinging() {
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        
        // Stop flash
        flashHelper?.stopFlashing()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRinging()
        flashHelper?.release()
        settingsScope.cancel()
    }
}
@Composable
fun FakeVideoCallScreen(
    callerName: String,
    callerNumber: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    lifecycleOwner: LifecycleOwner
) {
    var isInCall by remember { mutableStateOf(false) }

    if (isInCall) {
        InVideoCallScreen(
            callerName = callerName,
            onEndCall = { onDecline() },
            lifecycleOwner = lifecycleOwner
        )
    } else {
        IncomingVideoCallScreen(
            callerName = callerName,
            callerNumber = callerNumber,
            onAnswer = {
                isInCall = true
                onAccept()
            },
            onDecline = onDecline
        )
    }
}

// --- INCOMING SCREEN (Giao diện chờ nghe máy) ---
@Composable
fun IncomingVideoCallScreen(
    callerName: String,
    callerNumber: String,
    onAnswer: () -> Unit,
    onDecline: () -> Unit
) {
    // Background Gradient mượt mà
    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A2980), // Xanh đậm
            Color(0xFF26D0CE)  // Xanh ngọc
        )
    )

    // Animation cho Avatar (Pulse effect)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush),
        contentAlignment = Alignment.Center
    ) {
        // --- CALLER INFO (TOP) ---
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Avatar với hiệu ứng sóng
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(scale)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                )
                Icon(
                    imageVector = Icons.Rounded.AccountCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(140.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Tên với icon verify nếu là người nổi tiếng
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = callerName,
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    // Kiểm tra xem có phải người nổi tiếng không
                    var isVerified by remember { mutableStateOf(false) }

                    LaunchedEffect(callerName) {
                        isVerified = AppContainer.messageRepository.isContactVerified(callerName)
                    }
                    
                    if (isVerified) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(R.drawable.ic_verify),
                            contentDescription = stringResource(R.string.verified),
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.video_call_ellipsis),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // --- ACTION BUTTONS (BOTTOM) ---
        // Sử dụng 2 nút to rõ ràng thay vì swipe phức tạp (Trend mới)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 80.dp, start = 40.dp, end = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Decline Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FilledIconButton(
                    onClick = onDecline,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFFF3B30)),
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_call_end),
                        contentDescription = stringResource(R.string.decline),
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.decline), color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            }

            // Accept Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FilledIconButton(
                    onClick = onAnswer,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF34C759)),
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_call),
                        contentDescription = stringResource(R.string.answer),
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.answer), color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            }
        }
    }
}

// --- IN-CALL SCREEN (Giao diện đang gọi) ---
@Composable
fun InVideoCallScreen(
    callerName: String,
    onEndCall: () -> Unit,
    lifecycleOwner: LifecycleOwner
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }

    // State cho thời gian gọi
    var callDuration by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(1000)
            callDuration++
        }
    }
    val minutes = callDuration / 60
    val seconds = callDuration % 60
    val durationText = String.format("%02d:%02d", minutes, seconds)

    // State cho vị trí camera (Draggable)
    val cameraWidth = 120.dp
    val cameraHeight = 160.dp
    val cameraWidthPx = with(density) { cameraWidth.toPx() }
    val cameraHeightPx = with(density) { cameraHeight.toPx() }

    // Mặc định góc phải trên
    var offsetX by remember { mutableFloatStateOf(screenWidth - cameraWidthPx - 40f) }
    var offsetY by remember { mutableFloatStateOf(150f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. FAKE CALLER VIDEO (Nền sau)
        // Vì không có video thật, ta dùng gradient động hoặc hình nền xịn
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF2C3E50), Color(0xFF4CA1AF))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = callerName.take(1).uppercase(),
                        fontSize = 100.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(R.string.camera_off),
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // 2. HEADER INFO
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = callerName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.shadow(4.dp)
            )
            Text(
                text = durationText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.shadow(4.dp)
            )
        }

        // 3. DRAGGABLE SELF CAMERA PREVIEW
        var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }
        var isFrontCamera by remember { mutableStateOf(true) }
        var isMicOn by remember { mutableStateOf(true) }
        var isVideoOn by remember { mutableStateOf(true) }
        val previewViewRef = remember { mutableStateOf<PreviewView?>(null) }
        val context = androidx.compose.ui.platform.LocalContext.current
        var hasCameraPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.CAMERA
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            )
        }

        var showSettingsDialog by remember { mutableStateOf(false) }
        val cameraPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            hasCameraPermission = isGranted
            if (!isGranted) {
                val activity = context as? android.app.Activity
                val shouldShowRationale = activity?.let { androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(it, android.Manifest.permission.CAMERA) } ?: false
                if (!shouldShowRationale) {
                    showSettingsDialog = true
                }
            }
        }

        val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
        DisposableEffect(lifecycle) {
            val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                    hasCameraPermission = ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.CAMERA
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                }
            }
            lifecycle.addObserver(observer)
            onDispose {
                lifecycle.removeObserver(observer)
            }
        }

        // Rebind camera khi isFrontCamera thay đổi
        LaunchedEffect(isFrontCamera, hasCameraPermission, isVideoOn) {
            val provider = cameraProvider ?: return@LaunchedEffect
            val previewView = previewViewRef.value ?: return@LaunchedEffect
            provider.unbindAll()
            if (!hasCameraPermission || !isVideoOn) return@LaunchedEffect
            val cameraSelector = if (isFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            try {
                provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview
                )
            } catch (e: Exception) { e.printStackTrace() }
        }

        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text("Cấp quyền Camera") },
                text = { Text("Để sử dụng tính năng này, vui lòng cấp quyền camera trong Cài đặt.") },
                confirmButton = {
                    TextButton(onClick = {
                        showSettingsDialog = false
                        val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }) {
                        Text("Cài đặt")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSettingsDialog = false }) {
                        Text("Hủy")
                    }
                }
            )
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .width(cameraWidth)
                .height(cameraHeight)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount.x).coerceIn(0f, screenWidth - cameraWidthPx)
                        offsetY = (offsetY + dragAmount.y).coerceIn(0f, screenHeight - cameraHeightPx)
                    }
                }
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                if (hasCameraPermission && isVideoOn) {
                    // Có quyền camera và đang bật video - hiển thị camera preview
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            previewViewRef.value = previewView
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val provider = cameraProviderFuture.get()
                                cameraProvider = provider
                                val cameraSelector = if (isFrontCamera) {
                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                } else {
                                    CameraSelector.DEFAULT_BACK_CAMERA
                                }
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                try {
                                    provider.unbindAll()
                                    provider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview
                                    )
                                } catch (e: Exception) { e.printStackTrace() }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Không có quyền camera hoặc đang tắt video - hiển thị placeholder với icon
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                if (!hasCameraPermission) {
                                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                } else {
                                    isVideoOn = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccountBox,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.camera_off_label),
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
                // Viền mỏng
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(2.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                )
            }
        }
        DisposableEffect(Unit) { onDispose { cameraProvider?.unbindAll() } }

        // 4. BOTTOM CONTROL BAR
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 40.dp, start = 20.dp, end = 20.dp)
        ) {
            // Thanh công cụ mờ (Glassmorphism)
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.fillMaxWidth().height(80.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute Button
                    CallControlButton(
                        painter = painterResource(if (isMicOn) R.drawable.ic_mic else R.drawable.ic_mic_off),
                        isActive = isMicOn,
                        onClick = { isMicOn = !isMicOn }
                    )

                    // Video Button
                    CallControlButton(
                        painter = painterResource(if (isVideoOn && hasCameraPermission) R.drawable.ic_video_call else R.drawable.ic_camera_off), 
                        isActive = isVideoOn && hasCameraPermission,
                        onClick = {
                            if (!hasCameraPermission) {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            } else {
                                isVideoOn = !isVideoOn
                            }
                        }
                    )

                    // Switch Camera
                    CallControlButton(
                        painter = painterResource(R.drawable.switch_camera),
                        isActive = !isFrontCamera
                    ) {
                        isFrontCamera = !isFrontCamera
                    }

                    // End Call
                    FilledIconButton(
                        onClick = onEndCall,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFFF3B30)),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_call_end),
                            contentDescription = stringResource(R.string.end),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CallControlButton(
    painter: Painter,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = if (isActive) Color.White.copy(alpha = 0.2f) else Color.Transparent
    val iconColor = if (isActive) Color.White else Color.White.copy(alpha = 0.5f)

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .background(bgColor, CircleShape)
            .size(48.dp)
    ) {
        Icon(painter = painter, contentDescription = null, tint = iconColor)    }
}