package com.clipvault.app.service

import android.accessibilityservice.AccessibilityService
import android.content.ClipboardManager
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import com.clipvault.app.ClipVaultApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ClipboardAccessibilityService : AccessibilityService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private var clipboardManager: ClipboardManager? = null
    private var lastSavedText: String = ""

    override fun onServiceConnected() {
        super.onServiceConnected()
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        // Listen for clipboard changes directly
        clipboardManager?.addPrimaryClipChangedListener {
            checkAndSaveClipboard()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Also trigger on UI events as a backup capture path
        checkAndSaveClipboard()
    }

    private fun checkAndSaveClipboard() {
        try {
            val clip = clipboardManager?.primaryClip ?: return
            if (clip.itemCount == 0) return
            val text = clip.getItemAt(0)?.text?.toString()?.trim() ?: return
            if (text.isEmpty()) return
            if (text == lastSavedText) return
            lastSavedText = text
            val repo = (application as ClipVaultApp).repository
            scope.launch {
                repo.save(text)
            }
        } catch (e: Exception) {
            // Silently ignore - never crash the service
        }
    }

    override fun onInterrupt() {
        // Required override - nothing to do
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
