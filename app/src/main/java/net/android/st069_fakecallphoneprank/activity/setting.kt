package net.android.st069_fakecallphoneprank.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.base.BaseActivity
import net.android.st069_fakecallphoneprank.base.DialogHelper
import net.android.st069_fakecallphoneprank.utils.LocaleHelper

class setting : BaseActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var btnBack: ImageButton
    private lateinit var phoneCallRingTimeLayout: ConstraintLayout
    private lateinit var ringToneLayout: ConstraintLayout
    private lateinit var vibrationLayout: ConstraintLayout
    private lateinit var soundLayout: ConstraintLayout
    private lateinit var flashLayout: ConstraintLayout

    private lateinit var phoneCallRingTimeText: TextView
    private lateinit var ringToneText: TextView
    private lateinit var vibrationSwitch: ImageView
    private lateinit var soundSwitch: ImageView
    private lateinit var flashSwitch: ImageView

    private val RINGTONE_REQUEST_CODE = 101
    private val CAMERA_PERMISSION_REQUEST_CODE = 102
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 103

    // For ringtone selection
    private val ringtonePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                }

                uri?.let {
                    val ringtone = RingtoneManager.getRingtone(this, uri)
                    val title = ringtone.getTitle(this)
                    ringToneText.text = title
                    sharedPreferences.edit().putString("ringtone_uri", uri.toString()).apply()
                    sharedPreferences.edit().putString("ringtone_name", title.toString()).apply()
                } ?: run {
                    Toast.makeText(this, getString(R.string.ringtone_updated), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, getString(R.string.failed_to_set_ringtone), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_setting)

        // Initialize shared preferences
        sharedPreferences = getSharedPreferences("FakeCallSettings", Context.MODE_PRIVATE)

        // Initialize views
        initViews()

        // Load saved settings
        loadSettings()

        // Setup click listeners
        setupClickListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        phoneCallRingTimeLayout = findViewById(R.id.phoneCallRingTime)
        ringToneLayout = findViewById(R.id.ringTone)
        vibrationLayout = findViewById(R.id.vibration)
        soundLayout = findViewById(R.id.sound)
        flashLayout = findViewById(R.id.flash)

        // Find TextViews for displaying current settings
        phoneCallRingTimeText = phoneCallRingTimeLayout.findViewById(R.id.tvPhoneCallRingTime)
        ringToneText = ringToneLayout.findViewById(R.id.tvRingtoneName)

        // Find switches
        vibrationSwitch = vibrationLayout.findViewById(R.id.ivVibrationSwitch)
        soundSwitch = soundLayout.findViewById(R.id.ivSoundSwitch)
        flashSwitch = flashLayout.findViewById(R.id.ivFlashSwitch)
    }

    private fun loadSettings() {
        // Load ring time
        val ringTime = sharedPreferences.getInt("ring_time", 15)
        phoneCallRingTimeText.text = "${ringTime}s"

        // Load ringtone
        val ringtoneName = sharedPreferences.getString("ringtone_name", "MiRemix.ogg")
        ringToneText.text = ringtoneName

        // Load switch states
        val vibrationEnabled = sharedPreferences.getBoolean("vibration_enabled", true)
        val soundEnabled = sharedPreferences.getBoolean("sound_enabled", true)
        val flashEnabled = sharedPreferences.getBoolean("flash_enabled", false)

        // Set switch images based on states
        vibrationSwitch.setImageResource(
            if (vibrationEnabled) R.drawable.ic_switch_setting_on
            else R.drawable.ic_switch_setting_off
        )

        soundSwitch.setImageResource(
            if (soundEnabled) R.drawable.ic_switch_setting_on
            else R.drawable.ic_switch_setting_off
        )

        flashSwitch.setImageResource(
            if (flashEnabled) R.drawable.ic_switch_setting_on
            else R.drawable.ic_switch_setting_off
        )
    }

    private fun setupClickListeners() {
        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Phone call ring time
        phoneCallRingTimeLayout.setOnClickListener {
            showRingTimeDialog()
        }

        // Ringtone selection
        ringToneLayout.setOnClickListener {
            openRingtonePicker()
        }

        // Vibration toggle
        vibrationLayout.setOnClickListener {
            val currentState = sharedPreferences.getBoolean("vibration_enabled", true)
            val newState = !currentState
            sharedPreferences.edit().putBoolean("vibration_enabled", newState).apply()

            vibrationSwitch.setImageResource(
                if (newState) R.drawable.ic_switch_setting_on
                else R.drawable.ic_switch_setting_off
            )

            // Provide haptic feedback when enabling vibration
            if (newState) {
                triggerVibration()
            }
        }

        // Sound toggle
        soundLayout.setOnClickListener {
            val currentState = sharedPreferences.getBoolean("sound_enabled", true)
            val newState = !currentState
            sharedPreferences.edit().putBoolean("sound_enabled", newState).apply()

            soundSwitch.setImageResource(
                if (newState) R.drawable.ic_switch_setting_on
                else R.drawable.ic_switch_setting_off
            )
        }

        // Flash toggle
        flashLayout.setOnClickListener {
            val currentState = sharedPreferences.getBoolean("flash_enabled", false)
            val newState = !currentState

            // Check camera flash availability
            if (newState && !packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                Toast.makeText(this,
                    getString(R.string.flash_is_not_available_on_this_device), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check camera permission for Android 6.0+
            if (newState && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                    // Request camera permission
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CAMERA),
                        CAMERA_PERMISSION_REQUEST_CODE
                    )
                    return@setOnClickListener
                }
            }

            sharedPreferences.edit().putBoolean("flash_enabled", newState).apply()

            flashSwitch.setImageResource(
                if (newState) R.drawable.ic_switch_setting_on
                else R.drawable.ic_switch_setting_off
            )
        }
    }

    private fun showRingTimeDialog() {
        val currentRingTime = sharedPreferences.getInt("ring_time", 15)

        // Create dialog with custom layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_phone_call_ring_time, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Get radio buttons
        val radio15s = dialogView.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.radio15s)
        val radio20s = dialogView.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.radio20s)
        val radio30s = dialogView.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.radio30s)
        val radio40s = dialogView.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.radio40s)
        val radio60s = dialogView.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.radio60s)

        val ivRadio15s = dialogView.findViewById<ImageView>(R.id.ivRadio15s)
        val ivRadio20s = dialogView.findViewById<ImageView>(R.id.ivRadio20s)
        val ivRadio30s = dialogView.findViewById<ImageView>(R.id.ivRadio30s)
        val ivRadio40s = dialogView.findViewById<ImageView>(R.id.ivRadio40s)
        val ivRadio60s = dialogView.findViewById<ImageView>(R.id.ivRadio60s)

        val btnDone = dialogView.findViewById<TextView>(R.id.btnDone)

        // Set current selection
        updateRadioSelection(currentRingTime, ivRadio15s, ivRadio20s, ivRadio30s, ivRadio40s, ivRadio60s)

        var selectedTime = currentRingTime

        // Radio button click listeners
        radio15s.setOnClickListener {
            selectedTime = 15
            updateRadioSelection(15, ivRadio15s, ivRadio20s, ivRadio30s, ivRadio40s, ivRadio60s)
        }

        radio20s.setOnClickListener {
            selectedTime = 20
            updateRadioSelection(20, ivRadio15s, ivRadio20s, ivRadio30s, ivRadio40s, ivRadio60s)
        }

        radio30s.setOnClickListener {
            selectedTime = 30
            updateRadioSelection(30, ivRadio15s, ivRadio20s, ivRadio30s, ivRadio40s, ivRadio60s)
        }

        radio40s.setOnClickListener {
            selectedTime = 40
            updateRadioSelection(40, ivRadio15s, ivRadio20s, ivRadio30s, ivRadio40s, ivRadio60s)
        }

        radio60s.setOnClickListener {
            selectedTime = 60
            updateRadioSelection(60, ivRadio15s, ivRadio20s, ivRadio30s, ivRadio40s, ivRadio60s)
        }

        // Done button
        btnDone.setOnClickListener {
            sharedPreferences.edit().putInt("ring_time", selectedTime).apply()
            phoneCallRingTimeText.text = "${selectedTime}s"
            dialog.dismiss()
        }

        dialog.show()
        DialogHelper.applyFullscreenToDialog(dialog)

        // Set dialog size: 258dp width, wrap_content height
        val width = (258 * resources.displayMetrics.density).toInt()
        dialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun updateRadioSelection(
        selectedTime: Int,
        ivRadio15s: ImageView,
        ivRadio20s: ImageView,
        ivRadio30s: ImageView,
        ivRadio40s: ImageView,
        ivRadio60s: ImageView
    ) {
        // Reset all to unchecked
        ivRadio15s.setImageResource(R.drawable.radio_unchecked)
        ivRadio20s.setImageResource(R.drawable.radio_unchecked)
        ivRadio30s.setImageResource(R.drawable.radio_unchecked)
        ivRadio40s.setImageResource(R.drawable.radio_unchecked)
        ivRadio60s.setImageResource(R.drawable.radio_unchecked)

        // Set selected to checked
        when (selectedTime) {
            15 -> ivRadio15s.setImageResource(R.drawable.radio_checked)
            20 -> ivRadio20s.setImageResource(R.drawable.radio_checked)
            30 -> ivRadio30s.setImageResource(R.drawable.radio_checked)
            40 -> ivRadio40s.setImageResource(R.drawable.radio_checked)
            60 -> ivRadio60s.setImageResource(R.drawable.radio_checked)
        }
    }

    private fun openRingtonePicker() {
        try {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,
                getString(R.string.select_ringtone))
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)

            // Set the current ringtone
            val currentRingtoneUri = sharedPreferences.getString("ringtone_uri", null)
            if (currentRingtoneUri != null) {
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(currentRingtoneUri))
            } else {
                // Set default ringtone if no custom ringtone is set
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
            }

            ringtonePickerLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
           // Toast.makeText(this, "Unable to open ringtone picker", Toast.LENGTH_SHORT).show()
        }
    }

    private fun triggerVibration() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ (API 31+)
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                // Android 9-11 (API 28-30)
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8+ (API 26+) - includes Android 9-13
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                // Fallback for older versions (shouldn't reach here for Android 9+)
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }
        } catch (e: Exception) {
            e.printStackTrace()
           // Toast.makeText(this, getString(R.string.vibration_not_available), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, enable flash
                    sharedPreferences.edit().putBoolean("flash_enabled", true).apply()
                    flashSwitch.setImageResource(R.drawable.ic_switch_setting_on)
                    //Toast.makeText(this, "Flash enabled", Toast.LENGTH_SHORT).show()
                } else {
                    // Permission denied
                   // Toast.makeText(this, "Camera permission is required for flash", Toast.LENGTH_SHORT).show()
                }
            }
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
                } else {
                   // Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}