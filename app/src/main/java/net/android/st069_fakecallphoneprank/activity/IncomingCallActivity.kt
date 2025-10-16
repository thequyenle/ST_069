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

        // DEBUG: Log the device type
        Log.d("IncomingCallActivity", "Device Type Received: '$deviceType'")
        Log.d("IncomingCallActivity", "Name: $name")
        Log.d("IncomingCallActivity", "Phone: $phoneNumber")

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

                // Set avatar
                if (!avatar.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(Uri.parse(avatar))
                        .placeholder(R.drawable.ic_addavatar)
                        .circleCrop()
                        .into(pixel5Binding!!.ivAvatar)

                    // Set background using avatar with blur/darken effect
                    setBackgroundFromAvatar(Uri.parse(avatar))
                } else {
                    pixel5Binding?.ivAvatar?.setImageResource(R.drawable.ic_addavatar)
                    // Set default background if no avatar
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

    // ========== PIXEL 5 SPECIFIC METHODS ==========

    private fun setBackgroundFromAvatar(avatarUri: Uri) {
        try {
            Glide.with(this)
                .asBitmap()
                .load(avatarUri)
                .transform(CenterCrop())
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                        // Create a blurred version of the bitmap for background
                        val blurredBitmap = createBlurredBitmap(resource)
                        pixel5Binding?.ivBackground?.setImageBitmap(blurredBitmap)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Set default background if loading is cleared
                        pixel5Binding?.ivBackground?.setImageResource(R.drawable.bg_call_pixel5)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        // Set default background if loading fails
                        pixel5Binding?.ivBackground?.setImageResource(R.drawable.bg_call_pixel5)
                    }
                })
        } catch (e: Exception) {
            // Set default background if any error occurs
            pixel5Binding?.ivBackground?.setImageResource(R.drawable.bg_call_pixel5)
        }
    }

    private fun createBlurredBitmap(originalBitmap: Bitmap): Bitmap {
        val width = originalBitmap.width
        val height = originalBitmap.height
        val blurredBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(blurredBitmap)
        val paint = Paint()

        // Apply blur effect using a simple box blur algorithm (70% blur)
        val blurredImage = fastBlur(originalBitmap, 20f)

        // Draw the blurred image
        canvas.drawBitmap(blurredImage, 0f, 0f, paint)

        // Apply slight darkening to ensure UI elements remain visible
        paint.colorFilter = PorterDuffColorFilter(Color.argb(60, 0, 0, 0), PorterDuff.Mode.SRC_ATOP)
        canvas.drawBitmap(blurredImage, 0f, 0f, paint)

        return blurredBitmap
    }

    private fun fastBlur(sentBitmap: Bitmap, radius: Float): Bitmap {
        val bitmap = sentBitmap.copy(sentBitmap.config, true)
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val radiusInt = radius.toInt()
        val wm = width - 1
        val hm = height - 1
        val wh = width * height
        val div = 2 * radiusInt + 1
        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int

        val vmin = IntArray(Math.max(width, height))

        var divsum = div + 1 shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        i = 0
        while (i < 256 * divsum) {
            dv[i] = i / divsum
            i++
        }

        yi = 0
        yw = yi

        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int

        y = 0
        while (y < height) {
            var bsum = 0
            var gsum = 0
            var rsum = 0
            var boutsum = 0
            var goutsum = 0
            var routsum = 0
            var binsum = 0
            var ginsum = 0
            var rinsum = 0
            i = -radiusInt
            while (i <= radiusInt) {
                p = pixels[yi + Math.min(wm, Math.max(i, 0))]
                rinsum += p and 0xff0000 shr 16
                ginsum += p and 0x00ff00 shr 8
                binsum += p and 0x0000ff
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                i++
            }
            stackpointer = radiusInt

            x = 0
            while (x < width) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radiusInt + div
                routsum -= stack[stackstart and (div - 1)][0]
                goutsum -= stack[stackstart and (div - 1)][1]
                boutsum -= stack[stackstart and (div - 1)][2]

                if (yi < wm) {
                    p = pixels[yi + wm]
                    rinsum += p and 0xff0000 shr 16
                    ginsum += p and 0x00ff00 shr 8
                    binsum += p and 0x0000ff
                } else {
                    p = 0
                }

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                rinsum -= stack[stackpointer][0]
                ginsum -= stack[stackpointer][1]
                binsum -= stack[stackpointer][2]

                stack[stackpointer][0] = p and 0xff0000 shr 16
                stack[stackpointer][1] = p and 0x00ff00 shr 8
                stack[stackpointer][2] = p and 0x0000ff

                yi++
                x++
            }
            yw += width
            y++
        }

        x = 0
        while (x < width) {
            var bsum = 0
            var gsum = 0
            var rsum = 0
            var boutsum = 0
            var goutsum = 0
            var routsum = 0
            var binsum = 0
            var ginsum = 0
            var rinsum = 0
            yp = -radiusInt * width
            i = -radiusInt
            while (i <= radiusInt) {
                yi = Math.max(0, yp) + x
                rinsum += r[yi]
                ginsum += g[yi]
                binsum += b[yi]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                yp += width
                i++
            }
            yi = x
            stackpointer = radiusInt

            y = 0
            while (y < height) {
                pixels[yi] = -0x1000000 and pixels[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radiusInt + div
                routsum -= stack[stackstart and (div - 1)][0]
                goutsum -= stack[stackstart and (div - 1)][1]
                boutsum -= stack[stackstart and (div - 1)][2]

                if (yi + wm < wh) {
                    p = pixels[yi + wm]
                    rinsum += p and 0xff0000 shr 16
                    ginsum += p and 0x00ff00 shr 8
                    binsum += p and 0x0000ff
                } else {
                    p = 0
                }

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                rinsum -= stack[stackpointer][0]
                ginsum -= stack[stackpointer][1]
                binsum -= stack[stackpointer][2]

                stack[stackpointer][0] = p and 0xff0000 shr 16
                stack[stackpointer][1] = p and 0x00ff00 shr 8
                stack[stackpointer][2] = p and 0x0000ff

                yi += width
                y++
            }
            x++
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
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