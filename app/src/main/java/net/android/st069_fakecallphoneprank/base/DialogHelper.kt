package net.android.st069_fakecallphoneprank.base

import android.app.Dialog
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.core.view.WindowCompat

/**
 * Helper object to apply fullscreen edge-to-edge to dialogs
 */
object DialogHelper {

    /**
     * Apply edge-to-edge fullscreen with hidden navigation bar to a dialog
     * Call this after dialog.show()
     * 
     * @param dialog The dialog to apply fullscreen to
     * @param onDismissCallback Optional callback to reapply fullscreen to parent activity
     */
    fun applyFullscreenToDialog(dialog: Dialog, onDismissCallback: (() -> Unit)? = null) {
        dialog.window?.let { window ->
            hideNavigationBarForDialog(window)
        }

        // Reapply fullscreen to parent activity when dialog is dismissed
        onDismissCallback?.let { callback ->
            dialog.setOnDismissListener {
                callback()
            }
        }
    }

    /**
     * Hide navigation bar for a dialog window
     */
    private fun hideNavigationBarForDialog(window: Window) {
        try {
            // Make dialog edge-to-edge
            WindowCompat.setDecorFitsSystemWindows(window, false)

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
            android.util.Log.w("DialogHelper", "Could not hide navigation bar: ${e.message}")
        }
    }
}

