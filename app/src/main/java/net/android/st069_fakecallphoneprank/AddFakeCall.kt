package net.android.st069_fakecallphoneprank

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import net.android.st069_fakecallphoneprank.data.entity.FakeCall
import net.android.st069_fakecallphoneprank.databinding.FragmentAddFakeCallBinding
import net.android.st069_fakecallphoneprank.viewmodel.FakeCallViewModel

class AddFakeCall : Fragment() {

    private var _binding: FragmentAddFakeCallBinding? = null
    private val binding get() = _binding!!

    // Use activityViewModels to share ViewModel with activity
    private val viewModel: FakeCallViewModel by activityViewModels()

    // Temporary storage for user selections
    private var selectedAvatar: String? = null
    private var selectedName: String = ""
    private var selectedPhone: String = ""
    private var selectedVoice: String? = null
    private var selectedDevice: String? = null
    private var selectedSetTime: Int = 0 // Default "Now" (0 seconds countdown)
    private var selectedTalkTime: Int = 15 // Default 15 seconds display duration

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFakeCallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        updateButtonStates()
    }

    private fun setupClickListeners() {
        // Back button - close fragment
        view?.findViewById<View>(R.id.tvTitle)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Add Avatar button
        binding.ivAddAvatar.setOnClickListener {
            openImagePicker()
        }

        // Add Name - show dialog
        binding.ivAddName.setOnClickListener {
            showNameInputDialog()
        }

        // Add Phone - show dialog
        binding.ivAddPhone.setOnClickListener {
            showPhoneInputDialog()
        }

        // Choose Voice button
        binding.ivChooseVoice.setOnClickListener {
            showVoiceSelectionDialog()
        }

        // Select Device button
        binding.ivSelectDevice.setOnClickListener {
            showDeviceSelectionDialog()
        }

        // Set Time button - Navigate to SetTime fragment
        binding.ivSetTime.setOnClickListener {
            navigateToSetTime()
        }

        // Talk Time button - Navigate to TalkTime fragment
        binding.ivTalkTime.setOnClickListener {
            navigateToTalkTime()
        }

        // Preview button
        binding.ivPreview.setOnClickListener {
            if (validateInputs()) {
                previewFakeCall()
            }
        }

        // Apply button - Save to database
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
        val builder = android.app.AlertDialog.Builder(requireContext())
        val input = android.widget.EditText(requireContext())
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
        val builder = android.app.AlertDialog.Builder(requireContext())
        val input = android.widget.EditText(requireContext())
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

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Choose Voice")
            .setItems(options) { _, which ->
                selectedVoice = options[which]
                Toast.makeText(context, "Selected: ${options[which]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showDeviceSelectionDialog() {
        val options = arrayOf(
            "Pixel 5", "Oppo", "iOS 12", "Samsung S20",
            "Xiaomi", "iOS 13", "Samsung A10", "Vivo"
        )

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Device")
            .setItems(options) { _, which ->
                selectedDevice = options[which]
                Toast.makeText(context, "Selected: ${options[which]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun navigateToSetTime() {
        // Navigate to SetTime fragment
        val setTimeFragment = SetTime().apply {
            arguments = Bundle().apply {
                putInt("CURRENT_SET_TIME", selectedSetTime)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, setTimeFragment)
            .addToBackStack("SetTime")
            .commit()

        // Listen for result via fragment result API
        parentFragmentManager.setFragmentResultListener("SET_TIME_RESULT", this) { _, bundle ->
            selectedSetTime = bundle.getInt("SET_TIME", 0)
            Toast.makeText(context, "Set time: ${selectedSetTime}s", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToTalkTime() {
        // Navigate to TalkTime fragment
        val talkTimeFragment = TalkTime().apply {
            arguments = Bundle().apply {
                putInt("CURRENT_TALK_TIME", selectedTalkTime)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, talkTimeFragment)
            .addToBackStack("TalkTime")
            .commit()

        // Listen for result
        parentFragmentManager.setFragmentResultListener("TALK_TIME_RESULT", this) { _, bundle ->
            selectedTalkTime = bundle.getInt("TALK_TIME", 15)
            Toast.makeText(context, "Talk time: ${selectedTalkTime}s", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(): Boolean {
        if (selectedName.isEmpty()) {
            Toast.makeText(context, "Please enter a name", Toast.LENGTH_SHORT).show()
            return false
        }

        if (selectedPhone.isEmpty()) {
            Toast.makeText(context, "Please enter a phone number", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun updateButtonStates() {
        // Enable/disable buttons based on input
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

        // Save to database (ViewModel will also schedule it with AlarmManager)
        viewModel.insert(fakeCall)

        Toast.makeText(context, "Fake call scheduled successfully!", Toast.LENGTH_SHORT).show()

        // Go back to home
        parentFragmentManager.popBackStack()
    }

    private fun previewFakeCall() {
        // TODO: Show preview of how the call will look
        val message = buildString {
            append("Preview:\n\n")
            append("Name: $selectedName\n")
            append("Phone: $selectedPhone\n")
            append("Voice: ${selectedVoice ?: "Default"}\n")
            append("Device: ${selectedDevice ?: "Default"}\n")
            append("Set Time: ${selectedSetTime}s\n")
            append("Talk Time: ${selectedTalkTime}s")
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Fake Call Preview")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}