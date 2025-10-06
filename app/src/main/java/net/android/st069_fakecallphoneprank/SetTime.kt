package net.android.st069_fakecallphoneprank

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import net.android.st069_fakecallphoneprank.databinding.FragmentSetTimeBinding

class SetTime : Fragment() {

    private var _binding: FragmentSetTimeBinding? = null
    private val binding get() = _binding!!

    private var currentSetTime: Int = 0
    private var selectedSetTime: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentSetTime = it.getInt("CURRENT_SET_TIME", 0)
        }
        selectedSetTime = currentSetTime

        // Handle back button press
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                saveAndReturn()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        updateSelection(selectedSetTime)
    }

    private fun setupClickListeners() {
        // Back button
        binding.layoutTitle.setOnClickListener {
            saveAndReturn()
        }

        // Save button
        view?.findViewById<View>(R.id.tvSave)?.setOnClickListener {
            saveAndReturn()
        }

        // Now (0 seconds)
        binding.ivNow.setOnClickListener {
            selectTime(0)
        }

        // 15 seconds
        binding.iv15s.setOnClickListener {
            selectTime(15)
        }

        // 30 seconds
        binding.iv30s.setOnClickListener {
            selectTime(30)
        }

        // 1 minute (60 seconds)
        binding.iv1m.setOnClickListener {
            selectTime(60)
        }

        // 5 minutes (300 seconds)
        binding.iv5m.setOnClickListener {
            selectTime(300)
        }

        // 10 minutes (600 seconds)
        binding.iv10m.setOnClickListener {
            selectTime(600)
        }

        // Add Time - Show NumberPicker Dialog
        binding.layoutAddTime.setOnClickListener {
            showTimePickerDialog()
        }


    }

    private fun showTimePickerDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_time_picker)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Get views
        val hourPicker = dialog.findViewById<com.shawnlin.numberpicker.NumberPicker>(R.id.hourPicker)
        val minutePicker = dialog.findViewById<com.shawnlin.numberpicker.NumberPicker>(R.id.minutePicker)
        val tvHourValue = dialog.findViewById<TextView>(R.id.tvHourValue)
        val tvMinValue = dialog.findViewById<TextView>(R.id.tvMinValue)
        val btnDone = dialog.findViewById<TextView>(R.id.btnDone)

        // Set initial values (convert current seconds to hours and minutes)
        val currentHours = selectedSetTime / 3600
        val currentMinutes = (selectedSetTime % 3600) / 60
        hourPicker.value = currentHours
        minutePicker.value = currentMinutes

        // Update TextViews with initial values
        tvHourValue.text = String.format("%02d", currentHours)
        tvMinValue.text = String.format("%02d", currentMinutes)

        // Hour picker value change listener
        hourPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            tvHourValue.text = String.format("%02d", newVal)
        }

        // Minute picker value change listener
        minutePicker.setOnValueChangedListener { picker, oldVal, newVal ->
            tvMinValue.text = String.format("%02d", newVal)
        }

        // Done button
        btnDone.setOnClickListener {
            val hours = hourPicker.value
            val minutes = minutePicker.value
            val totalSeconds = (hours * 3600) + (minutes * 60)

            selectedSetTime = totalSeconds
            updateSelection(totalSeconds)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun selectTime(seconds: Int) {
        selectedSetTime = seconds
        updateSelection(seconds)
    }

    private fun updateSelection(seconds: Int) {
        // Reset all to disabled state
        binding.ivNow.setImageResource(R.drawable.bg_disable)
        binding.tvNow.setTextColor(resources.getColor(R.color.text_secondary, null))

        binding.iv15s.setImageResource(R.drawable.bg_disable)
        binding.tv15s.setTextColor(resources.getColor(R.color.text_secondary, null))

        binding.iv30s.setImageResource(R.drawable.bg_disable)
        binding.tv30s.setTextColor(resources.getColor(R.color.text_secondary, null))

        binding.iv1m.setImageResource(R.drawable.bg_disable)
        binding.tv1m.setTextColor(resources.getColor(R.color.text_secondary, null))

        binding.iv5m.setImageResource(R.drawable.bg_disable)
        binding.tv5m.setTextColor(resources.getColor(R.color.text_secondary, null))

        binding.iv10m.setImageResource(R.drawable.bg_disable)
        binding.tv10m.setTextColor(resources.getColor(R.color.text_secondary, null))

        // Set selected state for predefined options
        when (seconds) {
            0 -> {
                binding.ivNow.setImageResource(R.drawable.bg_enable)
                binding.tvNow.setTextColor(resources.getColor(android.R.color.white, null))
            }
            15 -> {
                binding.iv15s.setImageResource(R.drawable.bg_enable)
                binding.tv15s.setTextColor(resources.getColor(android.R.color.white, null))
            }
            30 -> {
                binding.iv30s.setImageResource(R.drawable.bg_enable)
                binding.tv30s.setTextColor(resources.getColor(android.R.color.white, null))
            }
            60 -> {
                binding.iv1m.setImageResource(R.drawable.bg_enable)
                binding.tv1m.setTextColor(resources.getColor(android.R.color.white, null))
            }
            300 -> {
                binding.iv5m.setImageResource(R.drawable.bg_enable)
                binding.tv5m.setTextColor(resources.getColor(android.R.color.white, null))
            }
            600 -> {
                binding.iv10m.setImageResource(R.drawable.bg_enable)
                binding.tv10m.setTextColor(resources.getColor(android.R.color.white, null))
            }
            // For custom times, don't highlight any predefined option
        }
    }

    private fun saveAndReturn() {
        // Send result back to Activity or Fragment
        setFragmentResult("SET_TIME_RESULT", bundleOf("SET_TIME" to selectedSetTime))

        // Pop back stack
        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}