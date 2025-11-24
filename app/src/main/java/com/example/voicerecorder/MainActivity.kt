package com.example.voicerecorder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var btnPickDir: Button
    private lateinit var txtDir: TextView
    private lateinit var timePicker: TimePicker
    private lateinit var btnSchedule: Button
    private lateinit var btnStartNow: Button
    private lateinit var switchEnabled: Switch

    private val pickDir =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
            uri?.let {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                StorageHelper.saveDirectoryUri(this, uri.toString())
                txtDir.text = uri.toString()
                Toast.makeText(this, "Directory saved", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnPickDir = findViewById(R.id.btnPickDir)
        txtDir = findViewById(R.id.txtDir)
        timePicker = findViewById(R.id.timePicker)
        btnSchedule = findViewById(R.id.btnSchedule)
        btnStartNow = findViewById(R.id.btnStartNow)
        switchEnabled = findViewById(R.id.switchEnabled)

        val saved = StorageHelper.getDirectoryUri(this)
        txtDir.text = saved ?: "Not set"

        btnPickDir.setOnClickListener {
            pickDir.launch(null)
        }

        btnSchedule.setOnClickListener {
            scheduleDailyAlarm()
        }

        btnStartNow.setOnClickListener {
            startRecordingNow()
        }

        switchEnabled.setOnCheckedChangeListener { _, isChecked ->
            StorageHelper.saveEnabled(this, isChecked)
            if (!isChecked) cancelAlarms()
        }

        // request overlay/ignore battery optimization guidance
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                // not strict; just helpful guidance
            }
        }
    }

    private fun scheduleDailyAlarm() {
        val hour = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) timePicker.hour else timePicker.currentHour
        val minute = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) timePicker.minute else timePicker.currentMinute

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ScheduleReceiver::class.java)
        val pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pi)

        Toast.makeText(this, "Scheduled daily at %02d:%02d".format(hour, minute), Toast.LENGTH_SHORT).show()
        StorageHelper.saveScheduleTime(this, hour, minute)
        StorageHelper.saveEnabled(this, true)
    }

    private fun cancelAlarms() {
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ScheduleReceiver::class.java)
        val pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        am.cancel(pi)
        Toast.makeText(this, "Alarms cancelled", Toast.LENGTH_SHORT).show()
    }

    private fun startRecordingNow() {
        val intent = Intent(this, RecorderForegroundService::class.java)
        intent.action = RecorderForegroundService.ACTION_START
        ContextCompat.startForegroundService(this, intent)
        Toast.makeText(this, "Recording started (foreground service)", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        val enabled = StorageHelper.isEnabled(this)
        switchEnabled.isChecked = enabled
        val (h,m) = StorageHelper.getScheduleTime(this)
        if (h >= 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                timePicker.hour = h
                timePicker.minute = m
            }
        }
    }
}
