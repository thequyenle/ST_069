package net.android.st069_fakecallphoneprank.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.base.BaseActivity
import net.android.st069_fakecallphoneprank.databinding.ActivityMoreBinding
import net.android.st069_fakecallphoneprank.dialog.RatingDialog
import net.android.st069_fakecallphoneprank.utils.LocaleHelper

class MoreActivity : BaseActivity() {

    private lateinit var binding: ActivityMoreBinding

    private var isRated = false
    private lateinit var layoutRateUs: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        layoutRateUs = binding.layoutRate
        loadRatingStatus()

        setupClickListeners()
    }

    companion object {
        private const val PREFS_NAME = "FakeCallSettings"  // or any name you prefer
        private const val KEY_RATED = "is_rated"
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase))
    }

    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Language
        binding.layoutLanguage.setOnClickListener {
            val intent = Intent(this, LanguageActivity::class.java)
            intent.putExtra("from_settings", true)
            startActivity(intent)
        }

        // Rate Us
        binding.layoutRate.setOnClickListener {

            showRatingDialog()
        }

        // Share App
        binding.layoutShare.setOnClickListener {
            shareApp()
        }

        // Privacy Policy
        binding.layoutPrivacy.setOnClickListener {
            // TODO: Open privacy policy
            openPrivacyPolicy()
        }


    }

    private fun showRatingDialog() {
        RatingDialog.show(
            this,
            onRatingSubmitted = { rating ->
                // User đã chọn rating và submit
                handleRatingSubmitted()
            },
            onDismiss = {
                // Dialog đóng nhưng không submit (ấn Exit hoặc touch outside)
                // Không làm gì, giữ nguyên trạng thái
            }
        )
    }

    private fun handleRatingSubmitted() {
        // Đánh dấu đã rating
        isRated = true
        saveRatingStatus(true)

        // Ẩn layout Rate Us với animation
        layoutRateUs.animate()
            .alpha(0f)
            .translationY(-layoutRateUs.height.toFloat())
            .setDuration(300)
            .withEndAction {
                layoutRateUs.visibility = View.GONE
            }
            .start()
    }

    // To this:
    private fun saveRatingStatus(rated: Boolean) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_RATED, rated).apply()
    }

    private fun loadRatingStatus() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isRated = prefs.getBoolean(KEY_RATED, false)

        // Nếu đã rating rồi thì ẩn luôn
        if (isRated) {
            layoutRateUs.visibility = View.GONE
        }
    }


    private fun shareApp() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "Check out this amazing Alarm Clock app: http://play.google.com/store/apps/details?id=${packageName}"
            )
        }
        startActivity(Intent.createChooser(shareIntent, "Share app via"))
    }

    private fun openPrivacyPolicy() {
        val url = "https://sites.google.com/view/docx-reader-office-viewer/home"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse(url)
        }
        startActivity(intent)
    }



}