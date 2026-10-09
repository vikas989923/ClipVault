package com.clipvault.app.data

import androidx.room.*
import com.clipvault.app.model.ClipEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipDao {

    @Query("SELECT * FROM clip_entries ORDER BY isPinned DESC, timestamp DESC")
    fun getAllClips(): Flow<List<ClipEntry>>

    @Query("SELECT * FROM clip_entries WHERE isLink = 1 ORDER BY timestamp DESC")
    fun getLinkClips(): Flow<List<ClipEntry>>

    @Query("SELECT * FROM clip_entries WHERE text LIKE '%' || :query || '%' ORDER BY isPinned DESC, timestamp DESC")
    fun searchClips(query: String): Flow<List<ClipEntry>>

    @Query("SELECT text FROM clip_entries ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastClipText(): String?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(clip: ClipEntry)

    @Query("UPDATE clip_entries SET isPinned = :pinned WHERE id = :id")
    suspend fun updatePin(id: Long, pinned: Boolean)

    @Delete
    suspend fun delete(clip: ClipEntry)

    @Query("DELETE FROM clip_entries WHERE isPinned = 0")
    suspend fun deleteAllUnpinned()
}
