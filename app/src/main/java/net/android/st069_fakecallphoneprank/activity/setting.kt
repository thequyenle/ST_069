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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.utils.ImmersiveUtils

class setting : AppCompatActivity() {
    
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
    
    // For ringtone selection
    private val ringtonePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            uri?.let {
                val ringtone = RingtoneManager.getRingtone(this, uri)
                val title = ringtone.getTitle(this)
                ringToneText.text = title
                sharedPreferences.edit().putString("ringtone_uri", uri.toString()).apply()
                sharedPreferences.edit().putString("ringtone_name", title.toString()).apply()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setting)
        
        // Initialize shared preferences
        sharedPreferences = getSharedPreferences("FakeCallSettings", Context.MODE_PRIVATE)
        
        // Initialize views
        initViews()
        
        // Load saved settings
        loadSettings()
        
        // Setup click listeners
        setupClickListeners()
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    
    override fun onResume() {
        super.onResume()
        val root = findViewById<ConstraintLayout>(R.id.main)
        ImmersiveUtils.applyEdgeToEdgeHideNav(this, root, padTopForStatusBar = true)
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
                Toast.makeText(this, "Flash is not available on this device", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            sharedPreferences.edit().putBoolean("flash_enabled", newState).apply()
            
            flashSwitch.setImageResource(
                if (newState) R.drawable.ic_switch_setting_on 
                else R.drawable.ic_switch_setting_off
            )
        }
    }
    
    private fun showRingTimeDialog() {
        val options = arrayOf("5s", "10s", "15s", "20s", "30s")
        val currentRingTime = sharedPreferences.getInt("ring_time", 15)
        val currentIndex = when (currentRingTime) {
            5 -> 0
            10 -> 1
            15 -> 2
            20 -> 3
            30 -> 4
            else -> 2 // Default to 15s
        }
        
        AlertDialog.Builder(this)
            .setTitle("Select Ring Time")
            .setSingleChoiceItems(options, currentIndex) { dialog, which ->
                val selectedTime = when (which) {
                    0 -> 5
                    1 -> 10
                    2 -> 15
                    3 -> 20
                    4 -> 30
                    else -> 15
                }
                
                sharedPreferences.edit().putInt("ring_time", selectedTime).apply()
                phoneCallRingTimeText.text = "${selectedTime}s"
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun openRingtonePicker() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Ringtone")
        
        // Set the current ringtone
        val currentRingtoneUri = sharedPreferences.getString("ringtone_uri", null)
        if (currentRingtoneUri != null) {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(currentRingtoneUri))
        }
        
        ringtonePickerLauncher.launch(intent)
    }
    
    private fun triggerVibration() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }
    }
}