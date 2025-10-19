package net.android.st069_fakecallphoneprank

import android.app.Dialog
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
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

        // Set dim background with custom color
        dialog.window?.apply {
            setDimAmount(0.5f)
            addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
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

        // Reset Add Time to default state
        binding.ivAddTime.visibility = View.VISIBLE
        binding.ivAddTime.setImageResource(R.drawable.ic_addtime)
        binding.layoutAddTime.background = null
        binding.layoutAddTime.elevation = 0f
        binding.layoutAddTime.translationX = 0f
        binding.layoutAddTime.translationY = 0f

        // Reset layout size to wrap_content and marginStart to 18dp
        val layoutParams = binding.layoutAddTime.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        layoutParams.marginStart = (18 * resources.displayMetrics.density).toInt()
        binding.layoutAddTime.layoutParams = layoutParams

        // Reset text position to original
        binding.tvAddTime.gravity = android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL
        val textLayoutParams = binding.tvAddTime.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        textLayoutParams.startToStart = binding.ivAddTime.id
        textLayoutParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
        textLayoutParams.topToTop = binding.ivAddTime.id
        textLayoutParams.bottomToBottom = binding.ivAddTime.id
        textLayoutParams.marginStart = (20 * resources.displayMetrics.density).toInt()
        binding.tvAddTime.layoutParams = textLayoutParams

        binding.tvAddTime.text = getString(R.string.add_time)
        binding.tvAddTime.setTextColor(android.graphics.Color.parseColor("#2F2F2F"))

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
            else -> {
                // For custom times, highlight Add Time button
                if (seconds > 0) {
                    // Create rounded drawable with blue background and shadow
                    val drawable = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        setColor(android.graphics.Color.parseColor("#0B89FF"))
                        cornerRadius = 8f * resources.displayMetrics.density // 8dp corner radius
                    }

                    // Hide the image and set background on the layout container
                    binding.ivAddTime.visibility = View.GONE
                    binding.layoutAddTime.background = drawable

                    // Set size to 133dp x 44dp and marginStart to 22dp
                    val layoutParams = binding.layoutAddTime.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                    layoutParams.width = (133 * resources.displayMetrics.density).toInt()
                    layoutParams.height = (44 * resources.displayMetrics.density).toInt()
                    layoutParams.marginStart = (22 * resources.displayMetrics.density).toInt()
                    binding.layoutAddTime.layoutParams = layoutParams

                    // Add shadow (elevation and translation for shadow effect)
                    binding.layoutAddTime.elevation = 6f * resources.displayMetrics.density
                    binding.layoutAddTime.translationX = -1f * resources.displayMetrics.density
                    binding.layoutAddTime.translationY = 1f * resources.displayMetrics.density

                    // Center the text
                    binding.tvAddTime.gravity = android.view.Gravity.CENTER
                    val textLayoutParams = binding.tvAddTime.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                    textLayoutParams.startToStart = binding.layoutAddTime.id
                    textLayoutParams.endToEnd = binding.layoutAddTime.id
                    textLayoutParams.topToTop = binding.layoutAddTime.id
                    textLayoutParams.bottomToBottom = binding.layoutAddTime.id
                    textLayoutParams.marginStart = 0
                    binding.tvAddTime.layoutParams = textLayoutParams

                    binding.tvAddTime.setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
                    binding.tvAddTime.text = formatTime(seconds)
                }
            }
        }
    }

    private fun formatTime(seconds: Int): String {
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