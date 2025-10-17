package net.android.st069_fakecallphoneprank.utils

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.core.view.WindowCompat

object FullscreenHelper {

    /**
     * Makes the activity edge-to-edge and hides only navigation bar (keeps status bar visible)
     */
    fun enableFullscreen(activity: Activity) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ (API 30+)
                activity.window?.setDecorFitsSystemWindows(false)

                activity.window?.insetsController?.let { controller ->
                    // Hide only navigation bar, keep status bar visible
                    controller.hide(WindowInsets.Type.navigationBars())
                    // Set behavior to show bars temporarily when user swipes
                    controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                // Android 9-10 (API 28-29)
                @Suppress("DEPRECATION")
                activity.window?.decorView?.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
            }

            // Make window edge-to-edge (draw behind system bars)
            activity.window?.let { window ->
                WindowCompat.setDecorFitsSystemWindows(window, false)
                // Keep screen on during activity
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        } catch (e: Exception) {
            // Silently fail if window is not ready
            android.util.Log.w("FullscreenHelper", "Could not enable fullscreen: ${e.message}")
        }
    }
}
