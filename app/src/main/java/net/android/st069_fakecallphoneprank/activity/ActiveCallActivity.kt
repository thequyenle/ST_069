package net.android.st069_fakecallphoneprank.activities

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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.target.CustomTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.databinding.ActivityActiveCallBinding
import net.android.st069_fakecallphoneprank.viewmodel.FakeCallViewModel

class ActiveCallActivity : AppCompatActivity() {

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
    private var preloadedBackgroundBitmap: Bitmap? = null

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
        Log.d("ActiveCallActivity", "Avatar is null or empty: ${avatar.isNullOrEmpty()}")

        // PRE-LOAD avatar and background bitmaps BEFORE setting content view
        preloadAvatarBitmaps()

        setupUI()
        setupClickListeners()
        startCallTimer()
        playVoice()
    }

    private fun preloadAvatarBitmaps() {
        if (!avatar.isNullOrEmpty()) {
            val avatarFile = java.io.File(avatar!!)
            if (avatarFile.exists()) {
                Log.d("ActiveCallActivity", "Pre-loading avatar from: ${avatarFile.absolutePath}")
                try {
                    // Decode with smaller size for faster loading
                    val options = android.graphics.BitmapFactory.Options()
                    options.inSampleSize = 2 // Load at 50% size for speed
                    val originalBitmap = android.graphics.BitmapFactory.decodeFile(avatarFile.absolutePath, options)

                    if (originalBitmap != null) {
                        Log.d("ActiveCallActivity", "Bitmap loaded: ${originalBitmap.width}x${originalBitmap.height}")

                        // Create circular avatar
                        preloadedAvatarBitmap = createCircularBitmap(originalBitmap)

                        // Create blurred background
                        preloadedBackgroundBitmap = createBlurredBitmap(originalBitmap)

                        Log.d("ActiveCallActivity", "Avatar and background pre-loaded successfully!")
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

        // Use pre-loaded bitmaps (already loaded before layout inflation)
        if (preloadedAvatarBitmap != null && preloadedBackgroundBitmap != null) {
            binding.ivAvatar.setImageBitmap(preloadedAvatarBitmap)
            binding.ivBackgroundActiveCall.setImageBitmap(preloadedBackgroundBitmap)
            Log.d("ActiveCallActivity", "Avatar and background set from pre-loaded bitmaps!")
        } else {
            // Fallback to default if pre-loading failed
            binding.ivAvatar.setImageResource(R.drawable.ic_addavatar)
            binding.ivBackgroundActiveCall.setImageResource(R.drawable.bg_call_pixel5)
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
            toggleMute()
        }

        binding.btnSpeaker.setOnClickListener {
            toggleSpeaker()
        }
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

    // ========== BLUR BACKGROUND METHODS ==========

    private fun setBackgroundFromAvatarFile(avatarFile: java.io.File) {
        Log.d("ActiveCallActivity", "setBackgroundFromAvatarFile called with file: ${avatarFile.absolutePath}")
        try {
            Glide.with(this)
                .asBitmap()
                .load(avatarFile)
                .transform(CenterCrop())
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                        Log.d("ActiveCallActivity", "Bitmap loaded successfully, size: ${resource.width}x${resource.height}")
                        // Create a blurred version of the bitmap for background
                        val blurredBitmap = createBlurredBitmap(resource)
                        Log.d("ActiveCallActivity", "Blurred bitmap created, setting to background")
                        binding.ivBackgroundActiveCall.setImageBitmap(blurredBitmap)
                        Log.d("ActiveCallActivity", "Background blur applied successfully!")
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        Log.w("ActiveCallActivity", "Glide load cleared, using default background")
                        // Set default background if loading is cleared
                        binding.ivBackgroundActiveCall.setImageResource(R.drawable.bg_call_pixel5)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        Log.e("ActiveCallActivity", "Glide load failed, using default background")
                        // Set default background if loading fails
                        binding.ivBackgroundActiveCall.setImageResource(R.drawable.bg_call_pixel5)
                    }
                })
        } catch (e: Exception) {
            Log.e("ActiveCallActivity", "Exception in setBackgroundFromAvatarFile: ${e.message}", e)
            // Set default background if any error occurs
            binding.ivBackgroundActiveCall.setImageResource(R.drawable.bg_call_pixel5)
        }
    }

    private fun createBlurredBitmap(originalBitmap: Bitmap): Bitmap {
        // Scale down MORE for better performance (10% size = 100x faster)
        val scaleFactor = 0.1f
        val width = (originalBitmap.width * scaleFactor).toInt().coerceAtLeast(50)
        val height = (originalBitmap.height * scaleFactor).toInt().coerceAtLeast(50)

        // Create scaled bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)

        // Apply simple blur with smaller radius for speed
        val blurredBitmap = fastBlurSimple(scaledBitmap, 15)

        // Apply darkening overlay
        val canvas = Canvas(blurredBitmap)
        val paint = Paint()
        paint.colorFilter = PorterDuffColorFilter(Color.argb(80, 0, 0, 0), PorterDuff.Mode.SRC_ATOP)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Scale back up to original size for proper display
        val finalBitmap = Bitmap.createScaledBitmap(blurredBitmap, originalBitmap.width, originalBitmap.height, true)

        return finalBitmap
    }

    private fun fastBlurSimple(bitmap: Bitmap, radius: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Simple box blur - much safer than stack blur
        for (pass in 0 until 2) {
            // Horizontal pass
            for (y in 0 until height) {
                for (x in 0 until width) {
                    var r = 0
                    var g = 0
                    var b = 0
                    var count = 0

                    for (dx in -radius..radius) {
                        val nx = x + dx
                        if (nx >= 0 && nx < width) {
                            val pixel = pixels[y * width + nx]
                            r += (pixel shr 16) and 0xFF
                            g += (pixel shr 8) and 0xFF
                            b += pixel and 0xFF
                            count++
                        }
                    }

                    if (count > 0) {
                        pixels[y * width + x] = (0xFF shl 24) or ((r / count) shl 16) or ((g / count) shl 8) or (b / count)
                    }
                }
            }

            // Vertical pass
            for (x in 0 until width) {
                for (y in 0 until height) {
                    var r = 0
                    var g = 0
                    var b = 0
                    var count = 0

                    for (dy in -radius..radius) {
                        val ny = y + dy
                        if (ny >= 0 && ny < height) {
                            val pixel = pixels[ny * width + x]
                            r += (pixel shr 16) and 0xFF
                            g += (pixel shr 8) and 0xFF
                            b += pixel and 0xFF
                            count++
                        }
                    }

                    if (count > 0) {
                        pixels[y * width + x] = (0xFF shl 24) or ((r / count) shl 16) or ((g / count) shl 8) or (b / count)
                    }
                }
            }
        }

        val blurredBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        blurredBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return blurredBitmap
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
}