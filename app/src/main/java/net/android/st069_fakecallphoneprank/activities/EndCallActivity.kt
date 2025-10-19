package net.android.st069_fakecallphoneprank.activities

import android.content.Context
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.target.CustomTarget
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.base.BaseActivity
import net.android.st069_fakecallphoneprank.databinding.ActivityEndCallBinding
import net.android.st069_fakecallphoneprank.utils.LocaleHelper

class EndCallActivity : BaseActivity() {

    private lateinit var binding: ActivityEndCallBinding

    private var name: String = ""
    private var avatar: String? = null
    private var callDuration: Int = 0

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

        // Get data from intent
        name = intent.getStringExtra("NAME") ?: "Unknown"
        avatar = intent.getStringExtra("AVATAR")
        callDuration = intent.getIntExtra("CALL_DURATION", 0)

        // DEBUG: Log received data
        Log.d("EndCallActivity", "Name: $name")
        Log.d("EndCallActivity", "Avatar: $avatar")

        // PRE-LOAD avatar bitmap
        preloadAvatarBitmap()

        binding = ActivityEndCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupClickListeners()
    }

    private fun preloadAvatarBitmap() {
        if (!avatar.isNullOrEmpty()) {
            val avatarFile = java.io.File(avatar!!)
            if (avatarFile.exists()) {
                Log.d("EndCallActivity", "Pre-loading avatar from: ${avatarFile.absolutePath}")
                try {
                    val options = android.graphics.BitmapFactory.Options()
                    options.inSampleSize = 2
                    val originalBitmap = android.graphics.BitmapFactory.decodeFile(avatarFile.absolutePath, options)

                    if (originalBitmap != null) {
                        Log.d("EndCallActivity", "Bitmap loaded: ${originalBitmap.width}x${originalBitmap.height}")
                        preloadedAvatarBitmap = createCircularBitmap(originalBitmap)
                        Log.d("EndCallActivity", "Avatar pre-loaded successfully!")
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

        // Set avatar and background
        if (!avatar.isNullOrEmpty()) {
            // Check if avatar is URL or local file
            if (avatar!!.startsWith("http")) {
                // Load from URL using Glide
                Log.d("EndCallActivity", "Loading avatar from URL: $avatar")
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
                            binding.ivBackgroundEndCall.setImageDrawable(resource)
                            binding.ivBackgroundEndCall.setColorFilter(Color.parseColor("#B3000000"), PorterDuff.Mode.SRC_ATOP)
                            Log.d("EndCallActivity", "Background set from URL with #B3000000 tint!")
                        }

                        override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                            binding.ivBackgroundEndCall.setImageResource(R.drawable.bg_call_pixel5)
                        }
                    })
            } else {
                // Load from local file
                if (preloadedAvatarBitmap != null) {
                    binding.ivAvatar.setImageBitmap(preloadedAvatarBitmap)
                    Log.d("EndCallActivity", "Avatar set from pre-loaded bitmap!")
                } else {
                    binding.ivAvatar.setImageResource(R.drawable.ic_addavatar)
                }

                // Set background from local file
                val avatarFile = java.io.File(avatar!!)
                if (avatarFile.exists()) {
                    val originalBitmap = android.graphics.BitmapFactory.decodeFile(avatarFile.absolutePath)
                    binding.ivBackgroundEndCall.setImageBitmap(originalBitmap)
                    binding.ivBackgroundEndCall.setColorFilter(Color.parseColor("#B3000000"), PorterDuff.Mode.SRC_ATOP)
                    Log.d("EndCallActivity", "Background set from local file with #B3000000 tint!")
                } else {
                    binding.ivBackgroundEndCall.setImageResource(R.drawable.bg_call_pixel5)
                }
            }
        } else {
            // No avatar - use defaults
            binding.ivAvatar.setImageResource(R.drawable.ic_addavatar)
            binding.ivBackgroundEndCall.setImageResource(R.drawable.bg_call_pixel5)
        }
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
            android.util.Log.w("EndCallActivity", "Could not set status bar color: ${e.message}")
        }
    }
}
