package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AegisDatabase
import com.example.data.db.HiddenAppEntity
import com.example.data.db.SecretNoteEntity
import com.example.data.db.VaultMediaEntity
import com.example.data.model.AuthMethodType
import com.example.data.model.SecurityConfig
import com.example.data.repository.InstalledAppItem
import com.example.data.repository.PackageManagerRepository
import com.example.data.repository.VaultRepository
import com.example.data.security.PermissionManager
import com.example.data.security.SecurityRepository
import com.example.data.security.ShieldPermissionsState
import com.example.service.VaultShieldService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class VaultTab {
    APPS, MEDIA, NOTES
}

enum class SetupStep {
    SELECT_METHOD,
    INPUT_CREDENTIAL,
    CONFIRM_CREDENTIAL
}

sealed interface AuthState {
    data object SetupRequired : AuthState
    data object Locked : AuthState
    data object Unlocked : AuthState
}

data class AegisUiState(
    val authState: AuthState = AuthState.Locked,
    val authMethod: AuthMethodType = AuthMethodType.PIN,
    val configuredAuthMethods: List<AuthMethodType> = listOf(AuthMethodType.PIN),
    val securityConfig: SecurityConfig = SecurityConfig(),
    // Setup state
    val setupStep: SetupStep = SetupStep.SELECT_METHOD,
    val pendingSetupMethod: AuthMethodType = AuthMethodType.PIN,
    val pendingCredential: String = "",
    val setupError: String? = null,
    // Lock state
    val lockInput: String = "",
    val lockError: String? = null,
    val shakeErrorTrigger: Int = 0,
    val lockoutSeconds: Int = 0,
    val interceptedPackage: String? = null,
    val interceptedAppName: String? = null,
    // Permissions & Real-time Shield
    val shieldPermissions: ShieldPermissionsState = ShieldPermissionsState(false, false, false),
    // Dashboard state
    val currentTab: VaultTab = VaultTab.APPS,
    val hiddenApps: List<HiddenAppEntity> = emptyList(),
    val secretNotes: List<SecretNoteEntity> = emptyList(),
    val vaultMedia: List<VaultMediaEntity> = emptyList(),
    val installedApps: List<InstalledAppItem> = emptyList(),
    val isScanningApps: Boolean = false,
    val isAppPickerOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val selectedAppForConcealment: HiddenAppEntity? = null,
    val editingNote: SecretNoteEntity? = null,
    val isNoteEditorOpen: Boolean = false,
    val selectedMedia: VaultMediaEntity? = null,
    val searchQuery: String = "",
    val feedbackMessage: String? = null
)

class AegisVaultViewModel(application: Application) : AndroidViewModel(application) {
    private val securityRepository = SecurityRepository(application)
    private val packageManagerRepository = PackageManagerRepository(application)
    private val vaultRepository = VaultRepository(AegisDatabase.getInstance(application))
    private val permissionManager = PermissionManager(application)

    private val _uiState = MutableStateFlow(AegisUiState())
    val uiState: StateFlow<AegisUiState> = _uiState.asStateFlow()

    private var lockoutJob: Job? = null

    init {
        checkInitialAuthState()
        observeSecurityConfig()
        observeVaultData()
        refreshPermissions()
    }

    private fun observeSecurityConfig() {
        viewModelScope.launch {
            securityRepository.securityConfig.collect { config ->
                _uiState.update { current ->
                    val configured = config.configuredMethods
                    val currentMethodValid = configured.contains(current.authMethod)
                    current.copy(
                        securityConfig = config,
                        configuredAuthMethods = configured,
                        authMethod = if (currentMethodValid) current.authMethod else config.activeMethod
                    )
                }
            }
        }
    }

    fun refreshPermissions() {
        val permState = permissionManager.getPermissionsState(isServiceRunning = true)
        _uiState.update { it.copy(shieldPermissions = permState) }

        if (permState.hasUsageStats) {
            VaultShieldService.startService(getApplication())
        }
    }

    fun onRequestUsageStatsPermission() {
        permissionManager.requestUsageStatsPermission()
    }

    fun onRequestOverlayPermission() {
        permissionManager.requestOverlayPermission()
    }

    fun onInterceptPackage(packageName: String) {
        val app = _uiState.value.hiddenApps.find { it.packageName == packageName }
        val appName = app?.appName ?: packageName
        _uiState.update {
            it.copy(
                authState = AuthState.Locked,
                interceptedPackage = packageName,
                interceptedAppName = appName,
                lockInput = "",
                lockError = "Locked Application: $appName"
            )
        }
    }

    private fun checkInitialAuthState() {
        val isSetup = securityRepository.isSetupComplete()
        val config = securityRepository.securityConfig.value

        if (!isSetup) {
            _uiState.update {
                it.copy(
                    authState = AuthState.SetupRequired,
                    setupStep = SetupStep.SELECT_METHOD,
                    securityConfig = config,
                    configuredAuthMethods = config.configuredMethods
                )
            }
        } else {
            val now = System.currentTimeMillis()
            val remainingLockout = if (config.lockoutUntilMs > now) {
                ((config.lockoutUntilMs - now) / 1000).toInt() + 1
            } else 0

            _uiState.update {
                it.copy(
                    authState = AuthState.Locked,
                    authMethod = config.activeMethod,
                    configuredAuthMethods = config.configuredMethods,
                    securityConfig = config,
                    lockoutSeconds = remainingLockout
                )
            }

            if (remainingLockout > 0) {
                startLockoutCountdown(remainingLockout)
            }
        }
    }

    private fun observeVaultData() {
        viewModelScope.launch {
            vaultRepository.hiddenApps.collect { apps ->
                _uiState.update { it.copy(hiddenApps = apps) }
                if (apps.isNotEmpty() && permissionManager.hasUsageStatsPermission()) {
                    VaultShieldService.startService(getApplication())
                }
            }
        }
        viewModelScope.launch {
            vaultRepository.secretNotes.collect { notes ->
                _uiState.update { it.copy(secretNotes = notes) }
            }
        }
        viewModelScope.launch {
            vaultRepository.vaultMedia.collect { media ->
                _uiState.update { it.copy(vaultMedia = media) }
            }
        }
    }

    // --- SETUP FLOW ---

    fun onSelectSetupMethod(method: AuthMethodType) {
        _uiState.update {
            it.copy(
                pendingSetupMethod = method,
                setupStep = SetupStep.INPUT_CREDENTIAL,
                pendingCredential = "",
                setupError = null
            )
        }
    }

    fun onSubmitSetupCredential(credential: String) {
        val method = _uiState.value.pendingSetupMethod
        val error = validateCredentialFormat(method, credential)
        if (error != null) {
            _uiState.update { it.copy(setupError = error) }
            return
        }

        _uiState.update {
            it.copy(
                pendingCredential = credential,
                setupStep = SetupStep.CONFIRM_CREDENTIAL,
                setupError = null
            )
        }
    }

    fun onConfirmSetupCredential(confirmation: String) {
        val original = _uiState.value.pendingCredential
        if (original != confirmation) {
            _uiState.update {
                it.copy(
                    setupError = "Credentials do not match. Please try again.",
                    shakeErrorTrigger = it.shakeErrorTrigger + 1
                )
            }
            return
        }

        val method = _uiState.value.pendingSetupMethod
        securityRepository.saveMasterCredential(method, original)

        _uiState.update {
            it.copy(
                authState = AuthState.Unlocked,
                authMethod = method,
                pendingCredential = "",
                setupError = null,
                feedbackMessage = "Aegis Master Shield configured successfully"
            )
        }
    }

    fun onCancelSetupStep() {
        _uiState.update {
            when (it.setupStep) {
                SetupStep.CONFIRM_CREDENTIAL -> it.copy(
                    setupStep = SetupStep.INPUT_CREDENTIAL,
                    setupError = null
                )
                SetupStep.INPUT_CREDENTIAL -> it.copy(
                    setupStep = SetupStep.SELECT_METHOD,
                    setupError = null
                )
                SetupStep.SELECT_METHOD -> it
            }
        }
    }

    private fun validateCredentialFormat(method: AuthMethodType, credential: String): String? {
        return when (method) {
            AuthMethodType.PIN -> {
                if (credential.length < 4) "PIN must be at least 4 digits"
                else if (credential.length > 8) "PIN cannot exceed 8 digits"
                else null
            }
            AuthMethodType.PATTERN -> {
                if (credential.split("-").filter { it.isNotBlank() }.size < 4) {
                    "Connect at least 4 dots for pattern security"
                } else null
            }
            AuthMethodType.PASSWORD -> {
                if (credential.length < 6) "Password must be at least 6 characters"
                else null
            }
        }
    }

    // --- LOCK SCREEN FLOW ---

    fun onSwitchLockMethod(method: AuthMethodType) {
        _uiState.update {
            it.copy(
                authMethod = method,
                lockInput = "",
                lockError = null
            )
        }
    }

    fun onKeypadDigit(digit: String) {
        if (_uiState.value.lockoutSeconds > 0) return
        val current = _uiState.value.lockInput
        if (current.length < 8) {
            val updated = current + digit
            _uiState.update { it.copy(lockInput = updated, lockError = null) }
            // Only auto-submit when reaching the maximum 8 digits
            if (updated.length == 8) {
                tryUnlock(updated)
            }
        }
    }

    fun onKeypadBackspace() {
        val current = _uiState.value.lockInput
        if (current.isNotEmpty()) {
            _uiState.update { it.copy(lockInput = current.dropLast(1), lockError = null) }
        }
    }

    fun onPatternDrawn(patternString: String) {
        if (_uiState.value.lockoutSeconds > 0) return
        tryUnlock(patternString)
    }

    fun onPasswordInputChanged(password: String) {
        _uiState.update { it.copy(lockInput = password, lockError = null) }
    }

    fun onUnlockSubmitted() {
        tryUnlock(_uiState.value.lockInput)
    }

    private fun tryUnlock(input: String) {
        if (input.isBlank()) return
        val currentMethod = _uiState.value.authMethod
        val result = securityRepository.verifyCredential(currentMethod, input)
        when (result) {
            is SecurityRepository.VerifyResult.Success -> {
                val intercepted = _uiState.value.interceptedPackage
                if (intercepted != null) {
                    VaultShieldService.allowSessionForPackage(getApplication(), intercepted)
                    packageManagerRepository.launchApp(intercepted)
                    _uiState.update {
                        it.copy(
                            authState = AuthState.Unlocked,
                            interceptedPackage = null,
                            interceptedAppName = null,
                            lockInput = "",
                            lockError = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            authState = AuthState.Unlocked,
                            lockInput = "",
                            lockError = null,
                            lockoutSeconds = 0
                        )
                    }
                }
            }
            is SecurityRepository.VerifyResult.Failed -> {
                _uiState.update {
                    it.copy(
                        lockInput = "",
                        lockError = "Incorrect ${currentMethod.label}. ${result.attemptsLeft} attempts remaining.",
                        shakeErrorTrigger = it.shakeErrorTrigger + 1
                    )
                }
            }
            is SecurityRepository.VerifyResult.LockedOut -> {
                _uiState.update {
                    it.copy(
                        lockInput = "",
                        lockError = "Security lockout active. Try again later.",
                        lockoutSeconds = result.remainingSeconds,
                        shakeErrorTrigger = it.shakeErrorTrigger + 1
                    )
                }
                startLockoutCountdown(result.remainingSeconds)
            }
        }
    }

    private fun startLockoutCountdown(seconds: Int) {
        lockoutJob?.cancel()
        lockoutJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000)
                remaining--
                _uiState.update { it.copy(lockoutSeconds = remaining) }
            }
            _uiState.update { it.copy(lockError = null) }
        }
    }

    fun onLockVault() {
        _uiState.update {
            it.copy(
                authState = AuthState.Locked,
                lockInput = "",
                lockError = null
            )
        }
    }

    // --- SETTINGS & CREDENTIALS MANAGEMENT ---

    fun onOpenSettings() {
        _uiState.update { it.copy(isSettingsOpen = true) }
    }

    fun onDismissSettings() {
        _uiState.update { it.copy(isSettingsOpen = false) }
    }

    fun onSaveConfiguredCredential(method: AuthMethodType, credential: String) {
        securityRepository.saveMasterCredential(method, credential)
        _uiState.update {
            it.copy(
                feedbackMessage = "${method.label} updated successfully",
                authMethod = method
            )
        }
    }

    fun onRemoveCredential(method: AuthMethodType) {
        val success = securityRepository.removeCredential(method)
        if (success) {
            _uiState.update {
                it.copy(feedbackMessage = "${method.label} removed")
            }
        } else {
            _uiState.update {
                it.copy(feedbackMessage = "Cannot remove the only configured unlock method")
            }
        }
    }

    fun onResetAllSecurity() {
        securityRepository.resetSecurity()
        _uiState.update {
            it.copy(
                authState = AuthState.SetupRequired,
                setupStep = SetupStep.SELECT_METHOD,
                isSettingsOpen = false,
                lockInput = "",
                lockError = null,
                lockoutSeconds = 0,
                feedbackMessage = "Vault security reset. Please configure master credentials."
            )
        }
    }

    // --- DASHBOARD NAVIGATION & ACTIONS ---

    fun onSelectTab(tab: VaultTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }

    // --- APP HIDER ENGINE ---

    fun onOpenAppPicker() {
        _uiState.update { it.copy(isAppPickerOpen = true, isScanningApps = true) }
        viewModelScope.launch {
            val apps = packageManagerRepository.getInstalledLaunchableApps()
            _uiState.update { it.copy(installedApps = apps, isScanningApps = false) }
        }
    }

    fun onDismissAppPicker() {
        _uiState.update { it.copy(isAppPickerOpen = false) }
    }

    fun onImportApp(app: InstalledAppItem) {
        viewModelScope.launch {
            vaultRepository.addHiddenApp(
                packageName = app.packageName,
                appName = app.appName,
                launchActivity = app.launchActivity,
                category = if (app.isSystemApp) "System" else "User App"
            )
            if (permissionManager.hasUsageStatsPermission()) {
                VaultShieldService.startService(getApplication())
            }
            _uiState.update {
                it.copy(
                    isAppPickerOpen = false,
                    feedbackMessage = "${app.appName} added to Hidden Vault & Shield Guard"
                )
            }
        }
    }

    fun onLaunchHiddenApp(app: HiddenAppEntity) {
        viewModelScope.launch {
            VaultShieldService.allowSessionForPackage(getApplication(), app.packageName)
            val success = packageManagerRepository.launchApp(app.packageName)
            if (success) {
                vaultRepository.recordAppLaunch(app.packageName)
            } else {
                _uiState.update {
                    it.copy(
                        feedbackMessage = "Unable to launch ${app.appName}. App may be restricted."
                    )
                }
            }
        }
    }

    fun onUnhideApp(app: HiddenAppEntity) {
        viewModelScope.launch {
            vaultRepository.removeHiddenApp(app.packageName)
            _uiState.update {
                it.copy(feedbackMessage = "${app.appName} removed from vault")
            }
        }
    }

    fun onToggleConcealment(app: HiddenAppEntity, isConcealed: Boolean) {
        viewModelScope.launch {
            vaultRepository.toggleConcealmentStatus(app.packageName, isConcealed)
        }
    }

    fun onOpenConcealmentGuidance(app: HiddenAppEntity) {
        _uiState.update { it.copy(selectedAppForConcealment = app) }
    }

    fun onDismissConcealmentGuidance() {
        _uiState.update { it.copy(selectedAppForConcealment = null) }
    }

    fun onOpenSystemAppDetails(packageName: String) {
        packageManagerRepository.openAppSystemSettings(packageName)
    }

    fun onOpenSystemUninstall(packageName: String) {
        packageManagerRepository.openAppUninstallPrompt(packageName)
    }

    fun getAppIcon(packageName: String) = packageManagerRepository.getAppIcon(packageName)

    // --- SECRET NOTES ---

    fun onOpenNoteEditor(note: SecretNoteEntity? = null) {
        _uiState.update {
            it.copy(
                editingNote = note,
                isNoteEditorOpen = true
            )
        }
    }

    fun onDismissNoteEditor() {
        _uiState.update {
            it.copy(
                editingNote = null,
                isNoteEditorOpen = false
            )
        }
    }

    fun onSaveNote(id: String?, title: String, content: String, category: String) {
        if (title.isBlank() && content.isBlank()) return
        viewModelScope.launch {
            vaultRepository.saveSecretNote(
                id = id,
                title = if (title.isBlank()) "Untitled Note" else title,
                content = content,
                category = category
            )
            _uiState.update {
                it.copy(
                    editingNote = null,
                    isNoteEditorOpen = false,
                    feedbackMessage = "Secret Note safely encrypted"
                )
            }
        }
    }

    fun onDeleteNote(id: String) {
        viewModelScope.launch {
            vaultRepository.deleteSecretNote(id)
            _uiState.update {
                it.copy(
                    editingNote = null,
                    isNoteEditorOpen = false,
                    feedbackMessage = "Note deleted"
                )
            }
        }
    }

    // --- VAULT MEDIA ---

    fun onAddMedia(uriString: String, title: String, mimeType: String, sizeBytes: Long) {
        viewModelScope.launch {
            vaultRepository.addVaultMedia(
                title = title,
                uriString = uriString,
                mimeType = mimeType,
                fileSizeBytes = sizeBytes
            )
            _uiState.update {
                it.copy(feedbackMessage = "Media imported into Private Vault")
            }
        }
    }

    fun onDeleteMedia(id: String) {
        viewModelScope.launch {
            vaultRepository.deleteVaultMedia(id)
            _uiState.update {
                it.copy(
                    selectedMedia = null,
                    feedbackMessage = "Private media removed"
                )
            }
        }
    }

    fun onSelectMedia(media: VaultMediaEntity?) {
        _uiState.update { it.copy(selectedMedia = media) }
    }
}
