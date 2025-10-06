package net.android.st069_fakecallphoneprank.activities

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.databinding.ActivityActiveCallBinding

class ActiveCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityActiveCallBinding
    private var callTimer: CountDownTimer? = null
    private var voicePlayer: MediaPlayer? = null

    private var fakeCallId: Long = -1
    private var name: String = ""
    private var avatar: String? = null
    private var voiceType: String? = null
    private var talkTime: Int = 15

    private var elapsedSeconds: Int = 0

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

        binding = ActivityActiveCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from intent
        fakeCallId = intent.getLongExtra("FAKE_CALL_ID", -1)
        name = intent.getStringExtra("NAME") ?: "Unknown"
        avatar = intent.getStringExtra("AVATAR")
        voiceType = intent.getStringExtra("VOICE_TYPE")
        talkTime = intent.getIntExtra("TALK_TIME", 15)

        setupUI()
        setupClickListeners()
        startCallTimer()
        playVoice()
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

        // Initialize timer display
        binding.tvTimer.text = "00:00"
    }

    private fun setupClickListeners() {
        // End call button
        binding.btnEndCall.setOnClickListener {
            endCall()
        }

        // Other buttons - optional functionality
        binding.btnMute.setOnClickListener {
            // Toggle mute
            toggleMute()
        }

        binding.btnSpeaker.setOnClickListener {
            // Toggle speaker
            toggleSpeaker()
        }

        // Keypad, Add Call, Camera, Contact - optional implementations
    }

    private fun startCallTimer() {
        // Timer counts UP (showing elapsed time) but ends after talkTime
        callTimer = object : CountDownTimer((talkTime * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                elapsedSeconds++
                updateTimerDisplay(elapsedSeconds)
            }

            override fun onFinish() {
                // TalkTime expired - automatically end call
                endCall()
            }
        }.start()
    }

    private fun updateTimerDisplay(seconds: Int) {
        val minutes = seconds / 60
        val secs = seconds % 60
        binding.tvTimer.text = String.format("%02d:%02d", minutes, secs)
    }

    private fun playVoice() {
        // Play voice if custom voice is set
        if (!voiceType.isNullOrEmpty() && voiceType!!.contains("/")) {
            try {
                voicePlayer = MediaPlayer().apply {
                    setDataSource(voiceType)
                    prepare()
                    start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun toggleMute() {
        voicePlayer?.let { player ->
            if (player.isPlaying) {
                player.setVolume(0f, 0f)
                binding.btnMute.alpha = 0.5f
            } else {
                player.setVolume(1f, 1f)
                binding.btnMute.alpha = 1f
            }
        }
    }

    private fun toggleSpeaker() {
        // Speaker toggle logic
        binding.btnSpeaker.alpha = if (binding.btnSpeaker.alpha == 1f) 0.5f else 1f
    }

    private fun endCall() {
        callTimer?.cancel()
        stopVoice()

        // Go to end call screen
        val intent = Intent(this, EndCallActivity::class.java).apply {
            putExtra("NAME", name)
            putExtra("AVATAR", avatar)
            putExtra("CALL_DURATION", elapsedSeconds)
        }
        startActivity(intent)
        finish()
    }

    private fun stopVoice() {
        voicePlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        voicePlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        callTimer?.cancel()
        stopVoice()
    }
}