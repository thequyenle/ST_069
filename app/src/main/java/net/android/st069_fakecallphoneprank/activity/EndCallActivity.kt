package net.android.st069_fakecallphoneprank.activities

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.target.CustomTarget
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.databinding.ActivityEndCallBinding

class EndCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEndCallBinding

    private var name: String = ""
    private var avatar: String? = null
    private var callDuration: Int = 0

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

        // Get data from intent
        name = intent.getStringExtra("NAME") ?: "Unknown"
        avatar = intent.getStringExtra("AVATAR")
        callDuration = intent.getIntExtra("CALL_DURATION", 0)

        // DEBUG: Log received data
        Log.d("EndCallActivity", "Name: $name")
        Log.d("EndCallActivity", "Avatar: $avatar")
        Log.d("EndCallActivity", "Avatar is null or empty: ${avatar.isNullOrEmpty()}")

        // PRE-LOAD avatar and background bitmaps BEFORE setting content view
        preloadAvatarBitmaps()

        binding = ActivityEndCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupClickListeners()
    }

    private fun preloadAvatarBitmaps() {
        if (!avatar.isNullOrEmpty()) {
            val avatarFile = java.io.File(avatar!!)
            if (avatarFile.exists()) {
                Log.d("EndCallActivity", "Pre-loading avatar from: ${avatarFile.absolutePath}")
                try {
                    // Decode with smaller size for faster loading
                    val options = android.graphics.BitmapFactory.Options()
                    options.inSampleSize = 2 // Load at 50% size for speed
                    val originalBitmap = android.graphics.BitmapFactory.decodeFile(avatarFile.absolutePath, options)

                    if (originalBitmap != null) {
                        Log.d("EndCallActivity", "Bitmap loaded: ${originalBitmap.width}x${originalBitmap.height}")

                        // Create circular avatar
                        preloadedAvatarBitmap = createCircularBitmap(originalBitmap)

                        // Create blurred background
                        preloadedBackgroundBitmap = createBlurredBitmap(originalBitmap)

                        Log.d("EndCallActivity", "Avatar and background pre-loaded successfully!")
                    } else {
                        Log.e("EndCallActivity", "Failed to decode bitmap")
                    }
                } catch (e: Exception) {
                    Log.e("EndCallActivity", "Error pre-loading avatar: ${e.message}", e)
                }
            } else {
                Log.w("EndCallActivity", "Avatar file not found: $avatar")
            }
        }
    }

    private fun setupUI() {
        // Set caller name
        binding.tvCallerName.text = name

        // Use pre-loaded bitmaps (already loaded before layout inflation)
        if (preloadedAvatarBitmap != null && preloadedBackgroundBitmap != null) {
            binding.ivAvatar.setImageBitmap(preloadedAvatarBitmap)
            binding.ivBackgroundEndCall.setImageBitmap(preloadedBackgroundBitmap)
            Log.d("EndCallActivity", "Avatar and background set from pre-loaded bitmaps!")
        } else {
            // Fallback to default if pre-loading failed
            binding.ivAvatar.setImageResource(R.drawable.ic_addavatar)
            binding.ivBackgroundEndCall.setImageResource(R.drawable.bg_call_pixel5)
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

    // ========== BLUR BACKGROUND METHODS ==========

    private fun setBackgroundFromAvatarFile(avatarFile: java.io.File) {
        Log.d("EndCallActivity", "setBackgroundFromAvatarFile called with file: ${avatarFile.absolutePath}")
        try {
            Glide.with(this)
                .asBitmap()
                .load(avatarFile)
                .transform(CenterCrop())
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                        Log.d("EndCallActivity", "Bitmap loaded successfully, size: ${resource.width}x${resource.height}")
                        // Create a blurred version of the bitmap for background
                        val blurredBitmap = createBlurredBitmap(resource)
                        Log.d("EndCallActivity", "Blurred bitmap created, setting to background")
                        binding.ivBackgroundEndCall.setImageBitmap(blurredBitmap)
                        Log.d("EndCallActivity", "Background blur applied successfully!")
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        Log.w("EndCallActivity", "Glide load cleared, using default background")
                        // Set default background if loading is cleared
                        binding.ivBackgroundEndCall.setImageResource(R.drawable.bg_call_pixel5)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        Log.e("EndCallActivity", "Glide load failed, using default background")
                        // Set default background if loading fails
                        binding.ivBackgroundEndCall.setImageResource(R.drawable.bg_call_pixel5)
                    }
                })
        } catch (e: Exception) {
            Log.e("EndCallActivity", "Exception in setBackgroundFromAvatarFile: ${e.message}", e)
            // Set default background if any error occurs
            binding.ivBackgroundEndCall.setImageResource(R.drawable.bg_call_pixel5)
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