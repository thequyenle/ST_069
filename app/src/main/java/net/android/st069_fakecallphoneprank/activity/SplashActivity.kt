package net.android.st069_fakecallphoneprank.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import net.android.st069_fakecallphoneprank.HomeActivity
import net.android.st069_fakecallphoneprank.base.BaseActivity
import net.android.st069_fakecallphoneprank.databinding.ActivitySplashBinding
import net.android.st069_fakecallphoneprank.intro.IntroActivity
import net.android.st069_fakecallphoneprank.utils.LocaleHelper

class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load animation


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
}