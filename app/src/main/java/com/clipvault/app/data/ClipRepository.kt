package com.clipvault.app.data

import com.clipvault.app.model.ClipEntry
import kotlinx.coroutines.flow.Flow

class ClipRepository(private val dao: ClipDao) {

    val allClips: Flow<List<ClipEntry>> = dao.getAllClips()
    val linkClips: Flow<List<ClipEntry>> = dao.getLinkClips()

    fun search(query: String): Flow<List<ClipEntry>> = dao.searchClips(query)

    suspend fun save(text: String) {
        val isLink = text.startsWith("http://") || text.startsWith("https://") || text.startsWith("www.")
        val last = dao.getLastClipText()
        // Skip duplicate
        if (last == text) return
        dao.insert(ClipEntry(text = text, isLink = isLink))
    }

    suspend fun togglePin(clip: ClipEntry) {
        dao.updatePin(clip.id, !clip.isPinned)
    }

    suspend fun delete(clip: ClipEntry) {
        dao.delete(clip)
    }

    suspend fun clearAll() {
        dao.deleteAllUnpinned()
    }
}
