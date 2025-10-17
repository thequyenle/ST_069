package net.android.st069_fakecallphoneprank

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.willy.ratingbar.ScaleRatingBar
import net.android.st069_fakecallphoneprank.activity.AddFakeCallActivity
import net.android.st069_fakecallphoneprank.activity.AvailableCallsApiActivity
import net.android.st069_fakecallphoneprank.activity.MoreActivity
import net.android.st069_fakecallphoneprank.databinding.ActivityHomeBinding
import net.android.st069_fakecallphoneprank.utils.FullscreenHelper
import net.android.st069_fakecallphoneprank.utils.ImmersiveUtils
import net.android.st069_fakecallphoneprank.viewmodel.FakeCallViewModel

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: FakeCallViewModel by viewModels()
    private lateinit var sharedPreferences: SharedPreferences

    private var backPressCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable fullscreen edge-to-edge (after setContentView)
        FullscreenHelper.enableFullscreen(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)

        setupObservers()
        setupClickListeners()
        setupBackPressHandler()
    }

    override fun onResume() {
        super.onResume()
        val root = findViewById<View>(R.id.main)
        ImmersiveUtils.applyEdgeToEdgeHideNav(this, root, padTopForStatusBar = true)
        FullscreenHelper.enableFullscreen(this)
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
            val intent = Intent(this, AvailableCallsApiActivity::class.java)
            startActivity(intent)
        }

        // More button - Navigate to settings
        binding.ivMore.setOnClickListener {
            val intent = Intent(this, MoreActivity::class.java)
            startActivity(intent)
        }

        // Settings button - Navigate to Settings activity
        binding.ivSetting.setOnClickListener {
            val intent = Intent(this, net.android.st069_fakecallphoneprank.activity.setting::class.java)
            startActivity(intent)
        }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backPressCount++

                if (backPressCount % 2 == 1) {
                    // Odd number - exit app directly
                    finishAffinity()
                } else {
                    // Even number - show rating dialog
                    showRatingDialog()
                }
            }
        })
    }

    private fun showRatingDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_rating, null)
        val ratingBar = dialogView.findViewById<ScaleRatingBar>(R.id.ratingBar)
        val btnVote = dialogView.findViewById<View>(R.id.btnVote)
        val btnCancel = dialogView.findViewById<View>(R.id.btnCancel)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Cannot close by clicking outside
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        var currentRating = 0f
        var hasRated = sharedPreferences.getBoolean("has_rated", false)

        // Prevent re-selecting the same star
        ratingBar.setOnRatingChangeListener { _, rating, fromUser ->
            if (fromUser && rating == currentRating) {
                // Same star clicked - don't change
                return@setOnRatingChangeListener
            }
            if (fromUser) {
                currentRating = rating
            }
        }

        btnVote.setOnClickListener {
            if (currentRating > 0) {
                // Save that user has rated
                sharedPreferences.edit().putBoolean("has_rated", true).apply()

                if (currentRating >= 4) {
                    // Good rating - redirect to Play Store
                    openPlayStore()
                } else {
                    Toast.makeText(this, getString(R.string.thank_you_feedback), Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
                finishAffinity()
            } else {
                Toast.makeText(this, getString(R.string.please_select_rating), Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
            finishAffinity()
        }

        dialog.show()
    }

    private fun openPlayStore() {
        try {
            val uri = Uri.parse("market://details?id=$packageName")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } catch (e: Exception) {
            val uri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }
}