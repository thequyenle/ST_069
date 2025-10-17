package net.android.st069_fakecallphoneprank

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
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

    companion object {
        private const val PREFS_NAME = "FakeCallSettings"
        private const val KEY_RATED = "is_rated"
        private const val KEY_BACK_PRESS_COUNT = "back_press_count"
    }

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

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Load back press count from previous session
        backPressCount = sharedPreferences.getInt(KEY_BACK_PRESS_COUNT, 0)

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
                // Check if user has already rated (in HomeActivity or MoreActivity)
                val hasRated = sharedPreferences.getBoolean(KEY_RATED, false)

                // If user has rated, always exit directly
                if (hasRated) {
                    finishAffinity()
                    return
                }

                // If not rated yet, continue with back press counting
                backPressCount++

                // Save back press count to persist across app sessions
                sharedPreferences.edit().putInt(KEY_BACK_PRESS_COUNT, backPressCount).apply()

                if (backPressCount % 2 == 1) {
                    // Odd number (1st press) - exit app directly
                    finishAffinity()
                } else {
                    // Even number (2nd press) - show rating dialog
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
        val imvAvtRate = dialogView.findViewById<android.widget.ImageView>(R.id.imvAvtRate)
        val tv1 = dialogView.findViewById<android.widget.TextView>(R.id.tv1)
        val tv2 = dialogView.findViewById<android.widget.TextView>(R.id.tv2)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Cannot close by clicking outside
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Hide navigation bar for dialog
        dialog.window?.let { window ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Android 11+ (API 30+)
                    window.setDecorFitsSystemWindows(false)
                    window.insetsController?.let { controller ->
                        // Hide only navigation bar, keep status bar visible
                        controller.hide(WindowInsets.Type.navigationBars())
                        controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                } else {
                    // Android 9-10 (API 28-29)
                    @Suppress("DEPRECATION")
                    window.decorView.systemUiVisibility = (
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            )
                }
            } catch (e: Exception) {
                android.util.Log.w("HomeActivity", "Could not hide navigation bar: ${e.message}")
            }
        }

        var currentRating = 0

        // Set initial state - button disabled with gray color
        btnVote.isEnabled = false
        btnVote.alpha = 0.5f

        // Handle star rating changes with deselect prevention
        ratingBar.setOnRatingChangeListener { ratingBarView, rating, fromUser ->
            if (fromUser) {
                val newRating = rating.toInt()

                // CRITICAL FIX: Prevent deselect when clicking the same star
                // When user clicks the same star, the library tries to deselect it (rating becomes 0)
                // We detect this and restore the previous rating immediately
                if (newRating == 0 && currentRating > 0) {
                    // User clicked the same star that was already selected
                    // Restore the previous rating to keep it selected
                    ratingBarView.rating = currentRating.toFloat()
                    return@setOnRatingChangeListener
                }

                // If somehow the same rating comes through, ignore it
                if (newRating == currentRating) {
                    return@setOnRatingChangeListener
                }

                currentRating = newRating

                // Update UI based on rating
                when (currentRating) {
                    0 -> {
                        imvAvtRate.setImageResource(R.drawable.ic_ask)
                        tv1.text = getString(R.string.do_you_like_the_app)
                        tv2.text = getString(R.string.let_us_know_your_experience)
                        btnVote.isEnabled = false
                        btnVote.alpha = 0.5f
                    }
                    1 -> {
                        imvAvtRate.setImageResource(R.drawable.ic_1star)
                        tv1.text = "Oh, no!"
                        tv2.text = "Please give us some feedback"
                        btnVote.isEnabled = true
                        btnVote.alpha = 1.0f
                    }
                    2 -> {
                        imvAvtRate.setImageResource(R.drawable.ic_2star)
                        tv1.text = "Oh, no!"
                        tv2.text = "Please give us some feedback"
                        btnVote.isEnabled = true
                        btnVote.alpha = 1.0f
                    }
                    3 -> {
                        imvAvtRate.setImageResource(R.drawable.ic_3star)
                        tv1.text = "Could be better!"
                        tv2.text = "How can we improve?"
                        btnVote.isEnabled = true
                        btnVote.alpha = 1.0f
                    }
                    4 -> {
                        imvAvtRate.setImageResource(R.drawable.ic_4star)
                        tv1.text = "We love you too!"
                        tv2.text = "Thanks for your feedback"
                        btnVote.isEnabled = true
                        btnVote.alpha = 1.0f
                    }
                    5 -> {
                        imvAvtRate.setImageResource(R.drawable.ic_5star)
                        tv1.text = "We love you too!"
                        tv2.text = "Thanks for your feedback"
                        btnVote.isEnabled = true
                        btnVote.alpha = 1.0f
                    }
                }
            }
        }

        btnVote.setOnClickListener {
            if (currentRating > 0) {
                // Save that user has rated (same key as MoreActivity)
                sharedPreferences.edit().putBoolean(KEY_RATED, true).apply()

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
}