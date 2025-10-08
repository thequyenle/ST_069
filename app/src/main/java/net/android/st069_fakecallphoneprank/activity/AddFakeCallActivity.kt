package net.android.st069_fakecallphoneprank.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.data.entity.FakeCall
import net.android.st069_fakecallphoneprank.databinding.ActivityAddFakeCallBinding
import net.android.st069_fakecallphoneprank.viewmodel.FakeCallViewModel
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan

class AddFakeCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddFakeCallBinding
    private val viewModel: FakeCallViewModel by viewModels()

    // Temporary storage for user selections
    private var selectedAvatar: String? = null
    private var selectedName: String = ""
    private var selectedPhone: String = ""
    private var selectedVoice: String? = null
    private var selectedDevice: String? = null
    private var selectedSetTime: Int = 0 // Default "Now"
    private var selectedTalkTime: Int = -1 // -1 means "not selected"

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedAvatar = uri.toString()
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(binding.ivAddAvatar)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFakeCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Make back button functional and reset state on enter
        resetToDefaultState()
        binding.layoutTitle.findViewById<View>(R.id.ivBack)?.setOnClickListener {
            finish()
        }

        setupClickListeners()
    }

    private fun resetToDefaultState() {
        // Reset all selections
        selectedAvatar = null
        selectedName = ""
        selectedPhone = ""
        selectedVoice = null
        selectedDevice = null
        selectedSetTime = 0
        selectedTalkTime = -1 // Changed from 15 to -1

        // Reset text views to default values
        binding.tvName.text = getString(R.string.name)
        binding.tvName.setTextColor(Color.parseColor("#A0A0A0"))

        binding.tvPhone.text = getString(R.string.phone_number)
        binding.tvPhone.setTextColor(Color.parseColor("#A0A0A0"))

        binding.tvVoice.text = "Choose Voice"
        binding.tvVoice.setTextColor(Color.parseColor("#2F2F2F"))

        binding.tvDevice.text = "Select Device"
        binding.tvDevice.setTextColor(Color.parseColor("#2F2F2F"))

        binding.tvSetTime.text = "Set Time"
        binding.tvSetTime.setTextColor(Color.parseColor("#2F2F2F"))

        binding.tvTalkTime.text = "Talk Time"
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

        // Add Name
        binding.ivAddName.setOnClickListener {
            showNameInputDialog()
        }

        // Add Phone
        binding.ivAddPhone.setOnClickListener {
            showPhoneInputDialog()
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

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun showNameInputDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_addfake_name, null)
        val input = dialogView.findViewById<android.widget.EditText>(R.id.etVoiceName)
        val btnCancel = dialogView.findViewById<android.widget.TextView>(R.id.btnCancel)
        val btnOk = dialogView.findViewById<android.widget.TextView>(R.id.btnOk)
        input.setText(selectedName)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnOk.setOnClickListener {
            val name = input.text.toString().trim()
            if (name.isEmpty()) {
                android.widget.Toast.makeText(this, "Please enter a name", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            selectedName = name
            binding.tvName.text = selectedName
            binding.tvName.setTextColor(resources.getColor(android.R.color.black, null))
            updateButtonStates()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showPhoneInputDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_addfake_phonenumber, null)
        val input = dialogView.findViewById<android.widget.EditText>(R.id.etVoiceName)
        val btnCancel = dialogView.findViewById<android.widget.TextView>(R.id.btnCancel)
        val btnOk = dialogView.findViewById<android.widget.TextView>(R.id.btnOk)
        input.setText(selectedPhone)
        input.inputType = android.text.InputType.TYPE_CLASS_PHONE

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnOk.setOnClickListener {
            val phone = input.text.toString().trim()
            if (phone.isEmpty()) {
                android.widget.Toast.makeText(this, "Please enter a phone number", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            selectedPhone = phone
            binding.tvPhone.text = selectedPhone
            binding.tvPhone.setTextColor(resources.getColor(android.R.color.black, null))
            updateButtonStates()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showVoiceSelectionDialog() {
        val intent = Intent(this, ChooseVoiceActivity::class.java)
        intent.putExtra("CURRENT_VOICE", selectedVoice)
        voicePickerLauncher.launch(intent)
    }

    private val voicePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                selectedVoice = data.getStringExtra("SELECTED_VOICE")
                val voiceFilePath = data.getStringExtra("VOICE_FILE_PATH")

                if (voiceFilePath != null) {
                    selectedVoice = voiceFilePath
                }
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
            binding.tvDevice.text = "Select Device"
            binding.tvDevice.setTextColor(Color.parseColor("#2F2F2F"))
            return
        }
        binding.tvDevice.text = selectedDevice
        binding.tvDevice.setTextColor(Color.BLACK)
    }

    private fun updateVoiceText() {
        if (selectedVoice == null) {
            binding.tvVoice.text = "Choose Voice"
            binding.tvVoice.setTextColor(Color.parseColor("#2F2F2F"))
            return
        }

        val text = "Voice: $selectedVoice"
        val spannableString = SpannableString(text)
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#0B89FF")),
            0, "Voice:".length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvVoice.text = spannableString
    }

    private fun updateSetTimeText() {
        if (selectedSetTime < 0) {
            binding.tvSetTime.text = "Set Time"
            binding.tvSetTime.setTextColor(Color.parseColor("#2F2F2F"))
            return
        }
        val timeText = when (selectedSetTime) {
            0 -> "Now"
            15 -> "15s"
            30 -> "30s"
            60 -> "1m"
            300 -> "5m"
            600 -> "10m"
            else -> "${selectedSetTime}s"
        }
        binding.tvSetTime.text = timeText
        binding.tvSetTime.setTextColor(Color.BLACK)
    }

    private fun updateTalkTimeText() {
        if (selectedTalkTime <= 0) {
            binding.tvTalkTime.text = "Talk Time"
            binding.tvTalkTime.setTextColor(Color.parseColor("#2F2F2F"))
            return
        }
        val timeText = when (selectedTalkTime) {
            15 -> "15s"
            30 -> "30s"
            60 -> "1m"
            300 -> "5m"
            600 -> "10m"
            else -> "${selectedTalkTime}s"
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
        // Hide content layout and show Fragment Container
        binding.layoutTitle.visibility = View.GONE
        binding.contentLayout.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE

        // Create SetTime fragment with current value
        val setTimeFragment = net.android.st069_fakecallphoneprank.SetTime().apply {
            arguments = Bundle().apply {
                putInt("CURRENT_SET_TIME", selectedSetTime)
            }
        }

        // Show fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, setTimeFragment)
            .addToBackStack("SetTime")
            .commit()

        // Listen for result
        supportFragmentManager.setFragmentResultListener("SET_TIME_RESULT", this) { _, bundle ->
            selectedSetTime = bundle.getInt("SET_TIME", 0)
            updateSetTimeDisplay()

            // Hide fragment and show content layout
            binding.fragmentContainer.visibility = View.GONE
            binding.layoutTitle.visibility = View.VISIBLE
            binding.contentLayout.visibility = View.VISIBLE

            // Remove fragment
            supportFragmentManager.popBackStack()
        }
    }

    private fun showTalkTimeDialog() {
        // Hide content layout and show Fragment Container
        binding.contentLayout.visibility = View.GONE
        binding.layoutTitle.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE

        // Create TalkTime fragment with current value
        val talkTimeFragment = net.android.st069_fakecallphoneprank.TalkTime().apply {
            arguments = Bundle().apply {
                putInt("CURRENT_TALK_TIME", selectedTalkTime)
            }
        }

        // Show fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, talkTimeFragment)
            .addToBackStack("TalkTime")
            .commit()

        // Listen for result
        supportFragmentManager.setFragmentResultListener("TALK_TIME_RESULT", this) { _, bundle ->
            selectedTalkTime = bundle.getInt("TALK_TIME", -1) // Changed default from 15 to -1
            updateTalkTimeDisplay()

            // Hide fragment and show content layout
            binding.fragmentContainer.visibility = View.GONE
            binding.layoutTitle.visibility = View.VISIBLE
            binding.contentLayout.visibility = View.VISIBLE

            // Remove fragment
            supportFragmentManager.popBackStack()
        }
    }

    private fun updateSetTimeDisplay() {
        val timeText = when (selectedSetTime) {
            0 -> "Now"
            15 -> "15 seconds"
            30 -> "30 seconds"
            60 -> "1 minute"
            300 -> "5 minutes"
            600 -> "10 minutes"
            else -> "${selectedSetTime}s"
        }
        updateSetTimeText()
        updateButtonStates()
    }

    private fun updateTalkTimeDisplay() {
        val timeText = when (selectedTalkTime) {
            15 -> "15 seconds"
            30 -> "30 seconds"
            60 -> "1 minute"
            300 -> "5 minutes"
            600 -> "10 minutes"
            else -> "${selectedTalkTime}s"
        }
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
            // Enable and set colors
            binding.ivApply.setImageDrawable(createRoundedDrawable(Color.parseColor("#2596FF")))
            binding.ivPreview.setImageDrawable(createRoundedDrawable(Color.parseColor("#E53877")))
            binding.ivPreview.isEnabled = true
            binding.ivApply.isEnabled = true
            binding.ivApply.alpha = 1.0f
            binding.ivPreview.alpha = 1.0f
        } else {
            // Disable and set disabled state
            binding.ivApply.setImageResource(R.drawable.btn_apply_disable)
            binding.ivPreview.setImageResource(R.drawable.btn_preview_disable)
            binding.ivPreview.isEnabled = false
            binding.ivApply.isEnabled = false
            binding.ivApply.alpha = 0.5f
            binding.ivPreview.alpha = 0.5f
        }
    }

    private fun saveFakeCall() {
        val fakeCall = FakeCall(
            avatar = selectedAvatar,
            name = selectedName,
            phoneNumber = selectedPhone,
            voiceType = selectedVoice,
            deviceType = selectedDevice,
            setTime = selectedSetTime,
            talkTime = selectedTalkTime,
            isActive = true
        )

        viewModel.insert(fakeCall)

        Toast.makeText(this, "Fake call scheduled successfully!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun previewFakeCall() {
        val message = buildString {
            append("Preview:\n\n")
            append("Name: $selectedName\n")
            append("Phone: $selectedPhone\n")
            append("Voice: ${selectedVoice ?: "Default"}\n")
            append("Device: ${selectedDevice ?: "Default"}\n")
            append("Set Time: ${selectedSetTime}s\n")
            append("Talk Time: ${selectedTalkTime}s")
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("Fake Call Preview")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}