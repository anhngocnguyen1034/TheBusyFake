package com.example.thebusysimulator.presentation.ui.screen

import android.graphics.BitmapFactory
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.thebusysimulator.presentation.viewmodel.MessageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class EditorMode { DRAW, TEXT }

data class DrawPath(
    val offsets: List<Offset>,
    val color: Color,
    val strokeWidthPx: Float
)

data class TextOverlayItem(
    val text: String,
    val color: Color,
    val x: Float,
    val y: Float,
    val fontSizePx: Float
)

@Composable
fun ImageEditorScreen(
    navController: NavController,
    messageId: String,
    imagePath: String,
    viewModel: MessageViewModel
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val originalBitmap = remember(imagePath) {
        try { BitmapFactory.decodeFile(imagePath) } catch (_: Exception) { null }
    }

    if (originalBitmap == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    var mode by remember { mutableStateOf(EditorMode.DRAW) }
    var selectedColor by remember { mutableStateOf(Color(0xFFFF3B30)) }
    var strokeWidthDp by remember { mutableStateOf(8f) }
    var completedPaths by remember { mutableStateOf(listOf<DrawPath>()) }
    var currentOffsets by remember { mutableStateOf(listOf<Offset>()) }
    var textItems by remember { mutableStateOf(listOf<TextOverlayItem>()) }
    var inputText by remember { mutableStateOf("") }
    var containerSizePx by remember { mutableStateOf(Size.Zero) }
    var isSending by remember { mutableStateOf(false) }

    val imageDisplayBounds = remember(containerSizePx, originalBitmap.width, originalBitmap.height) {
        if (containerSizePx == Size.Zero) return@remember Rect(Offset.Zero, Size.Zero)
        val scale = minOf(
            containerSizePx.width / originalBitmap.width,
            containerSizePx.height / originalBitmap.height
        )
        val dispW = originalBitmap.width * scale
        val dispH = originalBitmap.height * scale
        val left = (containerSizePx.width - dispW) / 2f
        val top = (containerSizePx.height - dispH) / 2f
        Rect(Offset(left, top), Size(dispW, dispH))
    }

    val paletteColors = listOf(
        Color(0xFFFF3B30), Color(0xFFFF9500), Color(0xFFFFD60A),
        Color(0xFF34C759), Color(0xFF00C7BE), Color(0xFF007AFF),
        Color(0xFF5856D6), Color(0xFFFF2D55), Color(0xFFFFFFFF),
        Color(0xFF1C1C1E)
    )
    val strokeSizes = listOf(4f, 8f, 14f, 22f)

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.Close, "Cancel", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                // Draw / Text tabs
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    EditorModeTab("Draw", mode == EditorMode.DRAW) { mode = EditorMode.DRAW }
                    EditorModeTab("Text", mode == EditorMode.TEXT) { mode = EditorMode.TEXT }
                }
                Spacer(Modifier.weight(1f))
                // Undo
                IconButton(onClick = {
                    if (mode == EditorMode.DRAW && completedPaths.isNotEmpty()) {
                        completedPaths = completedPaths.dropLast(1)
                    } else if (mode == EditorMode.TEXT && textItems.isNotEmpty()) {
                        textItems = textItems.dropLast(1)
                    }
                }) {
                    Icon(Icons.Filled.Undo, "Undo", tint = Color.White)
                }
                // Send button
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isSending) Color.Gray else Color(0xFF007AFF))
                        .clickable(enabled = !isSending) {
                            isSending = true
                            scope.launch {
                                val saved = flattenAndSave(
                                    context = context,
                                    originalBitmap = originalBitmap,
                                    imageDisplayBounds = imageDisplayBounds,
                                    paths = completedPaths,
                                    textItems = textItems
                                )
                                if (saved != null) viewModel.sendChatMessage(messageId, "", saved)
                                navController.popBackStack()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Filled.Check, "Send", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
            }

            // ── Image + Drawing canvas ─────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .onSizeChanged { size ->
                        containerSizePx = Size(size.width.toFloat(), size.height.toFloat())
                    }
            ) {
                androidx.compose.foundation.Image(
                    painter = rememberAsyncImagePainter(imagePath),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(mode, selectedColor, strokeWidthDp) {
                            if (mode == EditorMode.DRAW) {
                                var head = Offset.Zero
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        head = offset
                                        currentOffsets = listOf(offset)
                                    },
                                    onDrag = { _, delta ->
                                        head += delta
                                        currentOffsets = currentOffsets + head
                                    },
                                    onDragEnd = {
                                        if (currentOffsets.isNotEmpty()) {
                                            val strokePx = strokeWidthDp * density.density
                                            completedPaths = completedPaths + DrawPath(currentOffsets, selectedColor, strokePx)
                                        }
                                        currentOffsets = emptyList()
                                    },
                                    onDragCancel = { currentOffsets = emptyList() }
                                )
                            } else {
                                detectTapGestures { offset ->
                                    if (inputText.isNotBlank()) {
                                        val fontSizePx = with(density) { 20.sp.toPx() }
                                        textItems = textItems + TextOverlayItem(
                                            text = inputText,
                                            color = selectedColor,
                                            x = offset.x,
                                            y = offset.y,
                                            fontSizePx = fontSizePx
                                        )
                                        inputText = ""
                                    }
                                }
                            }
                        }
                ) {
                    completedPaths.forEach { p -> drawStroke(p.offsets, p.color, p.strokeWidthPx) }
                    if (currentOffsets.size >= 1) {
                        drawStroke(currentOffsets, selectedColor, strokeWidthDp * density.density)
                    }
                }

                // Text overlays as Compose composables
                textItems.forEach { item ->
                    androidx.compose.material3.Text(
                        text = item.text,
                        color = item.color,
                        fontSize = with(LocalDensity.current) { item.fontSizePx.toSp() },
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.7f),
                                offset = Offset(1f, 1f),
                                blurRadius = 3f
                            )
                        ),
                        modifier = Modifier.absoluteOffset {
                            androidx.compose.ui.unit.IntOffset(item.x.toInt(), item.y.toInt())
                        }
                    )
                }
            }

            // ── Bottom toolbar ─────────────────────────────────────────
            if (mode == EditorMode.DRAW) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1C1C1E))
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        paletteColors.forEach { color ->
                            val isSelected = color == selectedColor
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 34.dp else 26.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(
                                        if (isSelected) Modifier.border(2.5.dp, Color.White, CircleShape)
                                        else if (color == Color(0xFF1C1C1E)) Modifier.border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                        else Modifier
                                    )
                                    .clickable { selectedColor = color }
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Brush", color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.labelSmall)
                        strokeSizes.forEach { size ->
                            val isSelected = size == strokeWidthDp
                            Box(
                                modifier = Modifier
                                    .size(minOf(size * 2f, 34f).dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) selectedColor
                                        else Color.White.copy(alpha = 0.35f)
                                    )
                                    .then(if (isSelected) Modifier.border(1.5.dp, Color.White, CircleShape) else Modifier)
                                    .clickable { strokeWidthDp = size }
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1C1C1E))
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        paletteColors.forEach { color ->
                            val isSelected = color == selectedColor
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 30.dp else 22.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(
                                        if (isSelected) Modifier.border(2.dp, Color.White, CircleShape)
                                        else if (color == Color(0xFF1C1C1E)) Modifier.border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                        else Modifier
                                    )
                                    .clickable { selectedColor = color }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                "Nhập text rồi chạm vào ảnh để đặt…",
                                color = Color.White.copy(alpha = 0.35f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.White.copy(alpha = 0.08f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
                            focusedBorderColor = selectedColor,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            cursorColor = selectedColor
                        )
                    )
                    Text(
                        text = if (inputText.isBlank()) "Nhập text ở trên, rồi chạm vào vị trí trên ảnh"
                        else "Chạm vào ảnh để đặt: \"$inputText\"",
                        color = if (inputText.isBlank()) Color.White.copy(alpha = 0.4f) else selectedColor.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (inputText.isBlank()) FontWeight.Normal else FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawStroke(offsets: List<Offset>, color: Color, strokeWidthPx: Float) {
    if (offsets.isEmpty()) return
    if (offsets.size == 1) {
        drawCircle(color = color, radius = strokeWidthPx / 2f, center = offsets[0])
        return
    }
    val path = Path()
    path.moveTo(offsets[0].x, offsets[0].y)
    for (i in 1 until offsets.size) path.lineTo(offsets[i].x, offsets[i].y)
    drawPath(path = path, color = color, style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

@Composable
private fun EditorModeTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.Black else Color.White.copy(alpha = 0.65f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

private suspend fun flattenAndSave(
    context: android.content.Context,
    originalBitmap: android.graphics.Bitmap,
    imageDisplayBounds: Rect,
    paths: List<DrawPath>,
    textItems: List<TextOverlayItem>
): String? = withContext(Dispatchers.IO) {
    try {
        if (imageDisplayBounds.width <= 0f) return@withContext null

        val scaleX = originalBitmap.width / imageDisplayBounds.width
        val scaleY = originalBitmap.height / imageDisplayBounds.height

        val result = originalBitmap.copy(android.graphics.Bitmap.Config.ARGB_8888, true)
        val canvas = android.graphics.Canvas(result)

        // Draw paths
        val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            style = AndroidPaint.Style.STROKE
            strokeCap = AndroidPaint.Cap.ROUND
            strokeJoin = AndroidPaint.Join.ROUND
        }
        for (dp in paths) {
            if (dp.offsets.isEmpty()) continue
            paint.color = dp.color.toArgb()
            paint.strokeWidth = dp.strokeWidthPx * scaleX
            val ap = AndroidPath()
            dp.offsets.forEachIndexed { i, o ->
                val ix = (o.x - imageDisplayBounds.left) * scaleX
                val iy = (o.y - imageDisplayBounds.top) * scaleY
                if (i == 0) ap.moveTo(ix, iy) else ap.lineTo(ix, iy)
            }
            canvas.drawPath(ap, paint)
        }

        // Draw text
        val tp = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            style = AndroidPaint.Style.FILL
            isFakeBoldText = true
        }
        for (item in textItems) {
            val ix = (item.x - imageDisplayBounds.left) * scaleX
            val iy = (item.y - imageDisplayBounds.top) * scaleY
            tp.color = item.color.toArgb()
            tp.textSize = item.fontSizePx * scaleX
            tp.setShadowLayer(3f * scaleX, scaleX, scaleY, android.graphics.Color.argb(180, 0, 0, 0))
            canvas.drawText(item.text, ix, iy, tp)
        }

        val dir = File(context.filesDir, "chat_images")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "edited_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            result.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, out)
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
