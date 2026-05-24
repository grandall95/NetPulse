package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ColorExcellent
import com.example.ui.theme.ColorFailed
import com.example.ui.theme.ColorGood
import com.example.ui.theme.ColorPoor

@Composable
fun ResultCard(
    modifier: Modifier = Modifier,
    title: String,
    statusText: String? = null,
    statusColor: Color? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
    headerContent: @Composable (RowScope: ColumnScope) -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = if (containerColor == MaterialTheme.colorScheme.surface) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (containerColor == MaterialTheme.colorScheme.surface) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                if (statusText != null) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = (statusColor ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor ?: MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = (if (containerColor == MaterialTheme.colorScheme.surface) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary).copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

private fun BorderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = 
    androidx.compose.foundation.BorderStroke(width, color)

@Composable
fun LatencyChart(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier,
    enableZoom: Boolean = false
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(0f) }

    val excellentThreshold = 50f
    val moderateThreshold = 150f

    val minLat = if (dataPoints.isEmpty()) 0f else dataPoints.minOrNull() ?: 0f
    val maxLat = if (dataPoints.isEmpty()) 100f else dataPoints.maxOrNull() ?: 100f
    val avgLat = if (dataPoints.isEmpty()) 0f else dataPoints.average().toFloat()

    val maxVal = (maxLat * 1.15f).coerceAtLeast(10f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Min: ${String.format("%.1f", minLat)}ms",
                style = MaterialTheme.typography.bodySmall,
                color = ColorExcellent,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Avg: ${String.format("%.1f", avgLat)}ms",
                style = MaterialTheme.typography.bodySmall,
                color = ColorGood,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Max: ${String.format("%.1f", maxLat)}ms",
                style = MaterialTheme.typography.bodySmall,
                color = ColorFailed,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .pointerInput(enableZoom) {
                    if (enableZoom) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            offset = (offset + pan.x).coerceIn(
                                -size.width.toFloat() * (scale - 1),
                                0f
                            )
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                if (dataPoints.size < 2) {
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(0f, height / 2),
                        end = Offset(width, height / 2),
                        strokeWidth = 2f
                    )
                    return@Canvas
                }

                val pointsCount = dataPoints.size
                val stepX = width / (pointsCount - 1)

                val mainPath = Path()
                val fillPath = Path()

                dataPoints.forEachIndexed { index, value ->
                    val rawX = index * stepX
                    // Apply zoom and panning offset
                    val x = (rawX * scale) + offset
                    val y = height - ((value / maxVal) * height)

                    if (index == 0) {
                        mainPath.moveTo(x, y)
                        fillPath.moveTo(x, height)
                        fillPath.lineTo(x, y)
                    } else {
                        mainPath.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }

                    if (index == dataPoints.lastIndex) {
                        fillPath.lineTo(x, height)
                        fillPath.close()
                    }
                }

                // Threshold Guideline Drawing
                val excY = height - ((excellentThreshold / maxVal) * height)
                val modY = height - ((moderateThreshold / maxVal) * height)

                if (excY in 0f..height) {
                    drawLine(
                        color = ColorExcellent.copy(alpha = 0.25f),
                        start = Offset(0f, excY),
                        end = Offset(width, excY),
                        strokeWidth = 2f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }
                if (modY in 0f..height) {
                    drawLine(
                        color = ColorPoor.copy(alpha = 0.25f),
                        start = Offset(0f, modY),
                        end = Offset(width, modY),
                        strokeWidth = 2f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }

                // Draw Gradient Area under Line
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ColorExcellent.copy(alpha = 0.2f),
                            ColorExcellent.copy(alpha = 0.0f)
                        )
                    )
                )

                // Select stroke color based on average
                val strokeColor = when {
                    avgLat < excellentThreshold -> ColorExcellent
                    avgLat < moderateThreshold -> ColorGood
                    else -> ColorFailed
                }

                drawPath(
                    path = mainPath,
                    color = strokeColor,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun PermissionRationaleDialog(
    permissionLabel: String,
    rationaleText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "$permissionLabel Permission Needed") },
        text = { Text(text = rationaleText) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Grant")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
