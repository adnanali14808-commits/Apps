package com.example.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AegisUiState
import com.example.ui.viewmodel.SetupStep
import kotlin.math.roundToInt

@Composable
fun SetupScreen(
    state: AegisUiState,
    onSelectMethod: (AuthMethodType) -> Unit,
    onSubmitCredential: (String) -> Unit,
    onConfirmCredential: (String) -> Unit,
    onCancelStep: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shakeOffset = rememberShakeOffset(state.shakeErrorTrigger)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .statusBarsPadding()
            .padding(24.dp)
            .offset { IntOffset(shakeOffset.roundToInt(), 0) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.setupStep != SetupStep.SELECT_METHOD) {
                IconButton(
                    onClick = onCancelStep,
                    modifier = Modifier.testTag("setup_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(NeonEmerald)
                )
                Text(
                    text = "AEGIS SHIELD SETUP",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = NeonEmerald
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress Step Indicators
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val steps = listOf(SetupStep.SELECT_METHOD, SetupStep.INPUT_CREDENTIAL, SetupStep.CONFIRM_CREDENTIAL)
            steps.forEachIndexed { index, step ->
                val isActive = step == state.setupStep
                val isDone = steps.indexOf(state.setupStep) > index
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .width(if (isActive) 36.dp else 18.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            when {
                                isDone -> CyberCyan
                                isActive -> NeonEmerald
                                else -> ObsidianSurfaceVariant
                            }
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dynamic Step Content
        AnimatedContent(
            targetState = state.setupStep,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "setup_step_content"
        ) { step ->
            when (step) {
                SetupStep.SELECT_METHOD -> {
                    MethodSelectionStep(
                        onSelectMethod = onSelectMethod
                    )
                }
                SetupStep.INPUT_CREDENTIAL -> {
                    CredentialEntryStep(
                        method = state.pendingSetupMethod,
                        isConfirmation = false,
                        errorMessage = state.setupError,
                        onComplete = onSubmitCredential
                    )
                }
                SetupStep.CONFIRM_CREDENTIAL -> {
                    CredentialEntryStep(
                        method = state.pendingSetupMethod,
                        isConfirmation = true,
                        errorMessage = state.setupError,
                        onComplete = onConfirmCredential
                    )
                }
            }
        }
    }
}

@Composable
private fun MethodSelectionStep(
    onSelectMethod: (AuthMethodType) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(ObsidianSurface)
                .border(2.dp, NeonEmerald, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = NeonEmerald,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Create Master Security Shield",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choose your preferred primary authentication method to protect your hidden apps, media, and private notes.",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MethodOptionCard(
                title = "Numeric PIN",
                subtitle = "Fast 4 to 8 digit quick code for one-hand entry",
                icon = Icons.Default.Dialpad,
                accentColor = NeonEmerald,
                onClick = { onSelectMethod(AuthMethodType.PIN) },
                testTag = "method_pin"
            )

            MethodOptionCard(
                title = "Pattern Lock",
                subtitle = "3x3 tactile connection grid with zero smudge trails",
                icon = Icons.Default.Gesture,
                accentColor = CyberCyan,
                onClick = { onSelectMethod(AuthMethodType.PATTERN) },
                testTag = "method_pattern"
            )

            MethodOptionCard(
                title = "Alphanumeric Password",
                subtitle = "High-entropy text passphrase for maximum security",
                icon = Icons.Default.Password,
                accentColor = Color(0xFFFFB800),
                onClick = { onSelectMethod(AuthMethodType.PASSWORD) },
                testTag = "method_password"
            )
        }
    }
}

@Composable
private fun MethodOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, ObsidianBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CredentialEntryStep(
    method: AuthMethodType,
    isConfirmation: Boolean,
    errorMessage: String?,
    onComplete: (String) -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isConfirmation) "Confirm ${method.label}" else "Set Your ${method.label}",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isConfirmation) {
                "Re-enter your credential to verify"
            } else {
                when (method) {
                    AuthMethodType.PIN -> "Enter 4 to 8 digits for your master PIN"
                    AuthMethodType.PATTERN -> "Draw your unique 3x3 pattern"
                    AuthMethodType.PASSWORD -> "Create a password with at least 6 characters"
                }
            },
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = errorMessage,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = CrimsonAlert,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        when (method) {
            AuthMethodType.PIN -> {
                PinDotIndicator(
                    pinLength = textInput.length,
                    maxDigits = 8,
                    isError = errorMessage != null
                )

                Spacer(modifier = Modifier.height(12.dp))

                SecurityKeypad(
                    showConfirmButton = textInput.length >= 4,
                    onDigitClick = { digit ->
                        if (textInput.length < 8) {
                            textInput += digit
                        }
                    },
                    onBackspaceClick = {
                        if (textInput.isNotEmpty()) {
                            textInput = textInput.dropLast(1)
                        }
                    },
                    onConfirmClick = {
                        if (textInput.length >= 4) {
                            onComplete(textInput)
                            textInput = ""
                        }
                    }
                )
            }

            AuthMethodType.PATTERN -> {
                PatternLockCanvas(
                    isError = errorMessage != null,
                    onPatternComplete = { patternStr ->
                        onComplete(patternStr)
                    }
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
                        value = textInput,
                        onValueChange = { textInput = it },
                        label = { Text(if (isConfirmation) "Confirm Password" else "Master Password") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (textInput.isNotBlank()) {
                                    onComplete(textInput)
                                }
                            }
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
                            .testTag("password_input_field")
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                onComplete(textInput)
                            }
                        },
                        enabled = textInput.length >= 6,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonEmerald,
                            contentColor = ObsidianBlack,
                            disabledContainerColor = ObsidianSurfaceVariant,
                            disabledContentColor = TextSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_credential_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isConfirmation) "Confirm & Enter Vault" else "Continue",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
