package com.example.voicerecorder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class ScheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // Start the recording service when the alarm triggers
        val svcIntent = Intent(context, RecorderForegroundService::class.java)
        svcIntent.action = RecorderForegroundService.ACTION_START
        ContextCompat.startForegroundService(context, svcIntent)
    }
}
