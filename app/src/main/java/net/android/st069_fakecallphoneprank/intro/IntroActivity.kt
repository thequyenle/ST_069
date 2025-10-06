package net.android.st069_fakecallphoneprank.intro

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import net.android.st069_fakecallphoneprank.activity.PermissionActivity
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.databinding.ActivityIntroBinding
import kotlin.math.abs

class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding
    private lateinit var introPages: List<IntroPage>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Intro pages data - using full screen background images
        introPages = listOf(
            IntroPage(R.drawable.bg_intro1, getString(R.string.intro1_title)),
            IntroPage(R.drawable.bg_intro2, getString(R.string.intro2_title)),
            IntroPage(R.drawable.bg_intro3, getString(R.string.intro3_title))

        )

        // Setup ViewPager2
        setupViewPager()

        // Attach dots indicator

       // binding.dotsIndicator.setViewPager2(binding.viewPager)

        // Update button text based on current page
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // Optional: Change button text for last page
                // binding.btnNext.text = if (position == introPages.size - 1) {
                //     "Get Started"
                // } else {
                //     getString(R.string.next)
                // }
            }
        })

        // Next/Get Started button
        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem < introPages.size - 1) {
                // Go to next page with smooth scroll
                binding.viewPager.setCurrentItem(
                    binding.viewPager.currentItem + 1,
                    true // Enable smooth scroll animation
                )
            } else {
                // Last page - mark intro as done and go to permission
                getSharedPreferences("fakecall_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("intro_done", true)
                    .apply()

                startActivity(Intent(this, PermissionActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        }
    }

    private fun setupViewPager() {
        val adapter = IntroAdapter(introPages)
        binding.viewPager.adapter = adapter

        // Important: Allow ViewPager to cache adjacent pages for smooth swiping
        binding.viewPager.offscreenPageLimit = 1

        // Add smooth page transformer
        // Choose one of these transformers (uncomment the one you prefer):

        // Option 1: Zoom Out Effect (Recommended)
        binding.viewPager.setPageTransformer(ZoomOutPageTransformer())

        // Option 2: Simple Fade Effect
        // binding.viewPager.setPageTransformer(FadePageTransformer())

        // Option 3: Depth Effect
        // binding.viewPager.setPageTransformer(DepthPageTransformer())
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

    // ========== PAGE TRANSFORMERS ==========

    /**
     * Zoom Out Page Transformer
     * Creates a smooth zoom out effect when swiping
     * Recommended for most use cases
     */
    inner class ZoomOutPageTransformer : ViewPager2.PageTransformer {
        private val minScale = 0.85f
        private val minAlpha = 0.5f

        override fun transformPage(view: View, position: Float) {
            view.apply {
                val pageWidth = width
                val pageHeight = height
                when {
                    position < -1 -> {
                        alpha = 0f
                    }
                    position <= 1 -> {
                        val scaleFactor = minScale.coerceAtLeast(1 - abs(position))
                        val vertMargin = pageHeight * (1 - scaleFactor) / 2
                        val horzMargin = pageWidth * (1 - scaleFactor) / 2
                        translationX = if (position < 0) {
                            horzMargin - vertMargin / 2
                        } else {
                            horzMargin + vertMargin / 2
                        }

                        scaleX = scaleFactor
                        scaleY = scaleFactor

                        alpha = (minAlpha + (((scaleFactor - minScale) / (1 - minScale)) * (1 - minAlpha)))
                    }
                    else -> {
                        alpha = 0f
                    }
                }
            }
        }
    }

    /**
     * Fade Page Transformer
     * Simple fade in/out effect when swiping
     * Good for minimal, clean transitions
     */
    inner class FadePageTransformer : ViewPager2.PageTransformer {
        override fun transformPage(page: View, position: Float) {
            page.apply {
                translationX = -position * width
                alpha = when {
                    position <= -1.0f || position >= 1.0f -> 0.0f
                    position == 0.0f -> 1.0f
                    else -> 1.0f - abs(position)
                }
            }
        }
    }

    /**
     * Depth Page Transformer
     * Creates a depth/3D effect when swiping
     * Good for a more dramatic transition
     */
    inner class DepthPageTransformer : ViewPager2.PageTransformer {
        private val minScale = 0.75f

        override fun transformPage(view: View, position: Float) {
            view.apply {
                val pageWidth = width
                when {
                    position < -1 -> { // [-Infinity,-1)
                        alpha = 0f
                    }
                    position <= 0 -> { // [-1,0]
                        alpha = 1f
                        translationX = 0f
                        translationZ = 0f
                        scaleX = 1f
                        scaleY = 1f
                    }
                    position <= 1 -> { // (0,1]
                        alpha = 1 - position
                        translationX = pageWidth * -position
                        translationZ = -1f
                        val scaleFactor = minScale + (1 - minScale) * (1 - abs(position))
                        scaleX = scaleFactor
                        scaleY = scaleFactor
                    }
                    else -> { // (1,+Infinity]
                        alpha = 0f
                    }
                }
            }
        }
    }
}