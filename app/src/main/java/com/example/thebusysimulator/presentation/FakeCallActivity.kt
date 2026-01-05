package com.example.thebusysimulator.presentation

import android.annotation.SuppressLint
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
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.thebusysimulator.presentation.ui.theme.TheBusySimulatorTheme
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import com.example.thebusysimulator.data.datasource.FakeCallSettingsDataSource
import com.example.thebusysimulator.presentation.util.FlashHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class FakeCallActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var flashHelper: FlashHelper? = null
    private val settingsScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Turn screen on and unlock
        turnScreenOnAndKeyguard()
        
        // Make fullscreen
        enableEdgeToEdge()
        
        // Get caller info from intent
        val callerName = intent.getStringExtra(EXTRA_CALLER_NAME) ?: "Unknown"
        val callerNumber = intent.getStringExtra(EXTRA_CALLER_NUMBER) ?: "Unknown"
        val action = intent.getStringExtra(EXTRA_ACTION) // "accept" or "decline"
        
        // Nếu action là "decline", đóng activity ngay
        if (action == "decline") {
            finish()
            return
        }

        // Initialize audio and vibration
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        // Initialize flash helper
        flashHelper = FlashHelper(this)

        // Start ringing
        startRinging()
        
        // Start flash if enabled
        startFlashIfEnabled()

        setContent {
            TheBusySimulatorTheme(darkTheme = isSystemInDarkTheme()) {
                FakeCallScreen(
                    callerName = callerName,
                    callerNumber = callerNumber,
                    onAccept = {
                        stopRinging()
                        // Navigate to in-call screen
                    },
                    onDecline = {
                        stopRinging()
                        finish()
                    }
                )
            }
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
        // Request audio focus
        requestAudioFocus()
        
        // Play ringtone
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
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Start vibration if enabled
        startVibrationIfEnabled()
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(audioAttributes)
                .build()
            audioManager?.requestAudioFocus(audioFocusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                null,
                AudioManager.STREAM_RING,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
        }
    }

    private fun startVibration() {
        val pattern = longArrayOf(0, 1000, 1000)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createWaveform(pattern, 0)
            vibrator?.vibrate(vibrationEffect)
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
                android.util.Log.e("FakeCallActivity", "Error starting vibration", e)
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
                android.util.Log.e("FakeCallActivity", "Error starting flash", e)
            }
        }
    }

    private fun stopRinging() {
        mediaPlayer?.release()
        mediaPlayer = null
        
        vibrator?.cancel()
        
        // Stop flash
        flashHelper?.stopFlashing()
        
        // Release audio focus
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager?.abandonAudioFocusRequest(it)
            }
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

    companion object {
        const val EXTRA_CALLER_NAME = "caller_name"
        const val EXTRA_CALLER_NUMBER = "caller_number"
        const val EXTRA_ACTION = "action" // "accept" or "decline"
    }
}

@Composable
fun FakeCallScreen(
    callerName: String,
    callerNumber: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    var isInCall by remember { mutableStateOf(false) }

    if (isInCall) {
        InCallScreen(
            callerName = callerName,
            callerNumber = callerNumber,
            onEndCall = { onDecline() }
        )
    } else {
        IncomingCallScreen(
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

@Composable
fun IncomingCallScreen(
    callerName: String,
    callerNumber: String,
    onAnswer: () -> Unit,
    onDecline: () -> Unit
) {
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF2C3E50), Color(0xFF000000))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        // 1. INFO NGƯỜI GỌI (Giữ nguyên như cũ)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .shadow(12.dp, CircleShape)
                    .background(Color(0xFF34495E), CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(80.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = callerName,
                    fontSize = 36.sp, // Font to hơn
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = callerNumber,
                    fontSize = 20.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 60.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // Text hướng dẫn nhỏ
            Text(
                text = "Swipe icon to respond",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Hàng chứa 2 nút: Từ chối và Trả lời với track chung liên tục
            // Sử dụng widthIn để giới hạn kích thước trên màn hình lớn (tablet)
            val configuration = LocalConfiguration.current
            val maxRowWidth = (configuration.screenWidthDp * 0.9f).dp.coerceAtMost(600.dp)
            
            // Track chung liên tục cho cả 2 nút
            val buttonSizeDp = (configuration.screenWidthDp * 0.15f).dp.coerceIn(64.dp, 80.dp)
            val trackHeightDp = buttonSizeDp
            
            Box(
                modifier = Modifier
                    .widthIn(max = maxRowWidth)
                    .fillMaxWidth()
                    .height(trackHeightDp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Nút TỪ CHỐI (Trái -> Phải)
                    SwipeCallButton(
                        icon = Icons.Filled.Call,
                        color = Color(0xFFFF3B30),
                        direction = SwipeDirection.LEFT_TO_RIGHT,
                        onTriggered = onDecline,
                        showTrack = false // Không hiển thị track riêng vì đã có track chung
                    )

                    // Nút TRẢ LỜI (Phải -> Trái)
                    SwipeCallButton(
                        icon = Icons.Filled.Call,
                        color = Color(0xFF4CD964),
                        direction = SwipeDirection.RIGHT_TO_LEFT,
                        onTriggered = onAnswer,
                        showTrack = false // Không hiển thị track riêng vì đã có track chung
                    )
                }
                
                // Mũi tên hướng dẫn ở giữa track (hiển thị phía trên)
                val infiniteTransition = rememberInfiniteTransition(label = "shared_arrow_alpha")
                val arrowAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.2f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ), label = "arrow_alpha"
                )
                
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy((-4).dp)
                ) {
                    // Mũi tên trái (cho nút từ chối)
                    repeat(2) { index ->
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer { alpha = (arrowAlpha + (index * 0.2f)) % 1f }
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Mũi tên phải (cho nút trả lời)
                    repeat(2) { index ->
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer { alpha = (arrowAlpha + (index * 0.2f)) % 1f }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InCallScreen(
    callerName: String,
    callerNumber: String,
    onEndCall: () -> Unit
) {
    var callDuration by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callDuration++
        }
    }
    
    val minutes = callDuration / 60
    val seconds = callDuration % 60
    val durationText = String.format("%02d:%02d", minutes, seconds)

    // Background Gradient mờ ảo hơn lúc gọi đến
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF141E30), Color(0xFF243B55))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        // Caller Info nhỏ gọn hơn
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
             Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.Gray.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = callerName.take(1).uppercase(),
                    fontSize = 32.sp,
                    color = Color.White
                )
            }
            Text(
                text = callerName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = callerNumber,
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = durationText,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Grid các nút chức năng giả (Dummy Buttons)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                DummyCallAction(icon = Icons.Filled.Close, text = "Mute")
                DummyCallAction(icon = Icons.Filled.Menu, text = "Keypad")
                DummyCallAction(icon = Icons.Default.ThumbUp, text = "Speaker")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                DummyCallAction(icon = Icons.Filled.Add, text = "Add Call")
                DummyCallAction(icon = Icons.Filled.Call, text = "FaceTime")
                DummyCallAction(icon = Icons.Filled.Person, text = "Contacts")
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Waveform Animation
        Row(
            modifier = Modifier.height(40.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(8) { index ->
                AnimatedWaveBar(delay = index * 80)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // End Call Button
        FloatingActionButton(
            onClick = onEndCall,
            modifier = Modifier.size(72.dp),
            containerColor = Color(0xFFFF3B30), // Đỏ tươi
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "End Call",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

enum class SwipeDirection {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun SwipeCallButton(
    icon: ImageVector,
    color: Color,
    direction: SwipeDirection,
    onTriggered: () -> Unit,
    showTrack: Boolean = true // Mặc định hiển thị track riêng
) {
    // 1. Lấy Density và Configuration để tính toán responsive
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    // Kích thước responsive - tính toán dựa trên chiều rộng màn hình
    // Button size: 15% chiều rộng màn hình, giới hạn từ 64dp đến 80dp
    val buttonSizeDp = (screenWidthDp * 0.15f).dp.coerceIn(64.dp, 80.dp)
    
    // Track width: 38% chiều rộng màn hình, với min 200dp và max 280dp
    // Đảm bảo có đủ không gian vuốt trên mọi thiết bị (nhỏ, vừa, lớn)
    val trackWidthDp = (screenWidthDp * 0.38f).dp.coerceIn(200.dp, 280.dp)

    // Chuyển đổi sang Pixel để tính toán logic kéo
    val buttonSizePx = with(density) { buttonSizeDp.toPx() }
    val trackWidthPx = with(density) { trackWidthDp.toPx() }
    val maxDragDistancePx = trackWidthPx - buttonSizePx

    // Ngưỡng để ẩn hiệu ứng loang (10dp đổi sang px)
    val hidePulseThresholdPx = with(density) { 10.dp.toPx() }

    // Animation state cho nút kéo
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // --- SETUP ANIMATION ---
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_infinity")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 2.0f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing)),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing)),
        label = "pulse_alpha"
    )

    val arrowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "arrow_alpha"
    )

    Box(
        contentAlignment = Alignment.Center
    ) {
        if (abs(offsetX.value) < hidePulseThresholdPx) {
            Canvas(
                modifier = Modifier
                    .width(trackWidthDp)
                    .height(buttonSizeDp)
            ) {
                val circleCenter = if (direction == SwipeDirection.LEFT_TO_RIGHT) {
                    Offset(size.height / 2, size.height / 2)
                } else {
                    Offset(size.width - (size.height / 2), size.height / 2)
                }

                drawCircle(
                    color = color.copy(alpha = pulseAlpha),
                    radius = (size.height / 2) * pulseScale,
                    center = circleCenter
                )
                drawCircle(
                    color = color.copy(alpha = pulseAlpha * 0.5f),
                    radius = (size.height / 2) * (pulseScale * 0.7f),
                    center = circleCenter
                )
            }
        }

        Box(
            modifier = Modifier
                .width(trackWidthDp)
                .height(buttonSizeDp),
            contentAlignment = if (direction == SwipeDirection.LEFT_TO_RIGHT) Alignment.CenterStart else Alignment.CenterEnd
        ) {
            // Background track - chỉ hiển thị khi showTrack = true
            if (showTrack) {
                Box(
                    modifier = Modifier
                        .width(trackWidthDp)
                        .height(buttonSizeDp)
                        .background(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(buttonSizeDp / 2)
                        )
                )
            }
            
            // Progress indicator - đã ẩn theo yêu cầu (không hiển thị màu xanh/đỏ khi vuốt)
            
            // Mũi tên - chỉ hiển thị khi showTrack = true (hiện tại không hiển thị vì showTrack = false)
            if (showTrack) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy((-4).dp)
                ) {
                    val arrowIcon = if (direction == SwipeDirection.LEFT_TO_RIGHT)
                        Icons.Default.KeyboardArrowRight else Icons.Default.KeyboardArrowLeft

                    repeat(3) { index ->
                        Icon(
                            imageVector = arrowIcon,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer { alpha = (arrowAlpha + (index * 0.2f)) % 1f }
                        )
                    }
                }
            }

            // --- LAYER 3: KNOB (NÚT KÉO) ---
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .size(buttonSizeDp)
                    .shadow(8.dp, CircleShape)
                    .background(color, CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                // Giảm ngưỡng từ 70% xuống 60% để dễ kích hoạt hơn
                                val threshold = maxDragDistancePx * 0.6f
                                val isTriggered = when (direction) {
                                    SwipeDirection.LEFT_TO_RIGHT -> offsetX.value > threshold
                                    SwipeDirection.RIGHT_TO_LEFT -> offsetX.value < -threshold
                                }

                                if (isTriggered) {
                                    onTriggered()
                                } else {
                                    scope.launch {
                                        // SỬA LỖI: Chỉ định rõ kiểu Float cho spring animation nếu cần thiết
                                        offsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring<Float>(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        )
                                    }
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val newOffset = offsetX.value + dragAmount.x
                                scope.launch {
                                    val clampedOffset = when (direction) {
                                        SwipeDirection.LEFT_TO_RIGHT -> newOffset.coerceIn(0f, maxDragDistancePx)
                                        SwipeDirection.RIGHT_TO_LEFT -> newOffset.coerceIn(-maxDragDistancePx, 0f)
                                    }
                                    offsetX.snapTo(clampedOffset)
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                val iconScale = 1f + (pulseScale - 1f) * 0.1f
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .scale(iconScale)
                )
            }
        }
    }
}

// Component phụ cho nút chức năng giả
@Composable
fun DummyCallAction(icon: ImageVector, text: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Color.White.copy(alpha = 0.15f), CircleShape), // Glassmorphism nhẹ
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 12.sp
        )
    }
}

@Composable
fun AnimatedWaveBar(delay: Int) {
    var animatedHeight by remember { mutableStateOf(8.dp) }
    
    LaunchedEffect(Unit) {
        delay(delay.toLong())
        while (true) {
            animatedHeight = (8..40).random().dp
            delay((100..300).random().toLong())
            animatedHeight = 8.dp
            delay((100..300).random().toLong())
        }
    }
    
    Box(
        modifier = Modifier
            .width(6.dp)
            .height(animatedHeight)
            .background(
                Color.White.copy(alpha = 0.6f),
                RoundedCornerShape(3.dp)
            )
    )
}

