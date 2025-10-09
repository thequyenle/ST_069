package net.android.st069_fakecallphoneprank.activity

import android.animation.ObjectAnimator
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.activities.ActiveCallActivity
import net.android.st069_fakecallphoneprank.databinding.ActivityIncomingCallOppoBinding
import net.android.st069_fakecallphoneprank.databinding.ActivityIncomingCallPixel5Binding

class IncomingCallActivity : AppCompatActivity() {

    private var oppoBinding: ActivityIncomingCallOppoBinding? = null
    private var pixel5Binding: ActivityIncomingCallPixel5Binding? = null

    private var mediaPlayer: MediaPlayer? = null

    private var name: String = ""
    private var phoneNumber: String = ""
    private var avatar: String? = null
    private var deviceType: String? = null
    private var talkTime: Int = 15

    // Swipe gesture variables (for Oppo)
    private var initialX: Float = 0f
    private var initialTouchX: Float = 0f
    private var isSwipeInProgress = false
    private val swipeThreshold = 200f

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

        // Get data from intent
        name = intent.getStringExtra("NAME") ?: "Unknown"
        phoneNumber = intent.getStringExtra("PHONE_NUMBER") ?: ""
        avatar = intent.getStringExtra("AVATAR")
        deviceType = intent.getStringExtra("DEVICE_TYPE")
        talkTime = intent.getIntExtra("TALK_TIME", 15)

        // Inflate the appropriate layout based on device type
        inflateLayoutBasedOnDevice()

        setupUI()
        setupInteractions()
        startRingtone()
    }

    private fun inflateLayoutBasedOnDevice() {
        when (deviceType) {
            "iPhone 15 Pro" -> {
                // Use Oppo layout
                oppoBinding = ActivityIncomingCallOppoBinding.inflate(layoutInflater)
                setContentView(oppoBinding!!.root)
            }
            "iPhone 14" -> {
                // Use Pixel 5 layout
                pixel5Binding = ActivityIncomingCallPixel5Binding.inflate(layoutInflater)
                setContentView(pixel5Binding!!.root)
            }
            else -> {
                // Default to Pixel 5 layout
                pixel5Binding = ActivityIncomingCallPixel5Binding.inflate(layoutInflater)
                setContentView(pixel5Binding!!.root)
            }
        }
    }

    private fun setupUI() {
        when {
            oppoBinding != null -> {
                // Setup Oppo UI
                oppoBinding?.tvCallerName?.text = name

                val phoneText = if (phoneNumber.isNotEmpty()) {
                    "$phoneNumber Mobile"
                } else {
                    "0000000000 Mobile"
                }
                oppoBinding?.tvPhoneNumber?.text = phoneText
            }

            pixel5Binding != null -> {
                // Setup Pixel 5 UI
                pixel5Binding?.tvCallerName?.text = name
                pixel5Binding?.tvCallStatus?.text = "Call coming..."

                // Set avatar
                if (!avatar.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(Uri.parse(avatar))
                        .placeholder(R.drawable.ic_addavatar)
                        .circleCrop()
                        .into(pixel5Binding!!.ivAvatar)
                } else {
                    pixel5Binding?.ivAvatar?.setImageResource(R.drawable.ic_addavatar)
                }
            }
        }
    }

    private fun setupInteractions() {
        when {
            oppoBinding != null -> {
                // Setup Oppo swipe gesture
                setupOppoSwipeGesture()
                setupOppoButtons()
            }

            pixel5Binding != null -> {
                // Setup Pixel 5 button clicks
                setupPixel5Buttons()
            }
        }
    }

    // ========== OPPO SPECIFIC METHODS ==========

    private fun setupOppoSwipeGesture() {
        oppoBinding?.imgCallOppo?.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = view.x
                    initialTouchX = event.rawX
                    isSwipeInProgress = true
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isSwipeInProgress) {
                        val deltaX = event.rawX - initialTouchX
                        view.x = initialX + deltaX

                        // Visual feedback
                        when {
                            deltaX < -swipeThreshold / 2 || deltaX > swipeThreshold / 2 -> {
                                view.alpha = 0.7f
                            }
                            else -> {
                                view.alpha = 1.0f
                            }
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isSwipeInProgress) {
                        val deltaX = event.rawX - initialTouchX

                        when {
                            deltaX < -swipeThreshold -> {
                                // Swiped LEFT - Accept call
                                animateSwipeComplete(view, true) {
                                    acceptCall()
                                }
                            }
                            deltaX > swipeThreshold -> {
                                // Swiped RIGHT - Decline call
                                animateSwipeComplete(view, false) {
                                    declineCall()
                                }
                            }
                            else -> {
                                // Return to center
                                animateReturnToCenter(view)
                            }
                        }

                        isSwipeInProgress = false
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun setupOppoButtons() {
        oppoBinding?.layoutCallActions?.setOnClickListener {
            // Silent button - mute ringtone
            mediaPlayer?.setVolume(0f, 0f)
        }

        oppoBinding?.layoutMessage?.setOnClickListener {
            // Message button - decline call
            declineCall()
        }
    }

    private fun animateSwipeComplete(view: View, isLeft: Boolean, onComplete: () -> Unit) {
        val targetX = if (isLeft) {
            -view.width.toFloat()
        } else {
            resources.displayMetrics.widthPixels.toFloat()
        }

        ObjectAnimator.ofFloat(view, "x", view.x, targetX).apply {
            duration = 200
            start()
        }

        ObjectAnimator.ofFloat(view, "alpha", view.alpha, 0f).apply {
            duration = 200
            start()
        }

        view.postDelayed({
            onComplete()
        }, 200)
    }

    private fun animateReturnToCenter(view: View) {
        ObjectAnimator.ofFloat(view, "x", view.x, initialX).apply {
            duration = 300
            start()
        }

        ObjectAnimator.ofFloat(view, "alpha", view.alpha, 1f).apply {
            duration = 300
            start()
        }
    }

    // ========== PIXEL 5 SPECIFIC METHODS ==========

    private fun setupPixel5Buttons() {
        pixel5Binding?.btnAccept?.setOnClickListener {
            acceptCall()
        }

        pixel5Binding?.btnDecline?.setOnClickListener {
            declineCall()
        }

        pixel5Binding?.btnBack?.setOnClickListener {
            declineCall()
        }
    }

    // ========== COMMON METHODS ==========

    private fun startRingtone() {
        try {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@IncomingCallActivity, ringtoneUri)
                setAudioStreamType(AudioManager.STREAM_RING)
                isLooping = true
                prepare()
                start()
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

    private fun acceptCall() {
        stopRingtone()

        val intent = Intent(this, ActiveCallActivity::class.java).apply {
            putExtra("NAME", name)
            putExtra("PHONE_NUMBER", phoneNumber)
            putExtra("AVATAR", avatar)
            putExtra("TALK_TIME", talkTime)
        }
        startActivity(intent)
        finish()
    }

    private fun declineCall() {
        stopRingtone()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRingtone()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        declineCall()
    }
}