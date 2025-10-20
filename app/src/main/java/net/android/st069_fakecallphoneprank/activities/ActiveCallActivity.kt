package net.android.st069_fakecallphoneprank.activities

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.target.CustomTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.base.BaseActivity
import net.android.st069_fakecallphoneprank.databinding.ActivityActiveCallBinding
import net.android.st069_fakecallphoneprank.utils.LocaleHelper
import net.android.st069_fakecallphoneprank.viewmodel.FakeCallViewModel

class ActiveCallActivity : BaseActivity() {

    private lateinit var binding: ActivityActiveCallBinding
    private var callTimer: CountDownTimer? = null
    private var voicePlayer: MediaPlayer? = null
    private lateinit var viewModel: FakeCallViewModel

    private var fakeCallId: Long = -1
    private var name: String = ""
    private var avatar: String? = null
    private var voiceType: String? = null
    private var talkTime: Int = 15

    private var elapsedSeconds: Int = 0

    // Pre-loaded bitmaps (loaded before layout inflation)
    private var preloadedAvatarBitmap: Bitmap? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase))
    }

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

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[FakeCallViewModel::class.java]

        // Get data from intent
        fakeCallId = intent.getLongExtra("FAKE_CALL_ID", -1)
        name = intent.getStringExtra("NAME") ?: "Unknown"
        avatar = intent.getStringExtra("AVATAR")
        voiceType = intent.getStringExtra("VOICE_TYPE")
        talkTime = intent.getIntExtra("TALK_TIME", 15)

        // DEBUG: Log received data
        Log.d("ActiveCallActivity", "FAKE_CALL_ID: $fakeCallId")
        Log.d("ActiveCallActivity", "Name: $name")
        Log.d("ActiveCallActivity", "Avatar: $avatar")
        Log.d("ActiveCallActivity", "Voice Type: $voiceType")

        // PRE-LOAD avatar bitmap
        preloadAvatarBitmap()

        setupUI()
        setupClickListeners()
        startCallTimer()
        playVoice()
    }

    private fun preloadAvatarBitmap() {
        if (!avatar.isNullOrEmpty()) {
            val avatarFile = java.io.File(avatar!!)
            if (avatarFile.exists()) {
                Log.d("ActiveCallActivity", "Pre-loading avatar from: ${avatarFile.absolutePath}")
                try {
                    val options = android.graphics.BitmapFactory.Options()
                    options.inSampleSize = 2
                    val originalBitmap = android.graphics.BitmapFactory.decodeFile(avatarFile.absolutePath, options)

                    if (originalBitmap != null) {
                        Log.d("ActiveCallActivity", "Bitmap loaded: ${originalBitmap.width}x${originalBitmap.height}")
                        preloadedAvatarBitmap = createCircularBitmap(originalBitmap)
                        Log.d("ActiveCallActivity", "Avatar pre-loaded successfully!")
                    } else {
                        Log.e("ActiveCallActivity", "Failed to decode bitmap")
                    }
                } catch (e: Exception) {
                    Log.e("ActiveCallActivity", "Error pre-loading avatar: ${e.message}", e)
                }
            } else {
                Log.w("ActiveCallActivity", "Avatar file not found: $avatar")
            }
        }
    }

    private fun setupUI() {
        // Set caller name
        binding.tvCallerName.text = name

        // Set avatar and background
        if (!avatar.isNullOrEmpty()) {
            // Check if avatar is URL or local file
            if (avatar!!.startsWith("http")) {
                // Load from URL using Glide
                Log.d("ActiveCallActivity", "Loading avatar from URL: $avatar")
                com.bumptech.glide.Glide.with(this)
                    .load(avatar)
                    .placeholder(R.drawable.ic_addavatar)
                    .error(R.drawable.ic_addavatar)
                    .circleCrop()
                    .into(binding.ivAvatar)

                // Load background from URL
                com.bumptech.glide.Glide.with(this)
                    .load(avatar)
                    .placeholder(R.drawable.bg_call_pixel5)
                    .error(R.drawable.bg_call_pixel5)
                    .into(object : com.bumptech.glide.request.target.CustomTarget<android.graphics.drawable.Drawable>() {
                        override fun onResourceReady(resource: android.graphics.drawable.Drawable, transition: com.bumptech.glide.request.transition.Transition<in android.graphics.drawable.Drawable>?) {
                            binding.ivBackgroundActiveCall.setImageDrawable(resource)
                            binding.ivBackgroundActiveCall.setColorFilter(Color.parseColor("#B3000000"), PorterDuff.Mode.SRC_ATOP)
                            Log.d("ActiveCallActivity", "Background set from URL with #E3000000 tint!")
                        }

                        override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                            binding.ivBackgroundActiveCall.setImageResource(R.drawable.bg_call_pixel5)
                        }
                    })
            } else {
                // Load from local file
                if (preloadedAvatarBitmap != null) {
                    binding.ivAvatar.setImageBitmap(preloadedAvatarBitmap)
                    Log.d("ActiveCallActivity", "Avatar set from pre-loaded bitmap!")
                } else {
                    binding.ivAvatar.setImageResource(R.drawable.ic_addavatar)
                }

                // Set background from local file
                val avatarFile = java.io.File(avatar!!)
                if (avatarFile.exists()) {
                    val originalBitmap = android.graphics.BitmapFactory.decodeFile(avatarFile.absolutePath)
                    binding.ivBackgroundActiveCall.setImageBitmap(originalBitmap)
                    binding.ivBackgroundActiveCall.setColorFilter(Color.parseColor("#B3000000"), PorterDuff.Mode.SRC_ATOP)
                    Log.d("ActiveCallActivity", "Background set from local file with #E3000000 tint!")
                } else {
                    binding.ivBackgroundActiveCall.setImageResource(R.drawable.bg_call_pixel5)
                    binding.ivBackgroundActiveCall.setColorFilter(Color.parseColor("#B3000000"), PorterDuff.Mode.SRC_ATOP)
                }
            }
        } else {
            // No avatar - use defaults
            binding.ivAvatar.setImageResource(R.drawable.ic_addavatar)
            binding.ivBackgroundActiveCall.setImageResource(R.drawable.bg_call_pixel5)
            binding.ivBackgroundActiveCall.setColorFilter(Color.parseColor("#B3000000"), PorterDuff.Mode.SRC_ATOP)
        }

        // Initialize timer display
        binding.tvTimer.text = "00:00"
    }

    private fun setupClickListeners() {
        // Back button - end call
        binding.btnBack.setOnClickListener {
            endCall()
        }

        // End call button
        binding.btnEndCall.setOnClickListener {
            endCall()
        }

        // All other buttons are disabled (no click listeners)
        // btnMute, btnSpeaker, btnKeypad, btnAddCall, btnCamera, btnContact
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
        if (!voiceType.isNullOrEmpty()) {
            try {
                Log.d("ActiveCallActivity", "Playing voice from: $voiceType")
                voicePlayer = MediaPlayer().apply {
                    // Check if URL or local file
                    if (voiceType!!.startsWith("http")) {
                        // Load from URL
                        setDataSource(voiceType)
                    } else {
                        // Load from local file
                        setDataSource(voiceType)
                    }
                    setAudioStreamType(android.media.AudioManager.STREAM_MUSIC)
                    prepareAsync() // Use async prepare for URLs
                    setOnPreparedListener {
                        Log.d("ActiveCallActivity", "Voice prepared, starting playback")
                        start()
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.e("ActiveCallActivity", "MediaPlayer error: what=$what, extra=$extra")
                        true
                    }
                }
            } catch (e: Exception) {
                Log.e("ActiveCallActivity", "Error playing voice: ${e.message}", e)
                e.printStackTrace()
            }
        } else {
            Log.d("ActiveCallActivity", "No voice type provided")
        }
    }

    private fun endCall() {
        callTimer?.cancel()
        stopVoice()

        // Deactivate the call (don't delete)
        deactivateCall()

        // Go to end call screen
        val intent = Intent(this, EndCallActivity::class.java).apply {
            putExtra("NAME", name)
            putExtra("AVATAR", avatar)
            putExtra("CALL_DURATION", elapsedSeconds)
        }
        startActivity(intent)
        finish()
    }

    private fun deactivateCall() {
        if (fakeCallId != -1L) {
            CoroutineScope(Dispatchers.IO).launch {
                viewModel.deactivateFakeCall(fakeCallId)
            }
        }
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

    private fun createCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = Math.min(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint()
        paint.isAntiAlias = true
        paint.shader = android.graphics.BitmapShader(bitmap, android.graphics.Shader.TileMode.CLAMP, android.graphics.Shader.TileMode.CLAMP)

        val radius = size / 2f
        canvas.drawCircle(radius, radius, radius, paint)

        return output
    }

    /**
     * Override to set transparent status bar for call screen
     */
    override fun setStatusBarColor() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor = android.graphics.Color.TRANSPARENT
            }
        } catch (e: Exception) {
            android.util.Log.w("ActiveCallActivity", "Could not set status bar color: ${e.message}")
        }
    }

    /**
     * Override to set dark status bar appearance (light icons) for call screen
     */
    override fun setStatusBarAppearance() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                // Android 11+ (API 30+) - Clear light status bar flag to show light icons
                window.insetsController?.setSystemBarsAppearance(
                    0,
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
        } catch (e: Exception) {
            android.util.Log.w("ActiveCallActivity", "Could not set status bar appearance: ${e.message}")
        }
    }

    /**
     * Override to return 0 for legacy API (no light status bar flag = light icons)
     */
    @Suppress("DEPRECATION")
    override fun getStatusBarAppearanceFlag(): Int {
        return 0  // No flag = dark status bar with light icons
    }
}
