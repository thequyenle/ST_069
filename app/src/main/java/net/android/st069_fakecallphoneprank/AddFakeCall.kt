package net.android.st069_fakecallphoneprank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import net.android.st069_fakecallphoneprank.data.entity.FakeCall
import net.android.st069_fakecallphoneprank.databinding.FragmentAddFakeCallBinding
import net.android.st069_fakecallphoneprank.viewmodel.FakeCallViewModel

class AddFakeCall : Fragment() {

    private var _binding: FragmentAddFakeCallBinding? = null
    private val binding get() = _binding!!

    // Initialize ViewModel
    private val viewModel: FakeCallViewModel by viewModels()

    // Temporary storage for user selections
    private var selectedAvatar: String? = null
    private var selectedVoice: String? = null
    private var selectedDevice: String? = null
    private var selectedSetTime: Int = 0 // Default "Now" (0 seconds countdown)
    private var selectedTalkTime: Int = 15 // Default 15 seconds display duration

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
    }

    private fun setupClickListeners() {
        // Add Avatar button
        binding.ivAddAvatar.setOnClickListener {
            // TODO: Open image picker
            openImagePicker()
        }

        // Choose Voice button
        binding.ivChooseVoice.setOnClickListener {
            // TODO: Navigate to voice selection screen
            navigateToVoiceSelection()
        }

        // Select Device button
        binding.ivSelectDevice.setOnClickListener {
            // TODO: Navigate to device selection screen
            navigateToDeviceSelection()
        }

        // Set Time button
        binding.ivSetTime.setOnClickListener {
            // TODO: Navigate to time selection screen
            navigateToSetTime()
        }

        // Talk Time button
        binding.ivTalkTime.setOnClickListener {
            // TODO: Navigate to talk time selection screen
            navigateToTalkTime()
        }

        // Preview button
        binding.ivPreview.setOnClickListener {
            // TODO: Show preview of fake call
            previewFakeCall()
        }

        // Apply button - Save to database
        binding.ivApply.setOnClickListener {
            saveFakeCall()
        }
    }

    private fun saveFakeCall() {
        // Get input values
        val name = binding.tvName?.text?.toString() ?: ""
        val phoneNumber = binding.tvPhone?.text?.toString() ?: ""

        // Validation
        if (name.isEmpty()) {
            Toast.makeText(context, "Please enter a name", Toast.LENGTH_SHORT).show()
            return
        }

        if (phoneNumber.isEmpty()) {
            Toast.makeText(context, "Please enter a phone number", Toast.LENGTH_SHORT).show()
            return
        }

        // Create FakeCall object
        val fakeCall = FakeCall(
            avatar = selectedAvatar,
            name = name,
            phoneNumber = phoneNumber,
            voiceType = selectedVoice,
            deviceType = selectedDevice,
            setTime = selectedSetTime,
            talkTime = selectedTalkTime,
            isActive = true
        )

        // Save to database
        viewModel.insert(fakeCall)

        Toast.makeText(context, "Fake call created successfully!", Toast.LENGTH_SHORT).show()

        // Navigate back or clear form
        clearForm()
    }

    private fun clearForm() {
        // Reset all fields
        selectedAvatar = null
        selectedVoice = null
        selectedDevice = null
        selectedSetTime = 0 // Reset to "Now"
        selectedTalkTime = 15 // Reset to default 15 seconds

        // Clear input fields
        // binding.tvName?.text = ""
        // binding.tvPhone?.text = ""
    }

    private fun openImagePicker() {
        // TODO: Implement image picker
        Toast.makeText(context, "Image picker - To be implemented", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToVoiceSelection() {
        // TODO: Navigate to voice selection fragment
        Toast.makeText(context, "Voice selection - To be implemented", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToDeviceSelection() {
        // TODO: Navigate to device selection fragment
        Toast.makeText(context, "Device selection - To be implemented", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToSetTime() {
        // TODO: Navigate to set time fragment
        Toast.makeText(context, "Set time - To be implemented", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToTalkTime() {
        // TODO: Navigate to talk time fragment
        Toast.makeText(context, "Talk time - To be implemented", Toast.LENGTH_SHORT).show()
    }

    private fun previewFakeCall() {
        // TODO: Show preview dialog or navigate to preview screen
        Toast.makeText(context, "Preview - To be implemented", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}