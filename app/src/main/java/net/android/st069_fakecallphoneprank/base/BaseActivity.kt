package net.android.st069_fakecallphoneprank.base

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import net.android.st069_fakecallphoneprank.utils.LocaleHelper

/**
 * Base Activity that provides edge-to-edge fullscreen with hidden navigation bar
 * All activities should extend this class for consistent UI behavior
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Apply fullscreen before setContentView
        applyFullscreenEdgeToEdge()
    }

    override fun onResume() {
        super.onResume()
        // Reapply fullscreen when activity resumes
        hideNavigationBarAndSetStatusBarAppearance()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Reapply when window regains focus
            hideNavigationBarAndSetStatusBarAppearance()
        }
    }

    /**
     * Apply edge-to-edge fullscreen with hidden navigation bar
     * Status bar remains visible with transparent color by default
     * Call screens can override setStatusBarAppearance() to use light icons
     */
    private fun applyFullscreenEdgeToEdge() {
        try {
            // Set status bar color FIRST (before making edge-to-edge)
            setStatusBarColor()

            // Make window edge-to-edge (draw behind system bars)
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // Hide navigation bar and set status bar appearance together
            hideNavigationBarAndSetStatusBarAppearance()
        } catch (e: Exception) {
            android.util.Log.w("BaseActivity", "Could not apply fullscreen: ${e.message}")
        }
    }

    /**
     * Hide navigation bar and set status bar appearance
     * Must be done together to avoid flags overwriting each other
     */
    private fun hideNavigationBarAndSetStatusBarAppearance() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ (API 30+)
                window.insetsController?.let { controller ->
                    controller.hide(WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

                    // Set status bar appearance (can be overridden by child activities)
                    setStatusBarAppearance()
                }
            } else {
                // Android 6-10 (API 23-29)
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or getStatusBarAppearanceFlag()  // Add status bar appearance flag
                )
            }
        } catch (e: Exception) {
            android.util.Log.w("BaseActivity", "Could not hide navigation bar: ${e.message}")
        }
    }

    /**
     * Get the status bar appearance flag for legacy API (Android 6-10)
     * Default: light status bar (dark icons)
     * Call screens override to return 0 (dark status bar with light icons)
     */
    @Suppress("DEPRECATION")
    protected open fun getStatusBarAppearanceFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            0
        }
    }

    /**
     * Set status bar color to transparent
     * This method can be overridden by child activities to use different colors
     */
    protected open fun setStatusBarColor() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor = android.graphics.Color.TRANSPARENT
            }
        } catch (e: Exception) {
            android.util.Log.w("BaseActivity", "Could not set status bar color: ${e.message}")
        }
    }

    /**
     * Set status bar appearance (light/dark icons)
     * Default: light appearance (dark icons on light background)
     * Call screens can override to use dark appearance (light icons on dark background)
     */
    protected open fun setStatusBarAppearance() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ (API 30+)
                window.insetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android 6+ (API 23+)
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        } catch (e: Exception) {
            android.util.Log.w("BaseActivity", "Could not set status bar appearance: ${e.message}")
        }
    }
}

