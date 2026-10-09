package com.clipvault.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.clipvault.app.data.ClipDatabase
import com.clipvault.app.data.ClipRepository

class ClipVaultApp : Application() {

    val database by lazy { ClipDatabase.getInstance(this) }
    val repository by lazy { ClipRepository(database.clipDao()) }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "clipvault_channel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            description = "ClipVault background monitoring"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
