package net.android.st069_fakecallphoneprank.base

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

/**
 * Base Activity that provides edge-to-edge fullscreen with hidden navigation bar
 * All activities should extend this class for consistent UI behavior
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Apply fullscreen before setContentView
        applyFullscreenEdgeToEdge()
    }

    override fun onResume() {
        super.onResume()
        // Reapply fullscreen when activity resumes
        hideNavigationBar()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Reapply when window regains focus
            hideNavigationBar()
        }
    }

    /**
     * Apply edge-to-edge fullscreen with hidden navigation bar
     * Status bar remains visible
     */
    private fun applyFullscreenEdgeToEdge() {
        try {
            // Make window edge-to-edge (draw behind system bars)
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // Hide navigation bar
            hideNavigationBar()
        } catch (e: Exception) {
            android.util.Log.w("BaseActivity", "Could not apply fullscreen: ${e.message}")
        }
    }

    /**
     * Hide only the navigation bar, keep status bar visible
     */
    private fun hideNavigationBar() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ (API 30+)
                window.insetsController?.let { controller ->
                    controller.hide(WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                // Android 9-10 (API 28-29)
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
            }
        } catch (e: Exception) {
            android.util.Log.w("BaseActivity", "Could not hide navigation bar: ${e.message}")
        }
    }
}

