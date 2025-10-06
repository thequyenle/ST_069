package net.android.st069_fakecallphoneprank.activities

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.databinding.ActivityIncomingCallBinding

class IncomingCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIncomingCallBinding
    private var mediaPlayer: MediaPlayer? = null

    private var fakeCallId: Long = -1
    private var name: String = ""
    private var phoneNumber: String = ""
    private var avatar: String? = null
    private var voiceType: String? = null
    private var talkTime: Int = 15

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

        binding = ActivityIncomingCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from intent
        fakeCallId = intent.getLongExtra("FAKE_CALL_ID", -1)
        name = intent.getStringExtra("NAME") ?: "Unknown"
        phoneNumber = intent.getStringExtra("PHONE_NUMBER") ?: ""
        avatar = intent.getStringExtra("AVATAR")
        voiceType = intent.getStringExtra("VOICE_TYPE")
        talkTime = intent.getIntExtra("TALK_TIME", 15)

        setupUI()
        setupClickListeners()
        playRingtone()
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
    }

    private fun setupClickListeners() {
        // Accept button - go to active call
        binding.btnAccept.setOnClickListener {
            stopRingtone()
            goToActiveCall()
        }

        // Decline button - end call
        binding.btnDecline.setOnClickListener {
            stopRingtone()
            finish()
        }
    }

    private fun playRingtone() {
        try {
            // Play default ringtone or custom voice
            if (!voiceType.isNullOrEmpty() && voiceType!!.contains("/")) {
                // Custom voice file
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(voiceType)
                    prepare()
                    isLooping = true
                    start()
                }
            } else {
                // Default ringtone
                val ringtoneUri = android.media.RingtoneManager.getDefaultUri(
                    android.media.RingtoneManager.TYPE_RINGTONE
                )
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(this@IncomingCallActivity, ringtoneUri)
                    prepare()
                    isLooping = true
                    start()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRingtone() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }

    private fun goToActiveCall() {
        val intent = Intent(this, ActiveCallActivity::class.java).apply {
            putExtra("FAKE_CALL_ID", fakeCallId)
            putExtra("NAME", name)
            putExtra("PHONE_NUMBER", phoneNumber)
            putExtra("AVATAR", avatar)
            putExtra("VOICE_TYPE", voiceType)
            putExtra("TALK_TIME", talkTime)
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRingtone()
    }
}