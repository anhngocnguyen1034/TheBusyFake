package com.example.thebusysimulator.presentation.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun ChatPatternBackground(pattern: ChatPattern, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        when (pattern) {
            ChatPattern.DOTS -> drawDots(color)
            ChatPattern.WAVES -> drawWaves(color)
            ChatPattern.CROSSES -> drawCrosses(color)
            ChatPattern.HEXAGONS -> drawHexagons(color)
            ChatPattern.DIAGONAL -> drawDiagonal(color)
            ChatPattern.CIRCLES -> drawCircles(color)
            ChatPattern.DIAMONDS -> drawDiamonds(color)
            ChatPattern.STARS -> drawStars(color)
        }
    }
}

private fun DrawScope.drawDots(color: Color) {
    val spacing = 28f
    val radius = 2.2f
    var y = spacing / 2
    var rowIndex = 0
    while (y < size.height + spacing) {
        val offsetX = if (rowIndex % 2 == 1) spacing / 2 else 0f
        var x = offsetX
        while (x < size.width + spacing) {
            drawCircle(color = color, radius = radius, center = Offset(x, y))
            x += spacing
        }
        y += spacing * 0.866f
        rowIndex++
    }
}

private fun DrawScope.drawWaves(color: Color) {
    val waveHeight = 12f
    val waveLength = 80f
    val spacing = 32f
    val stroke = Stroke(width = 1.5f, cap = StrokeCap.Round)
    var y = spacing
    while (y < size.height + spacing) {
        val path = Path()
        path.moveTo(0f, y)
        var x = 0f
        while (x <= size.width) {
            val angle = (x / waveLength) * 2 * PI.toFloat()
            val waveY = y + sin(angle) * waveHeight
            path.lineTo(x, waveY)
            x += 2f
        }
        drawPath(path = path, color = color, style = stroke)
        y += spacing
    }
}

private fun DrawScope.drawCrosses(color: Color) {
    val spacing = 36f
    val armLen = 7f
    val stroke = Stroke(width = 1.5f, cap = StrokeCap.Round)
    var y = spacing / 2
    while (y < size.height + spacing) {
        var x = spacing / 2
        while (x < size.width + spacing) {
            val path = Path()
            path.moveTo(x - armLen, y)
            path.lineTo(x + armLen, y)
            path.moveTo(x, y - armLen)
            path.lineTo(x, y + armLen)
            drawPath(path = path, color = color, style = stroke)
            x += spacing
        }
        y += spacing
    }
}

private fun DrawScope.drawHexagons(color: Color) {
    val r = 18f
    val w = r * sqrt(3f)
    val h = r * 2f
    val stroke = Stroke(width = 1.2f)
    var row = 0
    var cy = r
    while (cy < size.height + h) {
        var cx = if (row % 2 == 1) w else w / 2
        while (cx < size.width + w) {
            val path = Path()
            for (i in 0..5) {
                val angle = (PI / 3 * i - PI / 6).toFloat()
                val px = cx + r * cos(angle)
                val py = cy + r * sin(angle)
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            drawPath(path = path, color = color, style = stroke)
            cx += w
        }
        cy += h * 0.75f
        row++
    }
}

private fun DrawScope.drawDiagonal(color: Color) {
    val spacing = 24f
    val stroke = Stroke(width = 1.2f, cap = StrokeCap.Round)
    val totalDiag = size.width + size.height
    var offset = -size.height
    while (offset < size.width + spacing) {
        val path = Path()
        path.moveTo(offset, 0f)
        path.lineTo(offset + totalDiag, totalDiag)
        drawPath(path = path, color = color, style = stroke)
        offset += spacing
    }
}

private fun DrawScope.drawCircles(color: Color) {
    val gridSpacing = 64f
    val radii = listOf(10f, 18f, 26f)
    val stroke = Stroke(width = 1f)
    var y = gridSpacing / 2
    while (y < size.height + gridSpacing) {
        var x = gridSpacing / 2
        while (x < size.width + gridSpacing) {
            for (r in radii) {
                drawCircle(color = color, radius = r, center = Offset(x, y), style = stroke)
            }
            x += gridSpacing
        }
        y += gridSpacing
    }
}

private fun DrawScope.drawDiamonds(color: Color) {
    val spacing = 36f
    val halfSize = 9f
    val stroke = Stroke(width = 1.3f)
    var y = spacing / 2
    var rowIndex = 0
    while (y < size.height + spacing) {
        val offsetX = if (rowIndex % 2 == 1) spacing / 2 else 0f
        var x = offsetX
        while (x < size.width + spacing) {
            val path = Path()
            path.moveTo(x, y - halfSize)
            path.lineTo(x + halfSize, y)
            path.lineTo(x, y + halfSize)
            path.lineTo(x - halfSize, y)
            path.close()
            drawPath(path = path, color = color, style = stroke)
            x += spacing
        }
        y += spacing
        rowIndex++
    }
}

private fun DrawScope.drawStars(color: Color) {
    val spacing = 44f
    val outerR = 5f
    val innerR = 2.2f
    val points = 4
    var y = spacing / 2
    var rowIndex = 0
    while (y < size.height + spacing) {
        val offsetX = if (rowIndex % 2 == 1) spacing / 2 else 0f
        var x = offsetX
        while (x < size.width + spacing) {
            val path = Path()
            for (i in 0 until points * 2) {
                val angle = (PI / points * i - PI / 2).toFloat()
                val r = if (i % 2 == 0) outerR else innerR
                val px = x + r * cos(angle)
                val py = y + r * sin(angle)
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            drawPath(path = path, color = color.copy(alpha = color.alpha * 1.5f))
            x += spacing
        }
        y += spacing
        rowIndex++
    }
}
