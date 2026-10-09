package com.clipvault.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.clipvault.app.ClipVaultApp
import com.clipvault.app.model.ClipEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ClipViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as ClipVaultApp).repository

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // All clips (with search filter applied)
    @OptIn(ExperimentalCoroutinesApi::class)
    val clips: StateFlow<List<ClipEntry>> = _searchQuery
        .debounce(200)
        .flatMapLatest { query ->
            if (query.isBlank()) repo.allClips
            else repo.search(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Only links
    val linkClips: StateFlow<List<ClipEntry>> = repo.linkClips
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(q: String) { _searchQuery.value = q }

    fun togglePin(clip: ClipEntry) = viewModelScope.launch {
        repo.togglePin(clip)
    }

    fun delete(clip: ClipEntry) = viewModelScope.launch {
        repo.delete(clip)
    }

    fun clearAll() = viewModelScope.launch {
        repo.clearAll()
    }
}
