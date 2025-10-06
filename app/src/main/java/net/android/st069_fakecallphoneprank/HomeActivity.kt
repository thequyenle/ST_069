package net.android.st069_fakecallphoneprank

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import net.android.st069_fakecallphoneprank.databinding.ActivityHomeBinding
import net.android.st069_fakecallphoneprank.viewmodel.FakeCallViewModel

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: FakeCallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        // Observe all fake calls
        viewModel.allFakeCalls.observe(this) { fakeCalls ->
            // Update UI with fake calls list
            // You can create a RecyclerView adapter to display this
            println("Total fake calls: ${fakeCalls.size}")
        }

        // Observe active calls count
        viewModel.activeCallsCount.observe(this) { count ->
            // Update UI to show active calls count
            println("Active calls: $count")
        }

        // Observe upcoming calls
        viewModel.upcomingCalls.observe(this) { upcomingCalls ->
            // Update UI with upcoming scheduled calls
            println("Upcoming calls: ${upcomingCalls.size}")
        }
    }

    private fun setupClickListeners() {
        // Add Fake Call button
        binding.ivAddCall.setOnClickListener {
            // Navigate to AddFakeCall fragment
            // TODO: Implement navigation
        }

        // Available Fake Call button
        binding.ivAvaibleCall.setOnClickListener {
            // Navigate to list of saved fake calls
            // TODO: Implement navigation
        }

        // More button
        binding.ivMore.setOnClickListener {
            // Navigate to settings/more options
            // TODO: Implement navigation
        }
    }

    // Example: Create a test fake call
    private fun createTestFakeCall() {
        val testCall = net.android.st069_fakecallphoneprank.data.entity.FakeCall(
            name = "John Doe",
            phoneNumber = "099-345-213",
            voiceType = "Male voice",
            deviceType = "Samsung S20",
            setTime = 60, // Call appears after 60 seconds countdown
            talkTime = 120 // Call screen displays for 2 minutes
        )

        viewModel.insert(testCall)
    }
}