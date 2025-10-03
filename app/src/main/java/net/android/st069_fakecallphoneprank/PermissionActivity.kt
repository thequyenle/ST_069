package net.android.st069_fakecallphoneprank

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import net.android.st069_fakecallphoneprank.databinding.ActivityPermissionBinding

class PermissionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPermissionBinding

    private val REQ_CODE_OVERLAY = 100
    private val REQ_CODE_NOTIFICATION = 101
    private val REQ_CODE_EXACT_ALARM = 102

    private var isOverlayGranted = false
    private var isNotificationGranted = false
    private var isExactAlarmGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup UI visibility
        setupPermissionItems()

        // Button listeners
        binding.btnStorageToggle.setOnClickListener {
            toggleStoragePermission()
        }

        binding.btnCameraToggle.setOnClickListener {
            toggleCameraPermission()
        }

        binding.btnScreenToggle.setOnClickListener {
            toggleScreenPermission()
        }

        binding.btnContinue.setOnClickListener {
            continueToHome()
        }

        // Initial state check
        updateToggleStates()
        updateContinueButtonVisibility()
    }

    private fun setupPermissionItems() {
        // Show/hide permission items based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            binding.layoutCameraPermission.visibility = View.VISIBLE
        } else {
            binding.layoutCameraPermission.visibility = View.GONE
        }
    }

    private fun toggleStoragePermission() {
        if (!isOverlayGranted) {
            requestOverlayPermission()
        } else {
            Toast.makeText(this, "Storage permission already granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!isNotificationGranted) {
                requestNotificationPermission()
            } else {
                Toast.makeText(this, "Camera & Recorder permission already granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleScreenPermission() {
        if (!isExactAlarmGranted) {
            requestScreenPermission()
        } else {
            Toast.makeText(this, "Screen permission already granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, REQ_CODE_OVERLAY)
            }
        } else {
            isOverlayGranted = true
            updateToggleStates()
            updateContinueButtonVisibility()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQ_CODE_NOTIFICATION
                )
            }
        } else {
            isNotificationGranted = true
            updateToggleStates()
            updateContinueButtonVisibility()
        }
    }

    private fun requestScreenPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivityForResult(intent, REQ_CODE_EXACT_ALARM)
        } else {
            isExactAlarmGranted = true
            updateToggleStates()
            updateContinueButtonVisibility()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_CODE_OVERLAY -> {
                updateToggleStates()
                updateContinueButtonVisibility()
            }
            REQ_CODE_EXACT_ALARM -> {
                updateToggleStates()
                updateContinueButtonVisibility()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQ_CODE_NOTIFICATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
                }
                updateToggleStates()
                updateContinueButtonVisibility()
            }
        }
    }

    private fun updateToggleStates() {
        // Check SYSTEM_ALERT_WINDOW permission
        isOverlayGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }

        // Check POST_NOTIFICATIONS permission
        isNotificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        // Check SCHEDULE_EXACT_ALARM permission
        isExactAlarmGranted = true // For now, assume granted

        // Update toggle button images
        binding.btnStorageToggle.setImageResource(
            if (isOverlayGranted) R.drawable.ic_toggle_on else R.drawable.ic_toggle_off
        )

        binding.btnCameraToggle.setImageResource(
            if (isNotificationGranted) R.drawable.ic_toggle_on else R.drawable.ic_toggle_off
        )

        binding.btnScreenToggle.setImageResource(
            if (isExactAlarmGranted) R.drawable.ic_toggle_on else R.drawable.ic_toggle_off
        )
    }

    private fun updateContinueButtonVisibility() {
        val allGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isOverlayGranted && isNotificationGranted && isExactAlarmGranted
        } else {
            isOverlayGranted && isExactAlarmGranted
        }

        binding.btnContinue.visibility = if (allGranted) View.VISIBLE else View.GONE
    }

    private fun continueToHome() {
        getSharedPreferences("fakecall_prefs", MODE_PRIVATE)
            .edit()
            .putBoolean("permission_done", true)
            .apply()

        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        showSystemUI()
        updateToggleStates()
        updateContinueButtonVisibility()
    }

    private fun Activity.showSystemUI() {
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }
}