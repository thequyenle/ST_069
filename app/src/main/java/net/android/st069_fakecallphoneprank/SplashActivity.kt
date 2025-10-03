package net.android.st069_fakecallphoneprank

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import net.android.st069_fakecallphoneprank.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load animation
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        binding.ivLogo.startAnimation(fadeIn)
        binding.tvAppName.startAnimation(fadeIn)

        // Check onboarding status
        val prefs = getSharedPreferences("fakecall_prefs", MODE_PRIVATE)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = when {
                !prefs.getBoolean("language_done", false) ->
                    Intent(this, LanguageActivity::class.java)
                !prefs.getBoolean("intro_done", false) ->
                    Intent(this, IntroActivity::class.java)
                !prefs.getBoolean("permission_done", false) ->
                    Intent(this, PermissionActivity::class.java)
                else ->
                    Intent(this, HomeActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, 3000) // 3 seconds
    }

    override fun onResume() {
        super.onResume()
        showSystemUI()
    }

    private fun Activity.showSystemUI() {
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }
}