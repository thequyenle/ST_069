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
    private var selectedTalkTime: Int = 15 // Default 15 seconds

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

        setupClickListeners()
        updateButtonStates()
    }

    private fun setupClickListeners() {
        // Back button
        binding.layoutTitle.findViewById<View>(R.id.tvTitle)?.setOnClickListener {
            finish()
        }

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
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun showNameInputDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        val input = android.widget.EditText(this)
        input.hint = "Enter name"
        input.setText(selectedName)

        builder.setTitle("Contact Name")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                selectedName = input.text.toString()
                binding.tvName.text = selectedName
                binding.tvName.setTextColor(resources.getColor(android.R.color.black, null))
                updateButtonStates()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPhoneInputDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        val input = android.widget.EditText(this)
        input.hint = "Enter phone number"
        input.inputType = android.text.InputType.TYPE_CLASS_PHONE
        input.setText(selectedPhone)

        builder.setTitle("Phone Number")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                selectedPhone = input.text.toString()
                binding.tvPhone.text = selectedPhone
                binding.tvPhone.setTextColor(resources.getColor(android.R.color.black, null))
                updateButtonStates()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showVoiceSelectionDialog() {
        val options = arrayOf("Default", "Mom", "My love", "Cattry", "Male police voice")

        android.app.AlertDialog.Builder(this)
            .setTitle("Choose Voice")
            .setItems(options) { _, which ->
                selectedVoice = options[which]
                Toast.makeText(this, "Selected: ${options[which]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showDeviceSelectionDialog() {
        val options = arrayOf(
            "Pixel 5", "Oppo", "iOS 12", "Samsung S20",
            "Xiaomi", "iOS 13", "Samsung A10", "Vivo"
        )

        android.app.AlertDialog.Builder(this)
            .setTitle("Select Device")
            .setItems(options) { _, which ->
                selectedDevice = options[which]
                Toast.makeText(this, "Selected: ${options[which]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showSetTimeDialog() {
        val options = arrayOf("Now", "15 seconds", "30 seconds", "1 minute", "5 minutes", "10 minutes")
        val values = arrayOf(0, 15, 30, 60, 300, 600)

        android.app.AlertDialog.Builder(this)
            .setTitle("Set Time")
            .setItems(options) { _, which ->
                selectedSetTime = values[which]
                Toast.makeText(this, "Set time: ${options[which]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showTalkTimeDialog() {
        val options = arrayOf("15 seconds", "30 seconds", "1 minute", "5 minutes", "10 minutes")
        val values = arrayOf(15, 30, 60, 300, 600)

        android.app.AlertDialog.Builder(this)
            .setTitle("Talk Time")
            .setItems(options) { _, which ->
                selectedTalkTime = values[which]
                Toast.makeText(this, "Talk time: ${options[which]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun validateInputs(): Boolean {
        if (selectedName.isEmpty()) {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
            return false
        }

        if (selectedPhone.isEmpty()) {
            Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun updateButtonStates() {
        val isValid = selectedName.isNotEmpty() && selectedPhone.isNotEmpty()

        binding.ivPreview.isEnabled = isValid
        binding.ivApply.isEnabled = isValid

        binding.ivPreview.alpha = if (isValid) 1.0f else 0.5f
        binding.ivApply.alpha = if (isValid) 1.0f else 0.5f
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