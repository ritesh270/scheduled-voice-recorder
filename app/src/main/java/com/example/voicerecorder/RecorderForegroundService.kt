package com.example.voicerecorder

import android.app.*
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import androidx.core.app.NotificationCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RecorderForegroundService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val NOTIF_CHANNEL = "recorder_channel"
        const val NOTIF_ID = 0x1234
    }

    private var recorder: MediaRecorder? = null
    private var recordingFileUri: Uri? = null
    private var recordingStartDate: String? = null
    private var directoryUriString: String? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                directoryUriString = StorageHelper.getDirectoryUri(this)
                startForeground(NOTIF_ID, buildNotification("Recording..."))
                startRecording()
            }
            ACTION_STOP -> {
                stopRecording()
                stopForeground(true)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopRecording()
        super.onDestroy()
    }

    private fun startRecording() {
        try {
            stopRecording()
            val now = Date()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            recordingStartDate = sdf.format(now)

            val filename = "recording_\${recordingStartDate}_\${System.currentTimeMillis()}.mp4"

            // If user provided a SAF directory, try to create a file there
            val dirUri = directoryUriString?.let { Uri.parse(it) }
            if (dirUri != null) {
                // Use DocumentsContract to create a new document
                try {
                    val resolver = contentResolver
                    val mime = "audio/mp4"
                    val contentUri = android.provider.DocumentsContract.createDocument(resolver, dirUri, mime, filename)
                    if (contentUri != null) {
                        recordingFileUri = contentUri
                        val fd = resolver.openFileDescriptor(contentUri, "rw")?.fileDescriptor
                        recorder = MediaRecorder().apply {
                            setAudioSource(MediaRecorder.AudioSource.MIC)
                            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                            setOutputFile(fd)
                            prepare()
                            start()
                        }
                        scheduleMidnightRotate()
                        return
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

            // Fallback: app-specific files dir
            val outDir = File(getExternalFilesDir(null), "Recordings")
            if (!outDir.exists()) outDir.mkdirs()
            val outFile = File(outDir, filename)
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outFile.absolutePath)
                prepare()
                start()
            }
            recordingFileUri = Uri.fromFile(outFile)
            scheduleMidnightRotate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scheduleMidnightRotate() {
        // Schedule a PendingIntent at next midnight to rotate file
        val alarm = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, MidnightRotateReceiver::class.java)
        val pi = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 5)
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
    }

    private fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            recorder = null
        }
    }

    private fun buildNotification(text: String): Notification {
        val notifIntent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, NOTIF_CHANNEL)
            .setContentTitle("Auto Voice Recorder")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val ch = NotificationChannel(NOTIF_CHANNEL, "Recorder", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(ch)
        }
    }

    override fun onBind(intent: Intent?) = null
}
