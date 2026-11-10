package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HiddenAppDao {
    @Query("SELECT * FROM hidden_apps ORDER BY dateHiddenMs DESC")
    fun getAllHiddenApps(): Flow<List<HiddenAppEntity>>

    @Query("SELECT * FROM hidden_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getByPackageName(packageName: String): HiddenAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHiddenApp(app: HiddenAppEntity)

    @Update
    suspend fun updateHiddenApp(app: HiddenAppEntity)

    @Query("UPDATE hidden_apps SET launchCount = launchCount + 1 WHERE packageName = :packageName")
    suspend fun incrementLaunchCount(packageName: String)

    @Query("UPDATE hidden_apps SET isConcealedFromLauncher = :isConcealed WHERE packageName = :packageName")
    suspend fun updateConcealedStatus(packageName: String, isConcealed: Boolean)

    @Delete
    suspend fun deleteHiddenApp(app: HiddenAppEntity)

    @Query("DELETE FROM hidden_apps WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)
}

@Dao
interface SecretNoteDao {
    @Query("SELECT * FROM secret_notes ORDER BY updatedAtMs DESC")
    fun getAllSecretNotes(): Flow<List<SecretNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: SecretNoteEntity)

    @Update
    suspend fun updateNote(note: SecretNoteEntity)

    @Delete
    suspend fun deleteNote(note: SecretNoteEntity)

    @Query("DELETE FROM secret_notes WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface VaultMediaDao {
    @Query("SELECT * FROM vault_media ORDER BY dateAddedMs DESC")
    fun getAllVaultMedia(): Flow<List<VaultMediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: VaultMediaEntity)

    @Delete
    suspend fun deleteMedia(media: VaultMediaEntity)

    @Query("DELETE FROM vault_media WHERE id = :id")
    suspend fun deleteById(id: String)
}
