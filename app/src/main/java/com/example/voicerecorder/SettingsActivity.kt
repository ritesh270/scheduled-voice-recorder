package com.example.voicerecorder

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var btnPickDir: Button
    private lateinit var txtDir: TextView

    private val pickDir =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
            uri?.let {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                StorageHelper.saveDirectoryUri(this, uri.toString())
                txtDir.text = uri.toString()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        btnPickDir = findViewById(R.id.btnPickDirSettings)
        txtDir = findViewById(R.id.txtDirSettings)
        txtDir.text = StorageHelper.getDirectoryUri(this) ?: "Not set"

        btnPickDir.setOnClickListener {
            pickDir.launch(null)
        }
    }
}
