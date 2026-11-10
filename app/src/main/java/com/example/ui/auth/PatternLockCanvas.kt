package com.example.ui.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurfaceVariant
import kotlin.math.sqrt

@Composable
fun PatternLockCanvas(
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    lineColor: Color = if (isError) Color(0xFFFF4D6D) else NeonEmerald,
    nodeActiveColor: Color = if (isError) Color(0xFFFF4D6D) else CyberCyan,
    onPatternComplete: (String) -> Unit
) {
    val selectedNodes = remember { mutableStateListOf<Int>() }
    var currentTouchPosition by remember { mutableStateOf<Offset?>(null) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp)
            .testTag("pattern_lock_canvas"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            selectedNodes.clear()
                            currentTouchPosition = offset
                            val hit = findHitNode(offset, size.width.toFloat(), size.height.toFloat())
                            if (hit != null && !selectedNodes.contains(hit)) {
                                selectedNodes.add(hit)
                            }
                        },
                        onDragEnd = {
                            if (selectedNodes.isNotEmpty()) {
                                onPatternComplete(selectedNodes.joinToString("-"))
                            }
                            currentTouchPosition = null
                            selectedNodes.clear()
                        },
                        onDragCancel = {
                            currentTouchPosition = null
                            selectedNodes.clear()
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentTouchPosition = change.position
                            val hit = findHitNode(change.position, size.width.toFloat(), size.height.toFloat())
                            if (hit != null && !selectedNodes.contains(hit)) {
                                selectedNodes.add(hit)
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val hit = findHitNode(offset, size.width.toFloat(), size.height.toFloat())
                        if (hit != null && !selectedNodes.contains(hit)) {
                            selectedNodes.add(hit)
                            onPatternComplete(selectedNodes.joinToString("-"))
                            selectedNodes.clear()
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val cellWidth = width / 3f
            val cellHeight = height / 3f

            fun getNodeCenter(index: Int): Offset {
                val row = index / 3
                val col = index % 3
                return Offset(
                    x = col * cellWidth + cellWidth / 2f,
                    y = row * cellHeight + cellHeight / 2f
                )
            }

            // Draw connecting lines between selected nodes
            if (selectedNodes.size > 1) {
                for (i in 0 until selectedNodes.size - 1) {
                    val start = getNodeCenter(selectedNodes[i])
                    val end = getNodeCenter(selectedNodes[i + 1])
                    drawLine(
                        color = lineColor,
                        start = start,
                        end = end,
                        strokeWidth = 14f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // Draw active line from last node to current finger position
            if (selectedNodes.isNotEmpty() && currentTouchPosition != null) {
                val lastCenter = getNodeCenter(selectedNodes.last())
                drawLine(
                    color = lineColor.copy(alpha = 0.7f),
                    start = lastCenter,
                    end = currentTouchPosition!!,
                    strokeWidth = 10f,
                    cap = StrokeCap.Round
                )
            }

            // Draw the 9 nodes
            for (i in 0 until 9) {
                val center = getNodeCenter(i)
                val isSelected = selectedNodes.contains(i)

                // Outer halo ring for selected nodes
                if (isSelected) {
                    drawCircle(
                        color = nodeActiveColor.copy(alpha = 0.25f),
                        radius = cellWidth * 0.35f,
                        center = center
                    )
                    drawCircle(
                        color = nodeActiveColor,
                        radius = cellWidth * 0.28f,
                        center = center,
                        style = Stroke(width = 4f)
                    )
                } else {
                    drawCircle(
                        color = ObsidianSurfaceVariant,
                        radius = cellWidth * 0.22f,
                        center = center
                    )
                    drawCircle(
                        color = ObsidianBorder,
                        radius = cellWidth * 0.22f,
                        center = center,
                        style = Stroke(width = 2f)
                    )
                }

                // Inner core dot
                drawCircle(
                    color = if (isSelected) nodeActiveColor else Color(0xFF6F829D),
                    radius = if (isSelected) 18f else 10f,
                    center = center
                )
            }
        }
    }
}

private fun findHitNode(touch: Offset, width: Float, height: Float): Int? {
    val cellWidth = width / 3f
    val cellHeight = height / 3f
    val hitRadius = cellWidth * 0.4f

    for (i in 0 until 9) {
        val row = i / 3
        val col = i % 3
        val nodeCenter = Offset(
            x = col * cellWidth + cellWidth / 2f,
            y = row * cellHeight + cellHeight / 2f
        )
        val dx = touch.x - nodeCenter.x
        val dy = touch.y - nodeCenter.y
        if (sqrt(dx * dx + dy * dy) <= hitRadius) {
            return i
        }
    }
    return null
}
