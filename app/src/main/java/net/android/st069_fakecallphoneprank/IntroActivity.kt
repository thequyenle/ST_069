package net.android.st069_fakecallphoneprank

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import net.android.st069_fakecallphoneprank.IntroAdapter
import net.android.st069_fakecallphoneprank.IntroPage
import net.android.st069_fakecallphoneprank.databinding.ActivityIntroBinding
import com.google.android.material.tabs.TabLayoutMediator

class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding
    private lateinit var introPages: List<IntroPage>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Intro pages data - only title, no description
        introPages = listOf(
            IntroPage(
                R.drawable.intro_slide1,
                getString(R.string.intro1_title)
            ),
            IntroPage(
                R.drawable.intro_slide2,
                getString(R.string.intro2_title)
            ),
            IntroPage(
                R.drawable.intro_slide3,
                getString(R.string.intro3_title)
            )
        )

        // Setup ViewPager2
        binding.viewPager.adapter = IntroAdapter(introPages)

        // Setup dots indicator
        TabLayoutMediator(binding.dotsIndicator, binding.viewPager) { _, _ ->
            // Nothing to do here, just attach
        }.attach()

        // Update button text based on current page
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == introPages.size - 1) {
                    binding.btnNext.text = getString(R.string.get_started)
                } else {
                    binding.btnNext.text = getString(R.string.next)
                }
            }
        })

        // Next/Get Started button
        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem < introPages.size - 1) {
                // Go to next page
                binding.viewPager.currentItem += 1
            } else {
                // Last page - go to permission
                getSharedPreferences("fakecall_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("intro_done", true)
                    .apply()

                startActivity(Intent(this, PermissionActivity::class.java))
                finish()
            }
        }

        // Skip button
        binding.btnSkip.setOnClickListener {
            getSharedPreferences("fakecall_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("intro_done", true)
                .apply()

            startActivity(Intent(this, PermissionActivity::class.java))
            finish()
        }
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