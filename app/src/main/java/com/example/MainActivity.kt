package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.auth.DirectLockScreen
import com.example.ui.auth.SetupScreen
import com.example.ui.dashboard.VaultDashboardScreen
import com.example.ui.theme.AegisVaultTheme
import com.example.ui.viewmodel.AegisVaultViewModel
import com.example.ui.viewmodel.AuthState

class MainActivity : ComponentActivity() {
    private val viewModel: AegisVaultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingIntent(intent)

        setContent {
            AegisVaultTheme {
                AegisVaultApp(
                    viewModel = viewModel,
                    onExitApp = {
                        val state = viewModel.uiState.value
                        if (state.interceptedPackage != null) {
                            // Boot user to Home Screen so protected app cannot be accessed
                            goToHomeScreen()
                        } else {
                            finish()
                        }
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action == ACTION_INTERCEPT_PROTECTED_APP) {
            val lockedPkg = intent.getStringExtra(EXTRA_LOCKED_PACKAGE)
            if (lockedPkg != null) {
                viewModel.onInterceptPackage(lockedPkg)
            }
        }
    }

    private fun goToHomeScreen() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onLockVault()
    }

    companion object {
        const val ACTION_INTERCEPT_PROTECTED_APP = "com.example.action.INTERCEPT_PROTECTED_APP"
        const val EXTRA_LOCKED_PACKAGE = "extra_locked_package"
    }
}

@Composable
fun AegisVaultApp(
    viewModel: AegisVaultViewModel,
    onExitApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Crossfade(
        targetState = state.authState,
        label = "auth_state_transition",
        modifier = modifier.fillMaxSize()
    ) { authState ->
        when (authState) {
            AuthState.SetupRequired -> {
                BackHandler {
                    viewModel.onCancelSetupStep()
                }
                SetupScreen(
                    state = state,
                    onSelectMethod = viewModel::onSelectSetupMethod,
                    onSubmitCredential = viewModel::onSubmitSetupCredential,
                    onConfirmCredential = viewModel::onConfirmSetupCredential,
                    onCancelStep = viewModel::onCancelSetupStep
                )
            }

            AuthState.Locked -> {
                BackHandler {
                    onExitApp()
                }
                DirectLockScreen(
                    state = state,
                    onDigitClick = viewModel::onKeypadDigit,
                    onBackspaceClick = viewModel::onKeypadBackspace,
                    onPatternDrawn = viewModel::onPatternDrawn,
                    onPasswordChanged = viewModel::onPasswordInputChanged,
                    onUnlockSubmit = viewModel::onUnlockSubmitted,
                    onSwitchAuthMethod = viewModel::onSwitchLockMethod
                )
            }

            AuthState.Unlocked -> {
                BackHandler {
                    viewModel.onLockVault()
                }
                VaultDashboardScreen(
                    state = state,
                    onSelectTab = viewModel::onSelectTab,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onLockVault = viewModel::onLockVault,
                    // App actions
                    onOpenAppPicker = viewModel::onOpenAppPicker,
                    onDismissAppPicker = viewModel::onDismissAppPicker,
                    onImportApp = viewModel::onImportApp,
                    onLaunchApp = viewModel::onLaunchHiddenApp,
                    onUnhideApp = viewModel::onUnhideApp,
                    onToggleConcealment = viewModel::onToggleConcealment,
                    onOpenConcealmentGuidance = viewModel::onOpenConcealmentGuidance,
                    onDismissConcealmentGuidance = viewModel::onDismissConcealmentGuidance,
                    onOpenSystemSettings = viewModel::onOpenSystemAppDetails,
                    onOpenSystemUninstall = viewModel::onOpenSystemUninstall,
                    getAppIcon = viewModel::getAppIcon,
                    // Note actions
                    onOpenNoteEditor = viewModel::onOpenNoteEditor,
                    onDismissNoteEditor = viewModel::onDismissNoteEditor,
                    onSaveNote = viewModel::onSaveNote,
                    onDeleteNote = viewModel::onDeleteNote,
                    // Media actions
                    onSelectMedia = viewModel::onSelectMedia,
                    onAddMedia = viewModel::onAddMedia,
                    onDeleteMedia = viewModel::onDeleteMedia,
                    onClearFeedback = viewModel::clearFeedback,
                    // Permission actions
                    onRequestUsageStats = viewModel::onRequestUsageStatsPermission,
                    onRequestOverlay = viewModel::onRequestOverlayPermission,
                    // Settings actions
                    onOpenSettings = viewModel::onOpenSettings,
                    onDismissSettings = viewModel::onDismissSettings,
                    onSaveConfiguredCredential = viewModel::onSaveConfiguredCredential,
                    onRemoveCredential = viewModel::onRemoveCredential,
                    onResetAllSecurity = viewModel::onResetAllSecurity
                )
            }
        }
    }
}
