package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuthMethodType
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AegisUiState
import kotlin.math.roundToInt

@Composable
fun DirectLockScreen(
    state: AegisUiState,
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onPatternDrawn: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onUnlockSubmit: () -> Unit,
    onSwitchAuthMethod: (AuthMethodType) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val shakeOffset = rememberShakeOffset(state.shakeErrorTrigger)
    val isLockedOut = state.lockoutSeconds > 0
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .offset { IntOffset(shakeOffset.roundToInt(), 0) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Security Brand Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(ObsidianSurface)
                    .border(2.dp, if (isLockedOut) CrimsonAlert else NeonEmerald, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Vault Locked",
                    tint = if (isLockedOut) CrimsonAlert else NeonEmerald,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (state.interceptedAppName != null) "PROTECTED APPLICATION INTERCEPTED" else "AEGIS PRIVATE VAULT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = if (state.interceptedAppName != null) ElectricAmber else CyberCyan
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when {
                    isLockedOut -> "Vault Temporarily Locked"
                    state.interceptedAppName != null -> "${state.interceptedAppName} is Protected"
                    else -> "Enter ${state.authMethod.label}"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            if (isLockedOut) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Too many failed attempts. Try again in ${state.lockoutSeconds}s",
                    fontSize = 13.sp,
                    color = CrimsonAlert,
                    fontWeight = FontWeight.Medium
                )
            } else if (state.interceptedAppName != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Unlock with master credential or press Back to cancel",
                    fontSize = 13.sp,
                    color = ElectricAmber,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            } else if (state.lockError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.lockError,
                    fontSize = 13.sp,
                    color = CrimsonAlert,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Encrypted Local Sandbox Shield Active",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            // Multi-Method Switcher (Visible if user configured more than 1 method)
            if (state.configuredAuthMethods.size > 1 && !isLockedOut) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianSurface)
                        .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    state.configuredAuthMethods.forEach { method ->
                        val isSelected = method == state.authMethod
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CyberCyan.copy(alpha = 0.2f) else Color.Transparent)
                                .border(1.dp, if (isSelected) CyberCyan else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { onSwitchAuthMethod(method) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("switch_method_${method.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (method) {
                                    AuthMethodType.PIN -> "PIN"
                                    AuthMethodType.PATTERN -> "Pattern"
                                    AuthMethodType.PASSWORD -> "Password"
                                },
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) CyberCyan else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Method-Specific Interactive Unlock View
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            contentAlignment = Alignment.Center
        ) {
            when (state.authMethod) {
                AuthMethodType.PIN -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PinDotIndicator(
                            pinLength = state.lockInput.length,
                            maxDigits = 8,
                            isError = state.lockError != null || isLockedOut
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SecurityKeypad(
                            showConfirmButton = state.lockInput.length >= 4,
                            onDigitClick = onDigitClick,
                            onBackspaceClick = onBackspaceClick,
                            onConfirmClick = onUnlockSubmit
                        )
                    }
                }

                AuthMethodType.PATTERN -> {
                    PatternLockCanvas(
                        isError = state.lockError != null || isLockedOut,
                        onPatternComplete = onPatternDrawn
                    )
                }

                AuthMethodType.PASSWORD -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = state.lockInput,
                            onValueChange = onPasswordChanged,
                            label = { Text("Master Password") },
                            singleLine = true,
                            enabled = !isLockedOut,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { onUnlockSubmit() }
                            ),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle visibility",
                                        tint = TextSecondary
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonEmerald,
                                unfocusedBorderColor = ObsidianBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = ObsidianSurface,
                                unfocusedContainerColor = ObsidianSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("lock_password_input")
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onUnlockSubmit,
                            enabled = state.lockInput.isNotEmpty() && !isLockedOut,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonEmerald,
                                contentColor = ObsidianBlack
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("unlock_vault_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Unlock Vault",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Bottom Footer
        Row(
            modifier = Modifier.padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Zero Cloud Telemetry • 100% On-Device Protection",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}
