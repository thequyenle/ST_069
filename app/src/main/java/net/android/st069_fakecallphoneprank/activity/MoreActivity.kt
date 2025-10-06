package net.android.st069_fakecallphoneprank.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import net.android.st069_fakecallphoneprank.databinding.ActivityMoreBinding

class MoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
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
            openPlayStore()
        }

        // Share App
        binding.layoutShare.setOnClickListener {
            shareApp()
        }

        // Privacy Policy
        binding.layoutPrivacy.setOnClickListener {
            // TODO: Open privacy policy
            openUrl("https://your-privacy-policy-url.com")
        }

        // About
        binding.layoutAbout.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun openPlayStore() {
        try {
            val uri = Uri.parse("market://details?id=$packageName")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } catch (e: Exception) {
            val uri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Fake Call App")
            putExtra(
                Intent.EXTRA_TEXT,
                "Check out this awesome Fake Call app: https://play.google.com/store/apps/details?id=$packageName"
            )
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun showAboutDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("About")
            .setMessage("Fake Call - Prank Phone\nVersion 1.0\n\nCreate realistic fake calls to prank your friends!")
            .setPositiveButton("OK", null)
            .show()
    }
}