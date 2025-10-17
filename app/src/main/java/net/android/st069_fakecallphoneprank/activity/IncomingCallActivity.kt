package net.android.st069_fakecallphoneprank.activity

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.activities.ActiveCallActivity
import net.android.st069_fakecallphoneprank.databinding.ActivityIncomingCallOppoBinding
import net.android.st069_fakecallphoneprank.databinding.ActivityIncomingCallPixel5Binding
import net.android.st069_fakecallphoneprank.utils.FullscreenHelper

class IncomingCallActivity : AppCompatActivity() {

    private var oppoBinding: ActivityIncomingCallOppoBinding? = null
    private var pixel5Binding: ActivityIncomingCallPixel5Binding? = null

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null

    private lateinit var sharedPreferences: SharedPreferences
    private val handler = Handler(Looper.getMainLooper())
    private var flashRunnable: Runnable? = null
    private var vibrationRunnable: Runnable? = null
    private var autoEndRunnable: Runnable? = null

    private var fakeCallId: Long = -1
    private var name: String = ""
    private var phoneNumber: String = ""
    private var avatar: String? = null
    private var voiceType: String? = null
    private var deviceType: String? = null
    private var talkTime: Int = 15

    // Pre-loaded bitmaps (loaded before layout inflation)
    private var preloadedAvatarBitmap: Bitmap? = null
    private var preloadedBackgroundBitmap: Bitmap? = null

    // Swipe gesture variables (for Oppo)
    private var initialX: Float = 0f
    private var initialTouchX: Float = 0f
    private var isSwipeInProgress = false
    private val swipeThreshold = 200f

    // Settings values
    private var isVibrationEnabled = true
    private var isSoundEnabled = true
    private var isFlashEnabled = false
    private var ringTime = 15

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SharedPreferences and load settings
        sharedPreferences = getSharedPreferences("FakeCallSettings", Context.MODE_PRIVATE)
        loadSettings()

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
        fakeCallId = intent.getLongExtra("FAKE_CALL_ID", -1)
        name = intent.getStringExtra("NAME") ?: "Unknown"
        phoneNumber = intent.getStringExtra("PHONE_NUMBER") ?: ""
        avatar = intent.getStringExtra("AVATAR")
        voiceType = intent.getStringExtra("VOICE_TYPE")
        deviceType = intent.getStringExtra("DEVICE_TYPE")
        talkTime = intent.getIntExtra("TALK_TIME", 15)

        // DEBUG: Log the device type
        Log.d("IncomingCallActivity", "Device Type Received: '$deviceType'")
        Log.d("IncomingCallActivity", "Name: $name")
        Log.d("IncomingCallActivity", "Phone: $phoneNumber")
        Log.d("IncomingCallActivity", "Avatar: $avatar")
        Log.d("IncomingCallActivity", "Voice Type: $voiceType")
        Log.d("IncomingCallActivity", "Settings - Ring Time: ${ringTime}s, Sound: $isSoundEnabled, Vibration: $isVibrationEnabled, Flash: $isFlashEnabled")

        // PRE-LOAD avatar and background bitmaps BEFORE inflating layout
        preloadAvatarBitmaps()

        // Inflate the appropriate layout based on device type
        inflateLayoutBasedOnDevice()

        // Enable fullscreen edge-to-edge (after setContentView)
        FullscreenHelper.enableFullscreen(this)

        setupUI()
        setupInteractions()
        startRingtone()
        startVibration()
        startFlash()
        scheduleAutoEnd()
    }

    private fun loadSettings() {
        ringTime = sharedPreferences.getInt("ring_time", 15)
        isVibrationEnabled = sharedPreferences.getBoolean("vibration_enabled", true)
        isSoundEnabled = sharedPreferences.getBoolean("sound_enabled", true)
        isFlashEnabled = sharedPreferences.getBoolean("flash_enabled", false)
    }

    private fun inflateLayoutBasedOnDevice() {
        Log.d("IncomingCallActivity", "Checking device type: '$deviceType'")

        when (deviceType) {
            "Oppo" -> {
                Log.d("IncomingCallActivity", "Loading OPPO layout")
                oppoBinding = ActivityIncomingCallOppoBinding.inflate(layoutInflater)
                setContentView(oppoBinding!!.root)
            }
            else -> {
                // Default to Pixel 5 layout for all cases except explicit "Oppo"
                Log.d("IncomingCallActivity", "Loading PIXEL 5 layout as default. Device type was: '$deviceType'")
                pixel5Binding = ActivityIncomingCallPixel5Binding.inflate(layoutInflater)
                setContentView(pixel5Binding!!.root)
            }
        }
    }

    private fun preloadAvatarBitmaps() {
        Log.d("IncomingCallActivity", "preloadAvatarBitmaps called. Avatar path: '$avatar'")

        if (!avatar.isNullOrEmpty()) {
            val avatarFile = java.io.File(avatar!!)
            Log.d("IncomingCallActivity", "Avatar file path: ${avatarFile.absolutePath}")
            Log.d("IncomingCallActivity", "Avatar file exists: ${avatarFile.exists()}")

            if (avatarFile.exists()) {
                Log.d("IncomingCallActivity", "Pre-loading avatar from: ${avatarFile.absolutePath}")
                try {
                    // Decode with smaller size for faster loading
                    val options = android.graphics.BitmapFactory.Options()
                    options.inSampleSize = 2 // Load at 50% size for speed
                    val originalBitmap = android.graphics.BitmapFactory.decodeFile(avatarFile.absolutePath, options)

                    if (originalBitmap != null) {
                        Log.d("IncomingCallActivity", "Bitmap loaded: ${originalBitmap.width}x${originalBitmap.height}")

                        // Create circular avatar
                        preloadedAvatarBitmap = createCircularBitmap(originalBitmap)

                        // Create blurred background
                        preloadedBackgroundBitmap = createBlurredBitmap(originalBitmap)

                        Log.d("IncomingCallActivity", "Avatar and background pre-loaded successfully!")
                    } else {
                        Log.e("IncomingCallActivity", "Failed to decode bitmap from file: ${avatarFile.absolutePath}")
                    }
                } catch (e: Exception) {
                    Log.e("IncomingCallActivity", "Error pre-loading avatar: ${e.message}", e)
                }
            } else {
                Log.w("IncomingCallActivity", "Avatar file not found at path: $avatar")
            }
        } else {
            Log.w("IncomingCallActivity", "Avatar path is null or empty!")
        }
    }


    private fun setupUI() {
        when {
            oppoBinding != null -> {
                Log.d("IncomingCallActivity", "Setting up Oppo UI")
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
                Log.d("IncomingCallActivity", "Setting up Pixel 5 UI")
                // Setup Pixel 5 UI
                pixel5Binding?.tvCallerName?.text = name
                pixel5Binding?.tvCallStatus?.text = "Call coming..."

                // Set avatar and background
                if (!avatar.isNullOrEmpty()) {
                    // Check if avatar is URL or local file
                    if (avatar!!.startsWith("http")) {
                        // Load from URL using Glide
                        Log.d("IncomingCallActivity", "Loading avatar from URL: $avatar")
                        Glide.with(this)
                            .load(avatar)
                            .placeholder(R.drawable.ic_addavatar)
                            .error(R.drawable.ic_addavatar)
                            .circleCrop()
                            .into(pixel5Binding!!.ivAvatar)

                        // Load background from URL
                        Glide.with(this)
                            .load(avatar)
                            .placeholder(R.drawable.bg_call_pixel5)
                            .error(R.drawable.bg_call_pixel5)
                            .into(object : CustomTarget<Drawable>() {
                                override fun onResourceReady(resource: Drawable, transition: com.bumptech.glide.request.transition.Transition<in Drawable>?) {
                                    pixel5Binding?.ivBackground?.setImageDrawable(resource)
                                    pixel5Binding?.ivBackground?.setColorFilter(Color.parseColor("#B3000000"), PorterDuff.Mode.SRC_ATOP)
                                    Log.d("IncomingCallActivity", "Background set from URL with #B3000000 tint!")
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                    pixel5Binding?.ivBackground?.setImageResource(R.drawable.bg_call_pixel5)
                                }
                            })
                    } else {
                        // Load from local file
                        if (preloadedAvatarBitmap != null) {
                            pixel5Binding?.ivAvatar?.setImageBitmap(preloadedAvatarBitmap)
                            Log.d("IncomingCallActivity", "Avatar set from pre-loaded bitmap!")
                        } else {
                            pixel5Binding?.ivAvatar?.setImageResource(R.drawable.ic_addavatar)
                        }

                        // Set background from local file
                        val avatarFile = java.io.File(avatar!!)
                        if (avatarFile.exists()) {
                            val originalBitmap = android.graphics.BitmapFactory.decodeFile(avatarFile.absolutePath)
                            pixel5Binding?.ivBackground?.setImageBitmap(originalBitmap)
                            pixel5Binding?.ivBackground?.setColorFilter(Color.parseColor("#B3000000"), PorterDuff.Mode.SRC_ATOP)
                            Log.d("IncomingCallActivity", "Background set from local file with #B3000000 tint!")
                        } else {
                            pixel5Binding?.ivBackground?.setImageResource(R.drawable.bg_call_pixel5)
                        }
                    }
                } else {
                    // No avatar - use defaults
                    pixel5Binding?.ivAvatar?.setImageResource(R.drawable.ic_addavatar)
                    pixel5Binding?.ivBackground?.setImageResource(R.drawable.bg_call_pixel5)
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

    // ========== BITMAP PROCESSING METHODS ==========

    private fun createBlurredBitmap(originalBitmap: Bitmap): Bitmap {
        // Scale down for better performance (15% size for better quality)
        val scaleFactor = 0.15f
        val width = (originalBitmap.width * scaleFactor).toInt().coerceAtLeast(100)
        val height = (originalBitmap.height * scaleFactor).toInt().coerceAtLeast(100)

        // Create scaled bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)

        // Apply blur
        val blurredBitmap = fastBlurSimple(scaledBitmap, 25)

        // Scale back up to full screen size FIRST
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val scaledUpBitmap = Bitmap.createScaledBitmap(blurredBitmap, screenWidth, screenHeight, true)

        // Create a new bitmap for the darkened version
        val finalBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(finalBitmap)

        // Draw the blurred image first
        canvas.drawBitmap(scaledUpBitmap, 0f, 0f, null)

        // Draw semi-transparent black overlay (10% dark for visibility)
        val paint = Paint()
        paint.color = Color.argb(25, 0, 0, 0)  // 25/255 = ~10% dark
        canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), paint)

        Log.d("IncomingCallActivity", "Blurred background created: ${finalBitmap.width}x${finalBitmap.height} with 10% darkness overlay")

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
        if (!isSoundEnabled) {
            Log.d("IncomingCallActivity", "Sound is disabled in settings")
            return
        }

        try {
            // Get custom ringtone from settings or use default
            val ringtoneUriString = sharedPreferences.getString("ringtone_uri", null)
            val ringtoneUri = if (ringtoneUriString != null) {
                Uri.parse(ringtoneUriString)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }

            Log.d("IncomingCallActivity", "Starting ringtone: $ringtoneUri")

            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@IncomingCallActivity, ringtoneUri)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setAudioAttributes(
                        android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                } else {
                    @Suppress("DEPRECATION")
                    setAudioStreamType(AudioManager.STREAM_RING)
                }
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("IncomingCallActivity", "Error starting ringtone", e)
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
        stopVibration()
        stopFlash()
        cancelAutoEnd()

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

    private fun declineCall() {
        stopRingtone()
        stopVibration()
        stopFlash()
        cancelAutoEnd()
        finish()
    }

    private fun startVibration() {
        if (!isVibrationEnabled) {
            Log.d("IncomingCallActivity", "Vibration is disabled in settings")
            return
        }

        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ (API 31+)
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                // Android 9-11 (API 28-30)
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            // Vibrate in pattern: wait 1000ms, vibrate 1000ms, repeat
            val pattern = longArrayOf(0, 1000, 1000)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8+ (API 26+)
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }

            Log.d("IncomingCallActivity", "Vibration started")
        } catch (e: Exception) {
            Log.e("IncomingCallActivity", "Error starting vibration", e)
            e.printStackTrace()
        }
    }

    private fun stopVibration() {
        try {
            vibrator?.cancel()
            vibrator = null
            Log.d("IncomingCallActivity", "Vibration stopped")
        } catch (e: Exception) {
            Log.e("IncomingCallActivity", "Error stopping vibration", e)
            e.printStackTrace()
        }
    }

    private fun startFlash() {
        if (!isFlashEnabled) {
            Log.d("IncomingCallActivity", "Flash is disabled in settings")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
                cameraId = cameraManager?.cameraIdList?.get(0)

                if (cameraId == null) {
                    Log.w("IncomingCallActivity", "No camera found")
                    return
                }

                // Check if camera has flash unit
                val characteristics = cameraManager?.getCameraCharacteristics(cameraId!!)
                val hasFlash = characteristics?.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

                if (!hasFlash) {
                    Log.w("IncomingCallActivity", "Camera does not have a flash unit")
                    return
                }

                // Flash blinking pattern
                flashRunnable = object : Runnable {
                    var isOn = false
                    override fun run() {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                cameraManager?.setTorchMode(cameraId!!, isOn)
                                isOn = !isOn
                                handler.postDelayed(this, 500) // Blink every 500ms
                            }
                        } catch (e: CameraAccessException) {
                            Log.e("IncomingCallActivity", "Error toggling flash", e)
                        } catch (e: IllegalArgumentException) {
                            Log.e("IncomingCallActivity", "Flash not available", e)
                        }
                    }
                }
                handler.post(flashRunnable!!)

                Log.d("IncomingCallActivity", "Flash started")
            } catch (e: Exception) {
                Log.e("IncomingCallActivity", "Error starting flash", e)
                e.printStackTrace()
            }
        }
    }

    private fun stopFlash() {
        try {
            flashRunnable?.let {
                handler.removeCallbacks(it)
                flashRunnable = null
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && cameraId != null) {
                cameraManager?.setTorchMode(cameraId!!, false)
            }

            Log.d("IncomingCallActivity", "Flash stopped")
        } catch (e: Exception) {
            Log.e("IncomingCallActivity", "Error stopping flash", e)
            e.printStackTrace()
        }
    }

    private fun scheduleAutoEnd() {
        // Auto-end the call after ringTime seconds
        val delayMillis = ringTime * 1000L
        autoEndRunnable = Runnable {
            Log.d("IncomingCallActivity", "Auto-ending call after ${ringTime}s")
            declineCall()
        }
        handler.postDelayed(autoEndRunnable!!, delayMillis)
        Log.d("IncomingCallActivity", "Auto-end scheduled for ${ringTime}s")
    }

    private fun cancelAutoEnd() {
        autoEndRunnable?.let {
            handler.removeCallbacks(it)
            autoEndRunnable = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRingtone()
        stopVibration()
        stopFlash()
        cancelAutoEnd()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        declineCall()
    }
}