# Aegis Vault - Dedicated App Hider & Private Security Vault

Aegis Vault is an independent, high-security Android privacy container that allows users to conceal, manage, and launch private applications, secret photos/videos, and encrypted notes behind a unified multi-method authentication shield—without requiring replacement of the user's default phone launcher.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following core product and architectural decisions were confirmed during initial consultation and will govern the implementation:

- **Concealment & Runtime Strategy**: **Dual-Space Sandbox with Guided System Concealment**. Aegis Vault provides an isolated internal environment for imported apps, paired with step-by-step native OS uninstallation/disablement guidance so the original app is removed from the public launcher and app drawer while remaining fully operational and launchable inside Aegis Vault.
- **Visual Theme & Aesthetic Direction**: **Obsidian Stealth Dark with Neon Security Accents**. Ultra-deep black surfaces (`#0B0E14`, `#131822`), high-contrast electric neon green/cyan indicators (`#00F5A0`, `#00D9F5`), and crisp cyber-minimalist typography.
- **Vault Capabilities Scope**: **Triple Vault Suite**. In addition to application hiding and direct launching, the vault integrates an encrypted private media gallery and encrypted personal notes.
- **Direct Access Post-Setup**: Zero decoy screens, fake calculators, or distracting shell utilities. On every launch after initial credential setup, the app presents a direct, pristine cryptographic lock screen.

---

## 1. Overview & Core Concept

### What It Does
Aegis Vault operates as a local digital safe house on Android devices. Upon first launch, the user defines a master credential (numeric PIN, graphic 3x3 pattern, or alphanumeric password). On all subsequent launches, Aegis Vault immediately prompts for that credential. Once unlocked, users access a secure dashboard featuring:
1. **Hidden Apps Grid**: Instant launch, management, and unhiding of secured applications.
2. **App Import Engine**: Automatic querying of installed packages via Android's `QUERY_ALL_PACKAGES` / package visibility API, enabling quick selection and import into the vault.
3. **Dual-Space Concealment Workflow**: Clear step-by-step status indicators showing whether an app is imported, active in the vault, or still visible in the phone's public launcher.
4. **Private Media Safe**: Local encrypted gallery for sensitive images and videos.
5. **Secret Notes**: Cryptographically protected scratchpad for sensitive credentials, keys, and private thoughts.

### Target Audience & Persona
- Privacy-conscious individuals, business professionals, and security enthusiasts who share their devices with family, colleagues, or friends and require guaranteed separation between public and private applications.
- Users who do not want to sacrifice or switch their OEM home launcher (Pixel Launcher, Samsung One UI, Nova Launcher, etc.) just to hide a subset of sensitive apps (banking, messaging, trading, dating, work profiles).

### Key Value
- **Zero Launcher Disruption**: Runs strictly as an independent Android application.
- **Transparent Security**: Hardware-backed cryptographic hashing (PBKDF2/SHA-256 with salted keys stored in Android Keystore / encrypted preferences).
- **Fast Execution**: Seamless instant launching of hidden applications directly from the vault grid.

---

## 2. User Experience & Visual Design

### Key User Flows

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            USER JOURNEY LIFECYCLE                           │
└─────────────────────────────────────────────────────────────────────────────┘
  First Launch
       │
       ▼
┌──────────────┐     Select Auth Type      ┌─────────────────┐
│ Welcome &    │ ────────────────────────> │ Set Credential  │
│ Security Intro│ <──────────────────────── │ & Confirmation  │
└──────────────┘     Mismatch / Re-enter   └────────┬────────┘
                                                    │ Success
                                                    ▼
                                           ┌─────────────────┐
                                           │ Vault Dashboard │
                                           └─────────────────┘
  Subsequent Launch
       │
       ▼
┌──────────────┐     Correct Credential    ┌─────────────────────────────────┐
│ Direct Lock  │ ────────────────────────> │ Main Hidden Vault Dashboard     │
│ Screen       │                           │  ├── Apps Grid + Launching      │
└──────────────┘                           │  ├── Add Apps FAB (+) Picker   │
                                           │  ├── Private Media Vault        │
                                           │  └── Secret Encrypted Notes     │
                                           └─────────────────────────────────┘
```

#### Flow 1: First-Time Onboarding & Multi-Method Credential Setup
1. **Welcome Screen**: Clean cyber-minimalist introduction explaining the local zero-telemetry architecture.
2. **Method Selector**: Interactive segmented control to choose:
   - **PIN**: 4 to 8-digit numeric keypad with real-time dot animation.
   - **Pattern**: Custom 3x3 tactile touch canvas with gesture connection lines.
   - **Password**: Alphanumeric keyboard with visibility toggle and strength gauge.
3. **Confirmation Step**: Prompts the user to re-enter the chosen credential. Mismatches trigger immediate haptic shake feedback; matches commit the credential hash and salt to encrypted storage and immediately navigate to the Vault Dashboard.

#### Flow 2: Subsequent Launches & Instant Unlock
1. The app initializes directly into the designated lock screen matching the user's saved method.
2. As soon as the PIN/Pattern/Password matches, the lock screen transitions with an animated unlock gate into the Vault Dashboard.
3. Failed attempts provide clear remaining attempt counts and delay lockouts.

#### Flow 3: App Discovery, Import & Concealment
1. Tapping the prominent Floating Action Button (`+`) scans all installed device packages and presents a searchable, filterable installed apps sheet.
2. The user checks apps to hide (e.g., WhatsApp, Banking, Photos).
3. The selected apps appear in the Vault Dashboard. Aegis Vault details the concealment status (System Installed vs. Vault Managed) and offers a one-tap option to uninstall/disable the public APK so the icon completely disappears from the device launcher.

#### Flow 4: Private Media & Encrypted Notes Tabs
- Users can switch between the **Apps Grid**, **Private Media Vault**, and **Secret Notes** via a docked M3 Navigation Bar with fluid animated pills.

### Visual Identity & Theme (Obsidian Stealth Dark)

| Token Name | Hex Value | Semantic Purpose |
| :--- | :--- | :--- |
| `Background` | `#080B10` | Primary deep black canvas |
| `Surface` | `#111622` | Cards, input shields, lock keypad buttons |
| `SurfaceVariant` | `#1A2234` | Elevation layer, borders, secondary tiles |
| `Primary` | `#00F5A0` | Neon Green security accent, unlock buttons, FAB |
| `Secondary` | `#00D9F5` | Cyber Cyan secondary accent, tags, active badges |
| `Error` | `#FF4D6D` | Crimson red alert for invalid credentials |
| `OnSurface` | `#F0F4F8` | Crisp high-legibility primary text |
| `OnSurfaceMuted`| `#8B9BB4` | Subtitles, package IDs, secondary captions |

### Interactive Feedback & Motion
- **Haptic Touch**: Light tick on pattern node contact and keypad presses; heavy error buzz on invalid credentials.
- **Spring Transitions**: Smooth Compose `spring(dampingRatio = 0.8f, stiffness = 300f)` when expanding app details or transitioning between lock and dashboard.
- **Tactile Keypad**: Custom Compose `Canvas` for the 3x3 Pattern Lock with reactive glowing nodes and connecting line rendering.

---

## 3. Key Product Decisions & Trade-Offs

### Decision 1: Operating Without Replacing the Default Phone Launcher
- **Chosen Approach**: Aegis Vault functions as an independent standalone security app. To hide an app from the phone's default launcher, Aegis Vault stores the launch metadata, clones or safeguards the application data container, and initiates the OS package disable/uninstall flow so the app is removed from the public home screen and app drawer.
- **Why**: Replacing the user's default launcher breaks their custom widgets, gestures, OEM animations, and daily muscle memory. Operating independently preserves 100% of the device's native launcher experience.
- **Alternatives Considered**: Making Aegis Vault a custom home launcher. Rejected because users rarely want to replace their entire system launcher solely for privacy.

### Decision 2: Multi-Method Authentication Architecture
- **Chosen Approach**: Unified credential provider supporting PIN, Pattern, and Password via salted SHA-256 / PBKDF2 hashing.
- **Why**: Different users have distinct muscle memories and privacy needs (e.g., patterns are faster in one-handed use, alphanumeric passwords are resilient against smudge attacks).
- **Alternatives Considered**: Biometrics-only. Rejected because biometric credentials can be coerced and are not supported in all runtime environments.

### Decision 3: Local-First Zero-Cloud Architecture
- **Chosen Approach**: All credential hashes, hidden app metadata, notes, and media records reside strictly on-device in SQLite/Room and EncryptedSharedPreferences.
- **Why**: Privacy vaults must never leak package lists or personal notes to external cloud endpoints. Zero network transmission provides absolute privacy.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            AEGIS VAULT ARCHITECTURE                         │
└─────────────────────────────────────────────────────────────────────────────┘
                                  UI LAYER
 ┌──────────────────────┐   ┌────────────────────────┐   ┌───────────────────┐
 │   LockScreenHost     │   │   VaultDashboardScreen │   │ AppPickerSheet    │
 │ (PIN/Pattern/Passwd) │   │ (Apps/Media/Notes)     │   │ (Package Browser) │
 └──────────┬───────────┘   └───────────┬────────────┘   └─────────┬─────────┘
            │                           │                          │
            ▼                           ▼                          ▼
 ┌───────────────────────────────────────────────────────────────────────────┐
 │                      AegisVaultViewModel (StateFlow)                      │
 │    - AuthState (Locked / Setup / Unlocked)                                │
 │    - HiddenAppsState, MediaState, NotesState                              │
 └──────────────────────────────────────┬────────────────────────────────────┘
                                        │
                               DOMAIN / REPO LAYER
 ┌──────────────────────┐   ┌───────────┴────────────┐   ┌───────────────────┐
 │  SecurityRepository  │   │   VaultRepository      │   │ PackageManager    │
 │  - Hash verification │   │   - Hidden Apps DAO    │   │ Bridge            │
 │  - Credential config │   │   - Secret Notes DAO   │   │ - Query Packages  │
 │  - Keystore / Crypto │   │   - Vault Media DAO    │   │ - Launch Intent   │
 └──────────┬───────────┘   └───────────┬────────────┘   └─────────┬─────────┘
            │                           │                          │
            ▼                           ▼                          ▼
 ┌──────────────────────┐   ┌────────────────────────┐   ┌───────────────────┐
 │ Encrypted DataStore/ │   │ Local Room Database    │   │ Android System    │
 │ Keystore Preferences │   │ (AegisDatabase.db)     │   │ Package Manager   │
 └──────────────────────┘   └────────────────────────┘   └───────────────────┘
```

### Core Entities & Data Model

```kotlin
// 1. Hidden Application Entity
@Entity(tableName = "hidden_apps")
data class HiddenAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val launchActivity: String,
    val iconUri: String?,
    val isSystemConcealed: Boolean, // True if uninstalled/disabled from system launcher
    val category: String,
    val dateHiddenMs: Long,
    val launchCount: Int
)

// 2. Secret Encrypted Note Entity
@Entity(tableName = "secret_notes")
data class SecretNoteEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val createdAtMs: Long,
    val updatedAtMs: Long,
    val tag: String
)

// 3. Vault Media Entity
@Entity(tableName = "vault_media")
data class VaultMediaEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val fileName: String,
    val filePath: String,
    val mimeType: String,
    val dateAddedMs: Long,
    val fileSizeBytes: Long
)

// 4. Master Security Credential Model
enum class AuthMethodType { PIN, PATTERN, PASSWORD }

data class SecurityConfig(
    val isSetupComplete: Boolean,
    val authMethod: AuthMethodType,
    val credentialSalt: String,
    val credentialHash: String,
    val failedAttempts: Int,
    val lockoutTimestampMs: Long
)
```

### Interactive State Transitions
- `AegisUiState.SetupRequired`: Displayed on clean installs when no credential exists. Guides user through method selection and confirmation.
- `AegisUiState.Locked`: Standard state on app entry. Keypad/Pattern canvas validates input; transitions to `Unlocked` on correct match.
- `AegisUiState.Unlocked`: Grants access to Vault Dashboard. Loads hidden apps, media, and notes.

---

## 5. Implementation Roadmap & Verification Plan

### Phase 1: Security & Storage Foundation
- Configure `Room` database (`AegisDatabase`) with DAOs for hidden apps, secret notes, and private media.
- Implement `SecurityManager` for salted SHA-256 credential hashing, method selection, and verification state.
- Update `AndroidManifest.xml` with package query permissions (`QUERY_ALL_PACKAGES` and queries block) to read installed apps.

### Phase 2: Lock & Setup UI
- Build `SetupScreen` for onboarding with multi-method selection (PIN, 3x3 Pattern, Password).
- Build `DirectLockScreen` with responsive numeric keypad, custom interactive pattern canvas, and password field.
- Add haptic feedback and shake error animations on invalid inputs.

### Phase 3: Vault Dashboard & App Hider Engine
- Build `MainVaultScreen` featuring the apps grid, launch counters, and search filter.
- Build `AppPickerBottomSheet` to discover installed system applications and add them to the vault.
- Implement seamless package launching via `Intent.FLAG_ACTIVITY_NEW_TASK` and unhide/remove workflows.
- Implement uninstallation/disable guidance dialog to conceal apps from the public phone launcher.

### Phase 4: Private Media & Encrypted Notes
- Integrate local private media vault for photos/videos.
- Integrate secret encrypted notes manager with creation, editing, and search.

### Phase 5: Verification & Polish
- Validate the build using `compile_applet`.
- Verify smooth transitions, edge-to-edge system insets, and touch targets across all screens.
