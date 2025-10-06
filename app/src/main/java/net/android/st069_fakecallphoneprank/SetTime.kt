package net.android.st069_fakecallphoneprank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        // Save button (find the TextView in the layout)
        view?.findViewById<View>(R.id.tvSave)?.setOnClickListener {
            saveAndReturn()
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
    }

    private fun selectTime(seconds: Int) {
        selectedSetTime = seconds
        updateSelection(seconds)
    }

    private fun updateSelection(seconds: Int) {
        // Reset all to disabled state
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

        // Set selected state
        when (seconds) {
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