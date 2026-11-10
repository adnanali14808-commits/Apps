package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AegisDarkColorScheme = darkColorScheme(
    primary = NeonEmerald,
    onPrimary = ObsidianBlack,
    primaryContainer = NeonEmeraldDark,
    onPrimaryContainer = TextPrimary,
    secondary = CyberCyan,
    onSecondary = ObsidianBlack,
    secondaryContainer = CyberCyanMuted,
    onSecondaryContainer = TextPrimary,
    tertiary = ElectricAmber,
    onTertiary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = ObsidianBorder,
    outlineVariant = ObsidianSurfaceSubtle,
    error = CrimsonAlert,
    onError = TextPrimary
)

@Composable
fun AegisVaultTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AegisDarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    AegisVaultTheme(content = content)
}
