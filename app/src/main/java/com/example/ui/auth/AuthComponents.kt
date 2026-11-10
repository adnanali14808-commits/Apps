package com.example.ui.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun PinDotIndicator(
    pinLength: Int,
    maxDigits: Int = 8,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    val totalSlots = when {
        pinLength <= 4 -> 4
        pinLength <= 6 -> 6
        else -> 8
    }

    Row(
        modifier = modifier.padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalSlots) {
            val isFilled = i < pinLength
            val dotColor = when {
                isError -> Color(0xFFFF4D6D)
                isFilled -> NeonEmerald
                else -> ObsidianSurfaceVariant
            }

            Box(
                modifier = Modifier
                    .size(if (isFilled) 15.dp else 11.dp)
                    .clip(CircleShape)
                    .background(dotColor)
                    .border(
                        width = 1.5.dp,
                        color = if (isFilled) NeonEmerald else ObsidianBorder,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
fun SecurityKeypad(
    modifier: Modifier = Modifier,
    showConfirmButton: Boolean = false,
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onConfirmClick: () -> Unit = {}
) {
    val keypadRows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(if (showConfirmButton) "OK" else "", "0", "DEL")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keypadRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    when (key) {
                        "DEL" -> {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(ObsidianSurface)
                                    .border(1.dp, ObsidianBorder, CircleShape)
                                    .clickable { onBackspaceClick() }
                                    .testTag("keypad_del"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Backspace",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        "OK" -> {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(NeonEmerald)
                                    .clickable { onConfirmClick() }
                                    .testTag("keypad_ok"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Confirm",
                                    tint = Color.Black,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        "" -> {
                            Box(modifier = Modifier.size(72.dp))
                        }
                        else -> {
                            KeypadButton(
                                label = key,
                                onClick = { onDigitClick(key) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(ObsidianSurface)
            .border(1.5.dp, ObsidianBorder, CircleShape)
            .clickable { onClick() }
            .testTag("keypad_btn_$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
fun rememberShakeOffset(trigger: Int): Float {
    val anim = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger > 0) {
            anim.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    (-20f) at 50
                    20f at 100
                    (-15f) at 150
                    15f at 200
                    (-10f) at 250
                    10f at 300
                    (-4f) at 350
                    0f at 400
                }
            )
        }
    }

    return anim.value
}
