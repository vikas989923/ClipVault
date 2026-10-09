package com.clipvault.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.clipvault.app.service.ClipVaultForegroundService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val serviceIntent = Intent(context, ClipVaultForegroundService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}
