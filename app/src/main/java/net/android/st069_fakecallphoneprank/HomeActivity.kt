package net.android.st069_fakecallphoneprank

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import net.android.st069_fakecallphoneprank.activity.AddFakeCallActivity
import net.android.st069_fakecallphoneprank.activity.AvailableCallsActivity
import net.android.st069_fakecallphoneprank.activity.MoreActivity
import net.android.st069_fakecallphoneprank.databinding.ActivityHomeBinding
import net.android.st069_fakecallphoneprank.utils.ImmersiveUtils
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

    override fun onResume() {
        super.onResume()
        val root = findViewById<View>(R.id.main) // your root layout id
        ImmersiveUtils.applyEdgeToEdgeHideNav(this, root, padTopForStatusBar = true)
    }

    private fun setupObservers() {
        // Observe all fake calls
        viewModel.allFakeCalls.observe(this) { fakeCalls ->
            println("Total fake calls: ${fakeCalls.size}")
        }

        // Observe active calls count
        viewModel.activeCallsCount.observe(this) { count ->
            println("Active calls: $count")
        }

        // Observe upcoming calls
        viewModel.upcomingCalls.observe(this) { upcomingCalls ->
            println("Upcoming calls: ${upcomingCalls.size}")
        }
    }

    private fun setupClickListeners() {
        // Add Fake Call button - Launch AddFakeCallActivity
        binding.ivAddCall.setOnClickListener {
            val intent = Intent(this, AddFakeCallActivity::class.java)
            startActivity(intent)
        }

        // Available Fake Call button - Navigate to list activity
        binding.ivAvaibleCall.setOnClickListener {
            val intent = Intent(this, AvailableCallsActivity::class.java)
            startActivity(intent)
        }

        // More button - Navigate to settings
        binding.ivMore.setOnClickListener {
            val intent = Intent(this, MoreActivity::class.java)
            startActivity(intent)
        }
    }
}