package com.example.thebusysimulator.presentation.ui.screen

import android.graphics.BitmapFactory
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
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

// ─── Enums & data classes ────────────────────────────────────────────────────

enum class EditorMode { DRAW, TEXT, CROP }

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

// ─── Main screen ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
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

    // ── Bitmap state (crop replaces it) ──────────────────────────────────────
    var currentBitmap by remember { mutableStateOf(originalBitmap) }

    // ── Draw / Text state ────────────────────────────────────────────────────
    var mode by remember { mutableStateOf(EditorMode.DRAW) }
    var selectedColor by remember { mutableStateOf(Color(0xFFFF3B30)) }
    var strokeWidthDp by remember { mutableStateOf(8f) }
    var completedPaths by remember { mutableStateOf(listOf<DrawPath>()) }
    var currentOffsets by remember { mutableStateOf(listOf<Offset>()) }
    var textItems by remember { mutableStateOf(listOf<TextOverlayItem>()) }
    var inputText by remember { mutableStateOf("") }

    // ── Container & image bounds ──────────────────────────────────────────────
    var containerSizePx by remember { mutableStateOf(Size.Zero) }
    val imageDisplayBounds = remember(containerSizePx, currentBitmap.width, currentBitmap.height) {
        if (containerSizePx == Size.Zero) return@remember Rect(Offset.Zero, Size.Zero)
        val scale = minOf(
            containerSizePx.width / currentBitmap.width,
            containerSizePx.height / currentBitmap.height
        )
        val w = currentBitmap.width * scale
        val h = currentBitmap.height * scale
        Rect(
            Offset((containerSizePx.width - w) / 2f, (containerSizePx.height - h) / 2f),
            Size(w, h)
        )
    }

    // ── Crop state ────────────────────────────────────────────────────────────
    var cropLeft by remember { mutableStateOf(0f) }
    var cropTop by remember { mutableStateOf(0f) }
    var cropRight by remember { mutableStateOf(0f) }
    var cropBottom by remember { mutableStateOf(0f) }
    var isCropInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(imageDisplayBounds) {
        if (!isCropInitialized && imageDisplayBounds.width > 0f) {
            cropLeft = imageDisplayBounds.left
            cropTop = imageDisplayBounds.top
            cropRight = imageDisplayBounds.right
            cropBottom = imageDisplayBounds.bottom
            isCropInitialized = true
        }
    }

    // ── Misc ──────────────────────────────────────────────────────────────────
    var isSending by remember { mutableStateOf(false) }

    val presetColors = listOf(
        Color(0xFFFF3B30), Color(0xFFFF9500), Color(0xFFFFD60A),
        Color(0xFF34C759), Color(0xFF00C7BE), Color(0xFF007AFF),
        Color(0xFF5856D6), Color(0xFFFF2D55), Color(0xFFFFFFFF),
        Color(0xFF000000)
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────────────
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
                // Mode tabs
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    EditorModeTab("Draw", mode == EditorMode.DRAW) { mode = EditorMode.DRAW }
                    EditorModeTab("Text", mode == EditorMode.TEXT) { mode = EditorMode.TEXT }
                    EditorModeTab("Crop", mode == EditorMode.CROP) {
                        mode = EditorMode.CROP
                        if (!isCropInitialized && imageDisplayBounds.width > 0f) {
                            cropLeft = imageDisplayBounds.left
                            cropTop = imageDisplayBounds.top
                            cropRight = imageDisplayBounds.right
                            cropBottom = imageDisplayBounds.bottom
                            isCropInitialized = true
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                // Undo
                IconButton(onClick = {
                    when (mode) {
                        EditorMode.DRAW -> if (completedPaths.isNotEmpty()) completedPaths = completedPaths.dropLast(1)
                        EditorMode.TEXT -> if (textItems.isNotEmpty()) textItems = textItems.dropLast(1)
                        EditorMode.CROP -> {
                            cropLeft = imageDisplayBounds.left
                            cropTop = imageDisplayBounds.top
                            cropRight = imageDisplayBounds.right
                            cropBottom = imageDisplayBounds.bottom
                        }
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
                                    originalBitmap = currentBitmap,
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
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Check, "Send", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
            }

            // ── Image + overlays ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .onSizeChanged { containerSizePx = Size(it.width.toFloat(), it.height.toFloat()) }
            ) {
                Image(
                    painter = rememberAsyncImagePainter(currentBitmap),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                // Draw / Text canvas
                if (mode != EditorMode.CROP) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(mode, selectedColor, strokeWidthDp) {
                                if (mode == EditorMode.DRAW) {
                                    var head = Offset.Zero
                                    detectDragGestures(
                                        onDragStart = { o -> head = o; currentOffsets = listOf(o) },
                                        onDrag = { _, d -> head += d; currentOffsets = currentOffsets + head },
                                        onDragEnd = {
                                            if (currentOffsets.isNotEmpty()) {
                                                completedPaths = completedPaths + DrawPath(
                                                    currentOffsets, selectedColor,
                                                    strokeWidthDp * density.density
                                                )
                                            }
                                            currentOffsets = emptyList()
                                        },
                                        onDragCancel = { currentOffsets = emptyList() }
                                    )
                                } else {
                                    detectTapGestures { offset ->
                                        if (inputText.isNotBlank()) {
                                            textItems = textItems + TextOverlayItem(
                                                text = inputText, color = selectedColor,
                                                x = offset.x, y = offset.y,
                                                fontSizePx = with(density) { 20.sp.toPx() }
                                            )
                                            inputText = ""
                                        }
                                    }
                                }
                            }
                    ) {
                        completedPaths.forEach { p -> drawStroke(p.offsets, p.color, p.strokeWidthPx) }
                        if (currentOffsets.isNotEmpty()) {
                            drawStroke(currentOffsets, selectedColor, strokeWidthDp * density.density)
                        }
                    }
                    // Text overlays
                    textItems.forEach { item ->
                        Text(
                            text = item.text,
                            color = item.color,
                            fontSize = with(LocalDensity.current) { item.fontSizePx.toSp() },
                            fontWeight = FontWeight.Bold,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.7f),
                                    offset = Offset(1f, 1f),
                                    blurRadius = 3f
                                )
                            ),
                            modifier = Modifier.absoluteOffset {
                                IntOffset(item.x.toInt(), item.y.toInt())
                            }
                        )
                    }
                }

                // Crop overlay + handles
                if (mode == EditorMode.CROP && isCropInitialized && imageDisplayBounds.width > 0f) {
                    CropOverlay(
                        imageDisplayBounds = imageDisplayBounds,
                        cropLeft = cropLeft, cropTop = cropTop,
                        cropRight = cropRight, cropBottom = cropBottom
                    )

                    val minCropPx = with(density) { 40.dp.toPx() }
                    val imgL = imageDisplayBounds.left
                    val imgT = imageDisplayBounds.top
                    val imgR = imageDisplayBounds.right
                    val imgB = imageDisplayBounds.bottom

                    // Corner handles: TL, TR, BL, BR
                    CropCornerHandle(x = cropLeft, y = cropTop, cornerType = CornerType.TL) { dx, dy ->
                        cropLeft = (cropLeft + dx).coerceIn(imgL, cropRight - minCropPx)
                        cropTop = (cropTop + dy).coerceIn(imgT, cropBottom - minCropPx)
                    }
                    CropCornerHandle(x = cropRight, y = cropTop, cornerType = CornerType.TR) { dx, dy ->
                        cropRight = (cropRight + dx).coerceIn(cropLeft + minCropPx, imgR)
                        cropTop = (cropTop + dy).coerceIn(imgT, cropBottom - minCropPx)
                    }
                    CropCornerHandle(x = cropLeft, y = cropBottom, cornerType = CornerType.BL) { dx, dy ->
                        cropLeft = (cropLeft + dx).coerceIn(imgL, cropRight - minCropPx)
                        cropBottom = (cropBottom + dy).coerceIn(cropTop + minCropPx, imgB)
                    }
                    CropCornerHandle(x = cropRight, y = cropBottom, cornerType = CornerType.BR) { dx, dy ->
                        cropRight = (cropRight + dx).coerceIn(cropLeft + minCropPx, imgR)
                        cropBottom = (cropBottom + dy).coerceIn(cropTop + minCropPx, imgB)
                    }

                    // Edge handles: T, B, L, R
                    CropEdgeHandle(
                        x = (cropLeft + cropRight) / 2f, y = cropTop, isHorizontal = true
                    ) { _, dy -> cropTop = (cropTop + dy).coerceIn(imgT, cropBottom - minCropPx) }
                    CropEdgeHandle(
                        x = (cropLeft + cropRight) / 2f, y = cropBottom, isHorizontal = true
                    ) { _, dy -> cropBottom = (cropBottom + dy).coerceIn(cropTop + minCropPx, imgB) }
                    CropEdgeHandle(
                        x = cropLeft, y = (cropTop + cropBottom) / 2f, isHorizontal = false
                    ) { dx, _ -> cropLeft = (cropLeft + dx).coerceIn(imgL, cropRight - minCropPx) }
                    CropEdgeHandle(
                        x = cropRight, y = (cropTop + cropBottom) / 2f, isHorizontal = false
                    ) { dx, _ -> cropRight = (cropRight + dx).coerceIn(cropLeft + minCropPx, imgR) }
                }
            }

            // ── Bottom toolbar ─────────────────────────────────────────────────
            when (mode) {
                EditorMode.DRAW -> DrawToolbar(
                    selectedColor = selectedColor,
                    strokeWidthDp = strokeWidthDp,
                    presetColors = presetColors,
                    onColorChange = { selectedColor = it },
                    onStrokeChange = { strokeWidthDp = it }
                )
                EditorMode.TEXT -> TextToolbar(
                    inputText = inputText,
                    selectedColor = selectedColor,
                    presetColors = presetColors,
                    onTextChange = { inputText = it },
                    onColorChange = { selectedColor = it }
                )
                EditorMode.CROP -> CropToolbar(
                    onReset = {
                        cropLeft = imageDisplayBounds.left; cropTop = imageDisplayBounds.top
                        cropRight = imageDisplayBounds.right; cropBottom = imageDisplayBounds.bottom
                    },
                    onApply = {
                        scope.launch(Dispatchers.IO) {
                            val scaleX = currentBitmap.width / imageDisplayBounds.width
                            val scaleY = currentBitmap.height / imageDisplayBounds.height
                            val l = ((cropLeft - imageDisplayBounds.left) * scaleX).toInt().coerceAtLeast(0)
                            val t = ((cropTop - imageDisplayBounds.top) * scaleY).toInt().coerceAtLeast(0)
                            val w = ((cropRight - cropLeft) * scaleX).toInt()
                                .coerceAtMost(currentBitmap.width - l)
                            val h = ((cropBottom - cropTop) * scaleY).toInt()
                                .coerceAtMost(currentBitmap.height - t)
                            if (w > 0 && h > 0) {
                                val cropped = android.graphics.Bitmap.createBitmap(currentBitmap, l, t, w, h)
                                currentBitmap = cropped
                                completedPaths = emptyList()
                                textItems = emptyList()
                                isCropInitialized = false
                            }
                        }
                    }
                )
            }
        }
    }
}

// ─── Crop overlay (dark mask + grid lines) ───────────────────────────────────

@Composable
private fun CropOverlay(
    imageDisplayBounds: Rect,
    cropLeft: Float, cropTop: Float, cropRight: Float, cropBottom: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val overlay = Color.Black.copy(alpha = 0.55f)
        val iL = imageDisplayBounds.left; val iT = imageDisplayBounds.top
        val cL = cropLeft - iL; val cT = cropTop - iT
        val cR = cropRight - iL; val cB = cropBottom - iT
        val cW = cR - cL; val cH = cB - cT

        // 4 dark rects around crop area
        drawRect(overlay, Offset.Zero, Size(size.width, cT + iT))
        drawRect(overlay, Offset(0f, cB + iT), Size(size.width, size.height - cB - iT))
        drawRect(overlay, Offset(0f, cT + iT), Size(cL + iL, cH))
        drawRect(overlay, Offset(cR + iL, cT + iT), Size(size.width - cR - iL, cH))

        // Crop border
        drawRect(
            color = Color.White,
            topLeft = Offset(cL + iL, cT + iT),
            size = Size(cW, cH),
            style = Stroke(width = 2.dp.toPx())
        )

        // Rule-of-thirds grid lines
        val gridColor = Color.White.copy(alpha = 0.4f)
        val gridStroke = Stroke(width = 0.8.dp.toPx())
        val third1X = cL + iL + cW / 3f
        val third2X = cL + iL + cW * 2f / 3f
        val third1Y = cT + iT + cH / 3f
        val third2Y = cT + iT + cH * 2f / 3f

        drawLine(gridColor, Offset(third1X, cT + iT), Offset(third1X, cB + iT), gridStroke.width)
        drawLine(gridColor, Offset(third2X, cT + iT), Offset(third2X, cB + iT), gridStroke.width)
        drawLine(gridColor, Offset(cL + iL, third1Y), Offset(cR + iL, third1Y), gridStroke.width)
        drawLine(gridColor, Offset(cL + iL, third2Y), Offset(cR + iL, third2Y), gridStroke.width)
    }
}

// ─── Crop handles ─────────────────────────────────────────────────────────────

enum class CornerType { TL, TR, BL, BR }

@Composable
private fun CropCornerHandle(
    x: Float, y: Float,
    cornerType: CornerType,
    onDrag: (dx: Float, dy: Float) -> Unit
) {
    val density = LocalDensity.current
    val armLen = with(density) { 20.dp.toPx() }
    val thickness = with(density) { 3.dp.toPx() }
    val hitSize = 36.dp
    val halfHit = with(density) { hitSize.toPx() / 2f }

    Box(
        modifier = Modifier
            .absoluteOffset { IntOffset((x - halfHit).toInt(), (y - halfHit).toInt()) }
            .size(hitSize)
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount -> onDrag(dragAmount.x, dragAmount.y) }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val paint = Color.White
            val sx = if (cornerType == CornerType.TL || cornerType == CornerType.BL) -1 else 1
            val sy = if (cornerType == CornerType.TL || cornerType == CornerType.TR) -1 else 1
            // Horizontal arm
            drawLine(paint, Offset(cx, cy), Offset(cx + sx * armLen, cy), thickness)
            // Vertical arm
            drawLine(paint, Offset(cx, cy), Offset(cx, cy + sy * armLen), thickness)
        }
    }
}

@Composable
private fun CropEdgeHandle(
    x: Float, y: Float,
    isHorizontal: Boolean,
    onDrag: (dx: Float, dy: Float) -> Unit
) {
    val density = LocalDensity.current
    val w = if (isHorizontal) 32.dp else 12.dp
    val h = if (isHorizontal) 12.dp else 32.dp
    val halfW = with(density) { w.toPx() / 2f }
    val halfH = with(density) { h.toPx() / 2f }

    Box(
        modifier = Modifier
            .absoluteOffset { IntOffset((x - halfW).toInt(), (y - halfH).toInt()) }
            .size(w, h)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.85f))
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount -> onDrag(dragAmount.x, dragAmount.y) }
            }
    )
}

// ─── Draw toolbar ─────────────────────────────────────────────────────────────

@Composable
private fun DrawToolbar(
    selectedColor: Color,
    strokeWidthDp: Float,
    presetColors: List<Color>,
    onColorChange: (Color) -> Unit,
    onStrokeChange: (Float) -> Unit
) {
    var hueSliderWidthPx by remember { mutableStateOf(1f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1C1C1E))
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Preset color swatches + current color preview
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presetColors.forEach { color ->
                val isSelected = color == selectedColor
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 32.dp else 24.dp)
                        .clip(CircleShape)
                        .background(color)
                        .then(
                            if (isSelected) Modifier.border(2.dp, Color.White, CircleShape)
                            else if (color.luminance() < 0.05f) Modifier.border(1.dp, Color.White.copy(0.3f), CircleShape)
                            else Modifier
                        )
                        .clickable { onColorChange(color) }
                )
            }
            Spacer(Modifier.weight(1f))
            // Current color preview
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(selectedColor)
                    .border(2.dp, Color.White, CircleShape)
            )
        }

        // Hue gradient slider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .clip(RoundedCornerShape(13.dp))
                .onSizeChanged { hueSliderWidthPx = it.width.toFloat() }
                .pointerInput(hueSliderWidthPx) {
                    detectDragGestures(
                        onDragStart = { o ->
                            val hue = (o.x / hueSliderWidthPx * 360f).coerceIn(0f, 360f)
                            onColorChange(Color.hsv(hue, 1f, 1f))
                        },
                        onDrag = { change, _ ->
                            val hue = (change.position.x / hueSliderWidthPx * 360f).coerceIn(0f, 360f)
                            onColorChange(Color.hsv(hue, 1f, 1f))
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = (0..12).map { Color.hsv(it * 30f, 1f, 1f) }
                    )
                )
                // Indicator line at current hue (approximate)
                val hue = try {
                    val hsv = FloatArray(3)
                    android.graphics.Color.colorToHSV(selectedColor.toArgb(), hsv)
                    hsv[0]
                } catch (_: Exception) { 0f }
                val indicatorX = (hue / 360f) * size.width
                drawLine(
                    color = Color.White,
                    start = Offset(indicatorX, 0f),
                    end = Offset(indicatorX, size.height),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }

        // Stroke width slider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Thin dot preview
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.6f))
            )
            Slider(
                value = strokeWidthDp,
                onValueChange = onStrokeChange,
                valueRange = 2f..30f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = selectedColor,
                    activeTrackColor = selectedColor,
                    inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                )
            )
            // Thick dot preview
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.6f))
            )
            // Width label
            Text(
                "${strokeWidthDp.toInt()}dp",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.width(28.dp)
            )
        }

        // Stroke preview line
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height((strokeWidthDp.coerceAtMost(20f)).dp)
                .clip(RoundedCornerShape(50))
        ) {
            drawLine(
                color = selectedColor,
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = size.height,
                cap = StrokeCap.Round
            )
        }
    }
}

// ─── Text toolbar ─────────────────────────────────────────────────────────────

@Composable
private fun TextToolbar(
    inputText: String,
    selectedColor: Color,
    presetColors: List<Color>,
    onTextChange: (String) -> Unit,
    onColorChange: (Color) -> Unit
) {
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
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            presetColors.forEach { color ->
                val isSelected = color == selectedColor
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 30.dp else 22.dp)
                        .clip(CircleShape)
                        .background(color)
                        .then(
                            if (isSelected) Modifier.border(2.dp, Color.White, CircleShape)
                            else if (color.luminance() < 0.05f) Modifier.border(1.dp, Color.White.copy(0.3f), CircleShape)
                            else Modifier
                        )
                        .clickable { onColorChange(color) }
                )
            }
        }
        OutlinedTextField(
            value = inputText,
            onValueChange = onTextChange,
            placeholder = {
                Text("Nhập text rồi chạm lên ảnh…", color = Color.White.copy(0.35f),
                    style = MaterialTheme.typography.bodyMedium)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedContainerColor = Color.White.copy(0.08f), unfocusedContainerColor = Color.White.copy(0.08f),
                focusedBorderColor = selectedColor, unfocusedBorderColor = Color.White.copy(0.2f),
                cursorColor = selectedColor
            )
        )
        Text(
            text = if (inputText.isBlank()) "Nhập text ở trên, chạm vào vị trí bất kỳ trên ảnh"
                   else "Chạm vào ảnh để đặt: \"$inputText\"",
            color = if (inputText.isBlank()) Color.White.copy(0.4f) else selectedColor.copy(0.85f),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (inputText.isBlank()) FontWeight.Normal else FontWeight.Medium
        )
    }
}

// ─── Crop toolbar ─────────────────────────────────────────────────────────────

@Composable
private fun CropToolbar(onReset: () -> Unit, onApply: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1C1C1E))
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.4f))
        ) {
            Text("Reset", fontWeight = FontWeight.Medium)
        }
        Button(
            onClick = onApply,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
        ) {
            Icon(Icons.Filled.Crop, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Apply Crop", fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun DrawScope.drawStroke(offsets: List<Offset>, color: Color, strokeWidthPx: Float) {
    if (offsets.isEmpty()) return
    if (offsets.size == 1) { drawCircle(color = color, radius = strokeWidthPx / 2f, center = offsets[0]); return }
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
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.Black else Color.White.copy(0.65f),
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
        FileOutputStream(file).use { out -> result.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, out) }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
