package com.example.ui.dashboard

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import com.example.data.model.AuthMethodType
import com.example.ui.settings.VaultSettingsDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.HiddenAppEntity
import com.example.data.db.SecretNoteEntity
import com.example.data.db.VaultMediaEntity
import com.example.data.repository.InstalledAppItem
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
import com.example.ui.viewmodel.AegisUiState
import com.example.ui.viewmodel.VaultTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultDashboardScreen(
    state: AegisUiState,
    onSelectTab: (VaultTab) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onLockVault: () -> Unit,
    // App actions
    onOpenAppPicker: () -> Unit,
    onDismissAppPicker: () -> Unit,
    onImportApp: (InstalledAppItem) -> Unit,
    onLaunchApp: (HiddenAppEntity) -> Unit,
    onUnhideApp: (HiddenAppEntity) -> Unit,
    onToggleConcealment: (HiddenAppEntity, Boolean) -> Unit,
    onOpenConcealmentGuidance: (HiddenAppEntity) -> Unit,
    onDismissConcealmentGuidance: () -> Unit,
    onOpenSystemSettings: (String) -> Unit,
    onOpenSystemUninstall: (String) -> Unit,
    getAppIcon: (String) -> Bitmap?,
    // Note actions
    onOpenNoteEditor: (SecretNoteEntity?) -> Unit,
    onDismissNoteEditor: () -> Unit,
    onSaveNote: (String?, String, String, String) -> Unit,
    onDeleteNote: (String) -> Unit,
    // Media actions
    onSelectMedia: (VaultMediaEntity?) -> Unit,
    onAddMedia: (String, String, String, Long) -> Unit,
    onDeleteMedia: (String) -> Unit,
    onClearFeedback: () -> Unit,
    onRequestUsageStats: () -> Unit = {},
    onRequestOverlay: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onDismissSettings: () -> Unit = {},
    onSaveConfiguredCredential: (AuthMethodType, String) -> Unit = { _, _ -> },
    onRemoveCredential: (AuthMethodType) -> Unit = {},
    onResetAllSecurity: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onAddMedia(
                uri.toString(),
                "Vault Photo ${System.currentTimeMillis() % 10000}",
                "image/jpeg",
                0L
            )
        }
    }

    LaunchedEffect(state.feedbackMessage) {
        state.feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onClearFeedback()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        containerColor = ObsidianBlack,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            )
        },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianSurface)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Brand Icon
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NeonEmerald.copy(alpha = 0.15f))
                            .border(1.dp, NeonEmerald, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AEGIS VAULT",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = when (state.currentTab) {
                                VaultTab.APPS -> "${state.hiddenApps.size} Hidden Apps"
                                VaultTab.MEDIA -> "${state.vaultMedia.size} Private Media"
                                VaultTab.NOTES -> "${state.secretNotes.size} Encrypted Notes"
                            },
                            fontSize = 11.sp,
                            color = CyberCyan
                        )
                    }

                    // Search Toggle
                    IconButton(
                        onClick = {
                            isSearchExpanded = !isSearchExpanded
                            if (!isSearchExpanded) {
                                onSearchQueryChanged("")
                            }
                        },
                        modifier = Modifier.testTag("search_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (isSearchExpanded) NeonEmerald else TextSecondary
                        )
                    }

                    // Vault Settings Button
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ObsidianSurfaceVariant)
                            .border(1.dp, CyberCyan.copy(alpha = 0.4f), CircleShape)
                            .testTag("vault_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Vault Settings",
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Quick Re-Lock Button
                    IconButton(
                        onClick = onLockVault,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ObsidianSurfaceVariant)
                            .border(1.dp, CrimsonAlert.copy(alpha = 0.4f), CircleShape)
                            .testTag("lock_vault_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Vault",
                            tint = CrimsonAlert,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Expandable Search Bar
                AnimatedVisibility(
                    visible = isSearchExpanded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = onSearchQueryChanged,
                            placeholder = { Text("Filter items in vault...", color = TextTertiary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonEmerald,
                                unfocusedBorderColor = ObsidianBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = ObsidianSurfaceVariant,
                                unfocusedContainerColor = ObsidianSurfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("vault_search_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = ObsidianSurface,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = ObsidianBorder,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .navigationBarsPadding()
                    .testTag("dashboard_nav_bar")
            ) {
                // Apps Tab
                NavigationBarItem(
                    selected = state.currentTab == VaultTab.APPS,
                    onClick = { onSelectTab(VaultTab.APPS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (state.hiddenApps.isNotEmpty()) {
                                    Badge(
                                        containerColor = NeonEmerald,
                                        contentColor = ObsidianBlack
                                    ) {
                                        Text("${state.hiddenApps.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Apps, contentDescription = "Hidden Apps")
                        }
                    },
                    label = { Text("Apps", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ObsidianBlack,
                        selectedTextColor = NeonEmerald,
                        indicatorColor = NeonEmerald,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("tab_apps")
                )

                // Media Tab
                NavigationBarItem(
                    selected = state.currentTab == VaultTab.MEDIA,
                    onClick = { onSelectTab(VaultTab.MEDIA) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (state.vaultMedia.isNotEmpty()) {
                                    Badge(
                                        containerColor = ElectricAmber,
                                        contentColor = ObsidianBlack
                                    ) {
                                        Text("${state.vaultMedia.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Private Media")
                        }
                    },
                    label = { Text("Media", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ObsidianBlack,
                        selectedTextColor = ElectricAmber,
                        indicatorColor = ElectricAmber,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("tab_media")
                )

                // Notes Tab
                NavigationBarItem(
                    selected = state.currentTab == VaultTab.NOTES,
                    onClick = { onSelectTab(VaultTab.NOTES) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (state.secretNotes.isNotEmpty()) {
                                    Badge(
                                        containerColor = CyberCyan,
                                        contentColor = ObsidianBlack
                                    ) {
                                        Text("${state.secretNotes.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Description, contentDescription = "Secret Notes")
                        }
                    },
                    label = { Text("Notes", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ObsidianBlack,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("tab_notes")
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (state.currentTab) {
                        VaultTab.APPS -> onOpenAppPicker()
                        VaultTab.MEDIA -> {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        }
                        VaultTab.NOTES -> onOpenNoteEditor(null)
                    }
                },
                containerColor = when (state.currentTab) {
                    VaultTab.APPS -> NeonEmerald
                    VaultTab.MEDIA -> ElectricAmber
                    VaultTab.NOTES -> CyberCyan
                },
                contentColor = ObsidianBlack,
                shape = CircleShape,
                modifier = Modifier
                    .size(60.dp)
                    .testTag("add_app_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Item",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.currentTab) {
                VaultTab.APPS -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ShieldPermissionsBanner(
                            permissionsState = state.shieldPermissions,
                            onRequestUsageStats = onRequestUsageStats,
                            onRequestOverlay = onRequestOverlay,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        HiddenAppsView(
                            hiddenApps = state.hiddenApps,
                            searchQuery = state.searchQuery,
                            getAppIcon = getAppIcon,
                            onLaunchApp = onLaunchApp,
                            onOpenConcealmentGuidance = onOpenConcealmentGuidance,
                            onUnhideApp = onUnhideApp,
                            onOpenAppPicker = onOpenAppPicker,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                VaultTab.MEDIA -> {
                    PrivateMediaView(
                        mediaList = state.vaultMedia,
                        selectedMedia = state.selectedMedia,
                        onSelectMedia = onSelectMedia,
                        onAddMedia = onAddMedia,
                        onDeleteMedia = onDeleteMedia
                    )
                }
                VaultTab.NOTES -> {
                    SecretNotesView(
                        notes = state.secretNotes,
                        searchQuery = state.searchQuery,
                        onOpenEditor = onOpenNoteEditor,
                        onDeleteNote = onDeleteNote
                    )
                }
            }
        }

        // App Picker Bottom Sheet
        if (state.isAppPickerOpen) {
            val hiddenSet = remember(state.hiddenApps) {
                state.hiddenApps.map { it.packageName }.toSet()
            }
            AppPickerSheet(
                installedApps = state.installedApps,
                hiddenPackageNames = hiddenSet,
                isLoading = state.isScanningApps,
                onImportApp = onImportApp,
                onDismiss = onDismissAppPicker
            )
        }

        // Concealment Guidance Dialog
        if (state.selectedAppForConcealment != null) {
            ConcealmentGuidanceDialog(
                app = state.selectedAppForConcealment,
                onOpenSystemSettings = onOpenSystemSettings,
                onOpenUninstall = onOpenSystemUninstall,
                onToggleConcealment = { isConcealed ->
                    onToggleConcealment(state.selectedAppForConcealment, isConcealed)
                },
                onDismiss = onDismissConcealmentGuidance
            )
        }

        // Note Editor Dialog
        if (state.isNoteEditorOpen) {
            NoteEditorDialog(
                note = state.editingNote,
                onSave = onSaveNote,
                onDismiss = onDismissNoteEditor
            )
        }

        // Vault Security Settings Dialog
        if (state.isSettingsOpen) {
            VaultSettingsDialog(
                securityConfig = state.securityConfig,
                onSaveCredential = onSaveConfiguredCredential,
                onRemoveCredential = onRemoveCredential,
                onResetAllSecurity = onResetAllSecurity,
                onDismiss = onDismissSettings
            )
        }
    }
}

@Composable
fun ShieldPermissionsBanner(
    permissionsState: com.example.data.security.ShieldPermissionsState,
    onRequestUsageStats: () -> Unit,
    onRequestOverlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (permissionsState.hasUsageStats && permissionsState.hasOverlay) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(NeonEmerald.copy(alpha = 0.12f))
                .border(1.dp, NeonEmerald.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("shield_active_banner")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NeonEmerald)
                )
                Text(
                    text = "REAL-TIME SHIELD GUARD ACTIVE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonEmerald,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Auto-intercepts app launches",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }
    } else {
        androidx.compose.material3.Card(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, ElectricAmber.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                .testTag("shield_permission_prompt_card"),
            colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = ObsidianSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = ElectricAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Enable Real-Time Auto Lock Guard",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "Grant these 2 system permissions so Aegis can instantly detect and block hidden apps the moment they are tapped on the phone's Home Screen.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.Button(
                        onClick = onRequestUsageStats,
                        enabled = !permissionsState.hasUsageStats,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = if (permissionsState.hasUsageStats) ObsidianSurfaceVariant else CyberCyan,
                            contentColor = if (permissionsState.hasUsageStats) NeonEmerald else ObsidianBlack
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_grant_usage_stats")
                    ) {
                        Text(
                            text = if (permissionsState.hasUsageStats) "✓ Usage Granted" else "1. Usage Access",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    androidx.compose.material3.Button(
                        onClick = onRequestOverlay,
                        enabled = !permissionsState.hasOverlay,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = if (permissionsState.hasOverlay) ObsidianSurfaceVariant else NeonEmerald,
                            contentColor = if (permissionsState.hasOverlay) NeonEmerald else ObsidianBlack
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_grant_overlay")
                    ) {
                        Text(
                            text = if (permissionsState.hasOverlay) "✓ Overlay Active" else "2. Draw Overlay",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

