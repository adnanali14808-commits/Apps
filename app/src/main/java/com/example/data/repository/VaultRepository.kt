package com.example.data.repository

import com.example.data.db.AegisDatabase
import com.example.data.db.HiddenAppEntity
import com.example.data.db.SecretNoteEntity
import com.example.data.db.VaultMediaEntity
import kotlinx.coroutines.flow.Flow

class VaultRepository(private val database: AegisDatabase) {
    val hiddenApps: Flow<List<HiddenAppEntity>> = database.hiddenAppDao().getAllHiddenApps()
    val secretNotes: Flow<List<SecretNoteEntity>> = database.secretNoteDao().getAllSecretNotes()
    val vaultMedia: Flow<List<VaultMediaEntity>> = database.vaultMediaDao().getAllVaultMedia()

    suspend fun addHiddenApp(
        packageName: String,
        appName: String,
        launchActivity: String,
        category: String = "Secured"
    ) {
        val entity = HiddenAppEntity(
            packageName = packageName,
            appName = appName,
            launchActivity = launchActivity,
            category = category,
            dateHiddenMs = System.currentTimeMillis(),
            launchCount = 0,
            isConcealedFromLauncher = false
        )
        database.hiddenAppDao().insertHiddenApp(entity)
    }

    suspend fun removeHiddenApp(packageName: String) {
        database.hiddenAppDao().deleteByPackageName(packageName)
    }

    suspend fun recordAppLaunch(packageName: String) {
        database.hiddenAppDao().incrementLaunchCount(packageName)
    }

    suspend fun toggleConcealmentStatus(packageName: String, isConcealed: Boolean) {
        database.hiddenAppDao().updateConcealedStatus(packageName, isConcealed)
    }

    suspend fun saveSecretNote(
        id: String? = null,
        title: String,
        content: String,
        category: String = "Personal"
    ) {
        val now = System.currentTimeMillis()
        val note = if (id != null) {
            SecretNoteEntity(
                id = id,
                title = title,
                content = content,
                category = category,
                updatedAtMs = now
            )
        } else {
            SecretNoteEntity(
                title = title,
                content = content,
                category = category,
                createdAtMs = now,
                updatedAtMs = now
            )
        }
        database.secretNoteDao().insertNote(note)
    }

    suspend fun deleteSecretNote(id: String) {
        database.secretNoteDao().deleteById(id)
    }

    suspend fun addVaultMedia(
        title: String,
        uriString: String,
        mimeType: String,
        fileSizeBytes: Long = 0L
    ) {
        val entity = VaultMediaEntity(
            title = title,
            uriString = uriString,
            mimeType = mimeType,
            fileSizeBytes = fileSizeBytes,
            dateAddedMs = System.currentTimeMillis()
        )
        database.vaultMediaDao().insertMedia(entity)
    }

    suspend fun deleteVaultMedia(id: String) {
        database.vaultMediaDao().deleteById(id)
    }
}
