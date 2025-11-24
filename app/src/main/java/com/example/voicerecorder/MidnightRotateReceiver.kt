package com.example.voicerecorder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class MidnightRotateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // Restart the recorder service to create a new file for the new day
        val svcIntent = Intent(context, RecorderForegroundService::class.java)
        svcIntent.action = RecorderForegroundService.ACTION_STOP
        ContextCompat.startForegroundService(context, svcIntent)
        // After stopping, start again
        val startIntent = Intent(context, RecorderForegroundService::class.java)
        startIntent.action = RecorderForegroundService.ACTION_START
        ContextCompat.startForegroundService(context, startIntent)
    }
}
