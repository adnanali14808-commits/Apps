package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "hidden_apps")
data class HiddenAppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val launchActivity: String,
    val category: String = "General",
    val dateHiddenMs: Long = System.currentTimeMillis(),
    val launchCount: Int = 0,
    val isConcealedFromLauncher: Boolean = false
)

@Entity(tableName = "secret_notes")
data class SecretNoteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val category: String = "Personal",
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "vault_media")
data class VaultMediaEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val uriString: String,
    val mimeType: String,
    val fileSizeBytes: Long = 0L,
    val dateAddedMs: Long = System.currentTimeMillis()
)
