package net.android.st069_fakecallphoneprank.utils

import android.app.Activity
import android.os.Build
import android.view.View
import androidx.core.view.*

object ImmersiveUtils {

    /**
     * Edge-to-edge, hide ONLY the navigation bar.
     * Status bar stays visible.
     *
     * Call from Activity.onResume(). Pass your root view if you want
     * automatic padding under the status bar (so content isn't overlapped).
     *
     * @param root Optional: your root layout (e.g., findViewById(R.id.main)).
     *             If null => true edge-to-edge (content may go under status bar).
     * @param padTopForStatusBar If true, adds top padding equal to status bar height to the root.
     */
    fun applyEdgeToEdgeHideNav(activity: Activity, root: View? = null, padTopForStatusBar: Boolean = true) {
        val window = activity.window
        val decor = window.decorView

        // Draw behind system bars (edge-to-edge)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Optional: keep content below the visible status bar
        if (root != null && padTopForStatusBar) {
            ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
                val status = insets.getInsets(WindowInsetsCompat.Type.statusBars())
                // Add top padding so content doesn't sit under the visible status bar.
                v.setPadding(v.paddingLeft, status.top, v.paddingRight, v.paddingBottom)
                insets
            }
            root.requestApplyInsets()
        }

        // Hide ONLY the navigation bar; keep status bar visible
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = WindowInsetsControllerCompat(window, decor)
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            @Suppress("DEPRECATION")
            decor.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN   // draw under status bar
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION      // hide nav ONLY
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)    // swipe to reveal
            // Note: we DO NOT set SYSTEM_UI_FLAG_FULLSCREEN (which would hide status bar)
        }
    }
}
