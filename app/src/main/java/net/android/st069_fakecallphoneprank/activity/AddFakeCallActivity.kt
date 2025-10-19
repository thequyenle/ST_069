package net.android.st069_fakecallphoneprank.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.base.BaseActivity
import net.android.st069_fakecallphoneprank.data.entity.FakeCall
import net.android.st069_fakecallphoneprank.databinding.ActivityAddFakeCallBinding
import net.android.st069_fakecallphoneprank.viewmodel.FakeCallViewModel
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.core.widget.addTextChangedListener
import net.android.st069_fakecallphoneprank.utils.FullscreenHelper
import net.android.st069_fakecallphoneprank.utils.LocaleHelper

class AddFakeCallActivity : BaseActivity() {

    private lateinit var binding: ActivityAddFakeCallBinding
    private val viewModel: FakeCallViewModel by viewModels()

    // Edit mode
    private var isEditMode = false
    private var editingCallId: Long = -1

    // Temporary storage for user selections
    private var selectedAvatar: String? = null
    private var selectedName: String = ""
    private var selectedPhone: String = ""
    private var selectedVoice: String? = null
    private var selectedDevice: String? = null
    private var selectedSetTime: Int = 0
    private var selectedTalkTime: Int = -1

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Copy image to internal storage instead of using content URI
                val savedPath = saveImageToInternalStorage(uri)
                if (savedPath != null) {
                    selectedAvatar = savedPath
                    Glide.with(this)
                        .load(java.io.File(savedPath))
                        .circleCrop()
                        .into(binding.ivAddAvatar)
                    android.util.Log.d("AddFakeCallActivity", "Image saved to: $savedPath")
                } else {
                    Toast.makeText(this, getString(R.string.failed_save_image), Toast.LENGTH_SHORT).show()
                    android.util.Log.e("AddFakeCallActivity", "Failed to save image from URI: $uri")
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddFakeCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable fullscreen edge-to-edge (after setContentView)
        FullscreenHelper.enableFullscreen(this)

        // Check if in edit mode
        isEditMode = intent.getBooleanExtra("EDIT_MODE", false)

        if (isEditMode) {
            loadEditData()
        } else {
            resetToDefaultState()
        }

        binding.layoutTitle.findViewById<View>(R.id.ivBack)?.setOnClickListener {
            finish()
        }

        setupClickListeners()
        setupTextWatchers()
    }

    override fun onResume() {
        super.onResume()
        FullscreenHelper.enableFullscreen(this)
    }

    private fun loadEditData() {
        // Get data from intent
        editingCallId = intent.getLongExtra("FAKE_CALL_ID", -1)
        selectedName = intent.getStringExtra("NAME") ?: ""
        selectedPhone = intent.getStringExtra("PHONE") ?: ""
        selectedAvatar = intent.getStringExtra("AVATAR")

        // Voice data from database is a file path, need to extract display name
        val voiceData = intent.getStringExtra("VOICE")
        if (voiceData != null) {
            selectedVoiceFilePath = voiceData
            // Extract just the filename without extension for display
            selectedVoice = extractVoiceNameFromPath(voiceData)
        }

        selectedDevice = intent.getStringExtra("DEVICE")
        selectedSetTime = intent.getIntExtra("SET_TIME", 0)
        selectedTalkTime = intent.getIntExtra("TALK_TIME", 15)

        // Update UI with loaded data
        updateUIWithData()
    }

    private fun extractVoiceNameFromPath(path: String): String {
        // If it's a URL or file path, extract the filename
        return if (path.contains("/")) {
            val fileName = path.substringAfterLast("/")
            // Remove extension if present
            if (fileName.contains(".")) {
                fileName.substringBeforeLast(".")
            } else {
                fileName
            }
        } else {
            // If it's already just a name, return as is
            path
        }
    }

    private fun updateUIWithData() {
        // Set name
        if (selectedName.isNotEmpty()) {
            binding.etName.setText(selectedName)
        }

        // Set phone
        if (selectedPhone.isNotEmpty()) {
            binding.etPhone.setText(selectedPhone)
        }

        // Set avatar
        if (!selectedAvatar.isNullOrEmpty()) {
            Glide.with(this)
                .load(Uri.parse(selectedAvatar))
                .placeholder(R.drawable.ic_addavatar)
                .circleCrop()
                .into(binding.ivAddAvatar)
        }

        // Update other fields
        updateVoiceText()
        updateDeviceText()
        updateSetTimeText()
        updateTalkTimeText()
        updateButtonStates()
    }

    private fun resetToDefaultState() {
        // Reset all selections
        selectedAvatar = null
        selectedName = ""
        selectedPhone = ""
        selectedVoice = null
        selectedDevice = null
        selectedSetTime = 0
        selectedTalkTime = -1

        // Reset EditText fields
        binding.etName.setText("")
        binding.etPhone.setText("")

        binding.tvVoice.text = getString(R.string.choose_voice_default)
        binding.tvVoice.setTextColor(Color.parseColor("#2F2F2F"))

        binding.tvDevice.text = getString(R.string.select_device_default)
        binding.tvDevice.setTextColor(Color.parseColor("#2F2F2F"))

        binding.tvSetTime.text = getString(R.string.set_time_default)
        binding.tvSetTime.setTextColor(Color.parseColor("#2F2F2F"))

        binding.tvTalkTime.text = getString(R.string.talk_time_default)
        binding.tvTalkTime.setTextColor(Color.parseColor("#2F2F2F"))

        // Reset buttons to disabled state
        binding.ivApply.setImageResource(R.drawable.btn_apply_disable)
        binding.ivPreview.setImageResource(R.drawable.btn_preview_disable)
        binding.ivPreview.isEnabled = false
        binding.ivApply.isEnabled = false
        binding.ivApply.alpha = 0.5f
        binding.ivPreview.alpha = 0.5f
    }

    private fun setupClickListeners() {
        // Add Avatar
        binding.ivAddAvatar.setOnClickListener {
            openImagePicker()
        }

        // Choose Voice
        binding.ivChooseVoice.setOnClickListener {
            showVoiceSelectionDialog()
        }

        // Select Device
        binding.ivSelectDevice.setOnClickListener {
            showDeviceSelectionDialog()
        }

        // Set Time
        binding.ivSetTime.setOnClickListener {
            showSetTimeDialog()
        }

        // Talk Time
        binding.ivTalkTime.setOnClickListener {
            showTalkTimeDialog()
        }

        // Preview
        binding.ivPreview.setOnClickListener {
            if (validateInputs()) {
                previewFakeCall()
            }
        }

        // Apply
        binding.ivApply.setOnClickListener {
            if (validateInputs()) {
                saveFakeCall()
            }
        }

        // History
        binding.ivHitory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupTextWatchers() {
        // Name EditText listener
        binding.etName.addTextChangedListener {
            selectedName = it.toString().trim()
            updateButtonStates()
        }

        // Phone EditText listener
        binding.etPhone.addTextChangedListener {
            selectedPhone = it.toString().trim()
            updateButtonStates()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun showVoiceSelectionDialog() {
        val intent = Intent(this, ChooseVoiceActivity::class.java)
        intent.putExtra("CURRENT_VOICE", selectedVoice)
        voicePickerLauncher.launch(intent)
    }

    private var selectedVoiceFilePath: String? = null

    private val voicePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                selectedVoice = data.getStringExtra("SELECTED_VOICE")
                selectedVoiceFilePath = data.getStringExtra("VOICE_FILE_PATH")

                updateVoiceText()
                updateButtonStates()
            }
        }
    }

    private val devicePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                selectedDevice = data.getStringExtra("SELECTED_DEVICE")
                updateDeviceText()
                updateButtonStates()
            }
        }
    }

    private fun updateDeviceText() {
        if (selectedDevice == null) {
            binding.tvDevice.text = getString(R.string.select_device_default)
            binding.tvDevice.setTextColor(Color.parseColor("#2F2F2F"))
            return
        }
        binding.tvDevice.text = selectedDevice
        binding.tvDevice.setTextColor(Color.BLACK)
    }

    private fun updateVoiceText() {
        if (selectedVoice == null) {
            binding.tvVoice.text = getString(R.string.choose_voice_default)
            binding.tvVoice.setTextColor(Color.parseColor("#2F2F2F"))
            return
        }

        val voiceLabel = getString(R.string.voice_name)
        val text = "$voiceLabel $selectedVoice"
        val spannableString = SpannableString(text)
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#0B89FF")),
            0, voiceLabel.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvVoice.text = spannableString
    }

    private fun updateSetTimeText() {
        if (selectedSetTime < 0) {
            binding.tvSetTime.text = getString(R.string.set_time_default)
            binding.tvSetTime.setTextColor(Color.parseColor("#2F2F2F"))
            return
        }
        val timeText = when (selectedSetTime) {
            0 -> getString(R.string.now)
            15 -> "15s"
            30 -> "30s"
            60 -> "1m"
            300 -> "5m"
            600 -> "10m"
            else -> formatSetTime(selectedSetTime)
        }
        binding.tvSetTime.text = timeText
        binding.tvSetTime.setTextColor(Color.BLACK)
    }

    private fun formatSetTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            minutes > 0 -> "${minutes}m"
            else -> "${secs}s"
        }
    }

    private fun updateTalkTimeText() {
        if (selectedTalkTime <= 0) {
            binding.tvTalkTime.text = getString(R.string.talk_time_default)
            binding.tvTalkTime.setTextColor(Color.parseColor("#2F2F2F"))
            return
        }
        val timeText = when (selectedTalkTime) {
            15 -> "15s"
            30 -> "30s"
            60 -> "1m"
            300 -> "5m"
            600 -> "10m"
            else -> formatSetTime(selectedTalkTime)
        }
        binding.tvTalkTime.text = timeText
        binding.tvTalkTime.setTextColor(Color.BLACK)
    }

    private fun showDeviceSelectionDialog() {
        val intent = Intent(this, SelectDeviceActivity::class.java)
        intent.putExtra("CURRENT_DEVICE", selectedDevice)
        devicePickerLauncher.launch(intent)
    }

    private fun showSetTimeDialog() {
        binding.layoutTitle.visibility = View.GONE
        binding.contentLayout.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE

        val setTimeFragment = net.android.st069_fakecallphoneprank.SetTime().apply {
            arguments = Bundle().apply {
                putInt("CURRENT_SET_TIME", selectedSetTime)
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, setTimeFragment)
            .addToBackStack("SetTime")
            .commit()

        supportFragmentManager.setFragmentResultListener("SET_TIME_RESULT", this) { _, bundle ->
            selectedSetTime = bundle.getInt("SET_TIME", 0)
            updateSetTimeDisplay()

            binding.fragmentContainer.visibility = View.GONE
            binding.layoutTitle.visibility = View.VISIBLE
            binding.contentLayout.visibility = View.VISIBLE

            supportFragmentManager.popBackStack()
        }
    }

    private fun showTalkTimeDialog() {
        binding.contentLayout.visibility = View.GONE
        binding.layoutTitle.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE

        val talkTimeFragment = net.android.st069_fakecallphoneprank.TalkTime().apply {
            arguments = Bundle().apply {
                putInt("CURRENT_TALK_TIME", selectedTalkTime)
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, talkTimeFragment)
            .addToBackStack("TalkTime")
            .commit()

        supportFragmentManager.setFragmentResultListener("TALK_TIME_RESULT", this) { _, bundle ->
            selectedTalkTime = bundle.getInt("TALK_TIME", -1)
            updateTalkTimeDisplay()

            binding.fragmentContainer.visibility = View.GONE
            binding.layoutTitle.visibility = View.VISIBLE
            binding.contentLayout.visibility = View.VISIBLE

            supportFragmentManager.popBackStack()
        }
    }

    private fun updateSetTimeDisplay() {
        updateSetTimeText()
        updateButtonStates()
    }

    private fun updateTalkTimeDisplay() {
        updateTalkTimeText()
        updateButtonStates()
    }

    private fun validateInputs(): Boolean {
        val isValid = selectedName.isNotEmpty() &&
                selectedPhone.isNotEmpty() &&
                !selectedVoice.isNullOrEmpty() &&
                !selectedDevice.isNullOrEmpty() &&
                selectedSetTime >= 0 &&
                selectedTalkTime > 0

        if (!isValid) {
            if (selectedName.isEmpty()) {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
                return false
            }
            if (selectedPhone.isEmpty()) {
                Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show()
                return false
            }
            if (selectedVoice.isNullOrEmpty()) {
                Toast.makeText(this, "Please choose a voice", Toast.LENGTH_SHORT).show()
                return false
            }
            if (selectedDevice.isNullOrEmpty()) {
                Toast.makeText(this, "Please select a device", Toast.LENGTH_SHORT).show()
                return false
            }
            if (selectedSetTime < 0) {
                Toast.makeText(this, "Please set time", Toast.LENGTH_SHORT).show()
                return false
            }
            if (selectedTalkTime <= 0) {
                Toast.makeText(this, "Please set talk time", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    private fun createRoundedDrawable(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            cornerRadius = resources.getDimensionPixelSize(R.dimen.button_corner_radius).toFloat()
            setColor(color)
        }
    }

    private fun updateButtonStates() {
        val isValid = selectedName.isNotEmpty() &&
                selectedPhone.isNotEmpty() &&
                !selectedVoice.isNullOrEmpty() &&
                !selectedDevice.isNullOrEmpty() &&
                selectedSetTime >= 0 &&
                selectedTalkTime > 0

        if (isValid) {
            binding.ivApply.setImageDrawable(createRoundedDrawable(Color.parseColor("#2596FF")))
            binding.ivPreview.setImageDrawable(createRoundedDrawable(Color.parseColor("#E53877")))
            binding.ivPreview.isEnabled = true
            binding.ivApply.isEnabled = true
            binding.ivApply.alpha = 1.0f
            binding.ivPreview.alpha = 1.0f
        } else {
            binding.ivApply.setImageResource(R.drawable.btn_apply_disable)
            binding.ivPreview.setImageResource(R.drawable.btn_preview_disable)
            binding.ivPreview.isEnabled = false
            binding.ivApply.isEnabled = false
            binding.ivApply.alpha = 0.5f
            binding.ivPreview.alpha = 0.5f
        }
    }

    private fun saveFakeCall() {
        if (isEditMode && editingCallId != -1L) {
            // UPDATE existing call
            lifecycleScope.launch {
                val existingCall = viewModel.getFakeCallById(editingCallId)
                if (existingCall != null) {
                    val updatedCall = existingCall.copy(
                        avatar = selectedAvatar,
                        name = selectedName,
                        phoneNumber = selectedPhone,
                        voiceType = selectedVoiceFilePath ?: selectedVoice, // Store file path for playback
                        deviceType = selectedDevice,
                        setTime = selectedSetTime,
                        talkTime = selectedTalkTime,
                        scheduledTime = System.currentTimeMillis() + (selectedSetTime * 1000L)
                    )
                    viewModel.update(updatedCall)

                    // Reschedule the alarm
                    val scheduler = net.android.st069_fakecallphoneprank.services.FakeCallScheduler(this@AddFakeCallActivity)
                    scheduler.cancelFakeCall(editingCallId)
                    scheduler.scheduleFakeCall(updatedCall)

                    Toast.makeText(this@AddFakeCallActivity, "Fake call updated successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        } else {
            // INSERT new call
            val fakeCall = FakeCall(
                avatar = selectedAvatar,
                name = selectedName,
                phoneNumber = selectedPhone,
                voiceType = selectedVoiceFilePath ?: selectedVoice, // Store file path for playback
                deviceType = selectedDevice,
                setTime = selectedSetTime,
                talkTime = selectedTalkTime,
                isActive = true
            )

            viewModel.insert(fakeCall)

            Toast.makeText(this, "Fake call scheduled successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun previewFakeCall() {
        // Launch IncomingCallActivity directly for immediate preview
        val intent = Intent(this, IncomingCallActivity::class.java).apply {
            putExtra("FAKE_CALL_ID", -1L) // -1 for preview (not saved in DB)
            putExtra("NAME", selectedName)
            putExtra("PHONE_NUMBER", selectedPhone)
            putExtra("AVATAR", selectedAvatar)
            putExtra("VOICE_TYPE", selectedVoiceFilePath ?: selectedVoice)
            putExtra("DEVICE_TYPE", selectedDevice)
            putExtra("TALK_TIME", selectedTalkTime)
        }
        startActivity(intent)

        Toast.makeText(this,
            getString(R.string.preview_fake_call_triggered_immediately), Toast.LENGTH_SHORT).show()
    }

    /**
     * Copy image from content URI to app's internal storage
     * Returns the absolute file path, or null if failed
     */
    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            // Create avatars directory in internal storage
            val avatarsDir = java.io.File(filesDir, "avatars")
            if (!avatarsDir.exists()) {
                avatarsDir.mkdirs()
            }

            // Generate unique filename
            val timestamp = System.currentTimeMillis()
            val fileName = "avatar_$timestamp.jpg"
            val destFile = java.io.File(avatarsDir, fileName)

            // Copy image data
            contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Return absolute path
            destFile.absolutePath
        } catch (e: Exception) {
            android.util.Log.e("AddFakeCallActivity", "Error saving image to internal storage", e)
            null
        }
    }

}