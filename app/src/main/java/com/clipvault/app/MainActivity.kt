package com.clipvault.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.clipvault.app.service.ClipVaultForegroundService
import com.clipvault.app.ui.ClipVaultApp
import com.clipvault.app.ui.theme.ClipVaultTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start keep-alive foreground service
        startForegroundService(Intent(this, ClipVaultForegroundService::class.java))

        setContent {
            ClipVaultTheme {
                ClipVaultApp(
                    isAccessibilityEnabled = isAccessibilityEnabled(),
                    onEnableAccessibility = { openAccessibilitySettings() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Recompose to check accessibility status on return from settings
        setContent {
            ClipVaultTheme {
                ClipVaultApp(
                    isAccessibilityEnabled = isAccessibilityEnabled(),
                    onEnableAccessibility = { openAccessibilitySettings() }
                )
            }
        }
    }

    private fun isAccessibilityEnabled(): Boolean {
        val prefString = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return prefString.contains(packageName, ignoreCase = true)
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }
}
