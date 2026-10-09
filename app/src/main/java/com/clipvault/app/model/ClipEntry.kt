package com.clipvault.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clip_entries")
data class ClipEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isLink: Boolean = false
)
