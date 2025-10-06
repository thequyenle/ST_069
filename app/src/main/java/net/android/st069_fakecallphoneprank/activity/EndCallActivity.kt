package net.android.st069_fakecallphoneprank.activities

import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.databinding.ActivityEndCallBinding

class EndCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEndCallBinding

    private var name: String = ""
    private var avatar: String? = null
    private var callDuration: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show over lock screen
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        binding = ActivityEndCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from intent
        name = intent.getStringExtra("NAME") ?: "Unknown"
        avatar = intent.getStringExtra("AVATAR")
        callDuration = intent.getIntExtra("CALL_DURATION", 0)

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        // Set caller name
        binding.tvCallerName.text = name

        // Set avatar
        if (!avatar.isNullOrEmpty()) {
            Glide.with(this)
                .load(Uri.parse(avatar))
                .placeholder(R.drawable.ic_addavatar)
                .circleCrop()
                .into(binding.ivAvatar)
        } else {
            binding.ivAvatar.setImageResource(R.drawable.ic_addavatar)
        }

        // Optional: Show call duration
        // val minutes = callDuration / 60
        // val seconds = callDuration % 60
        // binding.tvDuration.text = String.format("Call duration: %02d:%02d", minutes, seconds)
    }

    private fun setupClickListeners() {
        // Return button - close and go back to home
        binding.btnReturn.setOnClickListener {
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}