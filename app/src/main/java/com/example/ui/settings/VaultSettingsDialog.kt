package com.example.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuthMethodType
import com.example.data.model.SecurityConfig
import com.example.ui.auth.PatternLockCanvas
import com.example.ui.auth.PinDotIndicator
import com.example.ui.auth.SecurityKeypad
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultSettingsDialog(
    securityConfig: SecurityConfig,
    onSaveCredential: (AuthMethodType, String) -> Unit,
    onRemoveCredential: (AuthMethodType) -> Unit,
    onResetAllSecurity: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var configuringMethod by remember { mutableStateOf<AuthMethodType?>(null) }
    var showResetAllConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianBlack,
        dragHandle = null,
        modifier = Modifier.testTag("vault_settings_dialog")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyberCyan.copy(alpha = 0.15f))
                            .border(1.dp, CyberCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Vault Security Settings",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Multi-Method Authentication & Reset",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ObsidianSurface)
                        .testTag("close_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Information Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianSurfaceVariant)
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "You can activate 1, 2, or all 3 unlock methods. If you ever forget one method, you can switch to another directly on the lock screen.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "AUTHENTICATION METHODS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = CyberCyan
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Method 1: PIN
            MethodConfigRow(
                method = AuthMethodType.PIN,
                icon = Icons.Default.Dialpad,
                isConfigured = securityConfig.isPinConfigured,
                canRemove = securityConfig.configuredMethods.size > 1 && securityConfig.isPinConfigured,
                onConfigure = { configuringMethod = AuthMethodType.PIN },
                onRemove = { onRemoveCredential(AuthMethodType.PIN) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Method 2: Pattern Lock
            MethodConfigRow(
                method = AuthMethodType.PATTERN,
                icon = Icons.Default.Gesture,
                isConfigured = securityConfig.isPatternConfigured,
                canRemove = securityConfig.configuredMethods.size > 1 && securityConfig.isPatternConfigured,
                onConfigure = { configuringMethod = AuthMethodType.PATTERN },
                onRemove = { onRemoveCredential(AuthMethodType.PATTERN) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Method 3: Password
            MethodConfigRow(
                method = AuthMethodType.PASSWORD,
                icon = Icons.Default.Password,
                isConfigured = securityConfig.isPasswordConfigured,
                canRemove = securityConfig.configuredMethods.size > 1 && securityConfig.isPasswordConfigured,
                onConfigure = { configuringMethod = AuthMethodType.PASSWORD },
                onRemove = { onRemoveCredential(AuthMethodType.PASSWORD) }
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "DANGER ZONE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = CrimsonAlert
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, CrimsonAlert.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockReset,
                            contentDescription = null,
                            tint = CrimsonAlert,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Reset Master Security",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "Erases all current PIN, Pattern, and Password credentials so you can start the setup wizard completely fresh.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    Button(
                        onClick = { showResetAllConfirm = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrimsonAlert.copy(alpha = 0.2f),
                            contentColor = CrimsonAlert
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_reset_all_security")
                    ) {
                        Text("Reset & Set Up New Credentials", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // Sub-dialog: Configure specific method
        if (configuringMethod != null) {
            CredentialConfigureModal(
                method = configuringMethod!!,
                onSave = { credential ->
                    onSaveCredential(configuringMethod!!, credential)
                    configuringMethod = null
                },
                onDismiss = { configuringMethod = null }
            )
        }

        // Reset Confirmation Dialog
        if (showResetAllConfirm) {
            AlertDialog(
                onDismissRequest = { showResetAllConfirm = false },
                containerColor = ObsidianSurface,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.border(1.dp, ObsidianBorder, RoundedCornerShape(18.dp)),
                title = {
                    Text(
                        text = "Reset All Security Credentials?",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                },
                text = {
                    Text(
                        text = "Are you sure? This will clear all configured credentials and prompt you to establish new ones.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showResetAllConfirm = false
                            onDismiss()
                            onResetAllSecurity()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrimsonAlert,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Yes, Reset Everything", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetAllConfirm = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun MethodConfigRow(
    method: AuthMethodType,
    icon: ImageVector,
    isConfigured: Boolean,
    canRemove: Boolean,
    onConfigure: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, if (isConfigured) NeonEmerald.copy(alpha = 0.4f) else ObsidianBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isConfigured) NeonEmerald.copy(alpha = 0.15f) else ObsidianSurfaceVariant)
                    .border(1.dp, if (isConfigured) NeonEmerald else ObsidianBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isConfigured) NeonEmerald else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = method.label,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isConfigured) NeonEmerald.copy(alpha = 0.15f) else ObsidianSurfaceVariant
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isConfigured) "ACTIVE" else "NOT SET",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isConfigured) NeonEmerald else TextTertiary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = method.description,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (canRemove) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Remove method",
                            tint = CrimsonAlert,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Button(
                    onClick = onConfigure,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConfigured) ObsidianSurfaceVariant else NeonEmerald,
                        contentColor = if (isConfigured) CyberCyan else ObsidianBlack
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = if (isConfigured) "Change" else "Set Up",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CredentialConfigureModal(
    method: AuthMethodType,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: input, 2: confirm
    var firstInput by remember { mutableStateOf("") }
    var secondInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .border(1.dp, ObsidianBorder, RoundedCornerShape(20.dp))
            .testTag("credential_configure_modal"),
        title = {
            Column {
                Text(
                    text = if (step == 1) "Set ${method.label}" else "Confirm ${method.label}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = if (step == 1) {
                        when (method) {
                            AuthMethodType.PIN -> "Enter 4 to 8 digits for your PIN"
                            AuthMethodType.PATTERN -> "Draw your 3x3 pattern"
                            AuthMethodType.PASSWORD -> "Enter at least 6 characters"
                        }
                    } else "Re-enter to verify",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        fontSize = 12.sp,
                        color = CrimsonAlert,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                when (method) {
                    AuthMethodType.PIN -> {
                        val currentText = if (step == 1) firstInput else secondInput
                        PinDotIndicator(
                            pinLength = currentText.length,
                            maxDigits = 8,
                            isError = errorMessage != null
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SecurityKeypad(
                            showConfirmButton = currentText.length >= 4,
                            onDigitClick = { digit ->
                                if (currentText.length < 8) {
                                    if (step == 1) firstInput += digit else secondInput += digit
                                    errorMessage = null
                                }
                            },
                            onBackspaceClick = {
                                if (step == 1) {
                                    if (firstInput.isNotEmpty()) firstInput = firstInput.dropLast(1)
                                } else {
                                    if (secondInput.isNotEmpty()) secondInput = secondInput.dropLast(1)
                                }
                                errorMessage = null
                            },
                            onConfirmClick = {
                                if (step == 1) {
                                    if (firstInput.length < 4) {
                                        errorMessage = "PIN must be 4 to 8 digits"
                                    } else {
                                        step = 2
                                        errorMessage = null
                                    }
                                } else {
                                    if (firstInput == secondInput) {
                                        onSave(firstInput)
                                    } else {
                                        errorMessage = "PINs do not match. Try again."
                                        secondInput = ""
                                    }
                                }
                            }
                        )
                    }

                    AuthMethodType.PATTERN -> {
                        PatternLockCanvas(
                            isError = errorMessage != null,
                            onPatternComplete = { pattern ->
                                if (step == 1) {
                                    if (pattern.split("-").filter { it.isNotBlank() }.size < 4) {
                                        errorMessage = "Connect at least 4 dots"
                                    } else {
                                        firstInput = pattern
                                        step = 2
                                        errorMessage = null
                                    }
                                } else {
                                    if (firstInput == pattern) {
                                        onSave(firstInput)
                                    } else {
                                        errorMessage = "Patterns do not match. Try again."
                                    }
                                }
                            }
                        )
                    }

                    AuthMethodType.PASSWORD -> {
                        val currentVal = if (step == 1) firstInput else secondInput
                        OutlinedTextField(
                            value = currentVal,
                            onValueChange = {
                                if (step == 1) firstInput = it else secondInput = it
                                errorMessage = null
                            },
                            label = { Text(if (step == 1) "Enter Password" else "Confirm Password") },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = TextSecondary
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonEmerald,
                                unfocusedBorderColor = ObsidianBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = ObsidianSurfaceVariant,
                                unfocusedContainerColor = ObsidianSurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (step == 1) {
                                    if (firstInput.length < 6) {
                                        errorMessage = "Password must be at least 6 characters"
                                    } else {
                                        step = 2
                                        errorMessage = null
                                    }
                                } else {
                                    if (firstInput == secondInput) {
                                        onSave(firstInput)
                                    } else {
                                        errorMessage = "Passwords do not match. Try again."
                                    }
                                }
                            },
                            enabled = currentVal.length >= 6,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonEmerald,
                                contentColor = ObsidianBlack
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (step == 1) "Next: Confirm" else "Save Password", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
