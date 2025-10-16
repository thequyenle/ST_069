package net.android.st069_fakecallphoneprank.activity

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
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

class IncomingCallActivity : AppCompatActivity() {

    private var oppoBinding: ActivityIncomingCallOppoBinding? = null
    private var pixel5Binding: ActivityIncomingCallPixel5Binding? = null

    private var mediaPlayer: MediaPlayer? = null

    private var fakeCallId: Long = -1
    private var name: String = ""
    private var phoneNumber: String = ""
    private var avatar: String? = null
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
        fakeCallId = intent.getLongExtra("FAKE_CALL_ID", -1)
        name = intent.getStringExtra("NAME") ?: "Unknown"
        phoneNumber = intent.getStringExtra("PHONE_NUMBER") ?: ""
        avatar = intent.getStringExtra("AVATAR")
        deviceType = intent.getStringExtra("DEVICE_TYPE")
        talkTime = intent.getIntExtra("TALK_TIME", 15)

        // DEBUG: Log the device type
        Log.d("IncomingCallActivity", "Device Type Received: '$deviceType'")
        Log.d("IncomingCallActivity", "Name: $name")
        Log.d("IncomingCallActivity", "Phone: $phoneNumber")
        Log.d("IncomingCallActivity", "Avatar: $avatar")

        // PRE-LOAD avatar and background bitmaps BEFORE inflating layout
        preloadAvatarBitmaps()

        // Inflate the appropriate layout based on device type
        inflateLayoutBasedOnDevice()

        setupUI()
        setupInteractions()
        startRingtone()
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
        if (!avatar.isNullOrEmpty()) {
            val avatarFile = java.io.File(avatar!!)
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
                        Log.e("IncomingCallActivity", "Failed to decode bitmap")
                    }
                } catch (e: Exception) {
                    Log.e("IncomingCallActivity", "Error pre-loading avatar: ${e.message}", e)
                }
            } else {
                Log.w("IncomingCallActivity", "Avatar file not found: $avatar")
            }
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

                // Use pre-loaded bitmaps (already loaded before layout inflation)
                if (preloadedAvatarBitmap != null && preloadedBackgroundBitmap != null) {
                    pixel5Binding?.ivAvatar?.setImageBitmap(preloadedAvatarBitmap)
                    pixel5Binding?.ivBackground?.setImageBitmap(preloadedBackgroundBitmap)
                    Log.d("IncomingCallActivity", "Avatar and background set from pre-loaded bitmaps!")
                } else {
                    // Fallback to default if pre-loading failed
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
            putExtra("FAKE_CALL_ID", fakeCallId)
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