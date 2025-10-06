package net.android.st069_fakecallphoneprank.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility object for date and time formatting
 */
object DateTimeUtils {

    /**
     * Format timestamp to readable date/time
     */
    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Format timestamp to time only
     */
    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Format timestamp to date only
     */
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Get time difference from now
     */
    fun getTimeUntil(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = timestamp - now

        if (diff < 0) return "Past"

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "$days day${if (days > 1) "s" else ""}"
            hours > 0 -> "$hours hour${if (hours > 1) "s" else ""}"
            minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""}"
            else -> "$seconds second${if (seconds > 1) "s" else ""}"
        }
    }
}

/**
 * Utility object for SET TIME (countdown before call appears)
 */
object SetTimeUtils {

    /**
     * Get available set time options (in seconds)
     * These are countdown timers BEFORE the fake call appears
     */
    fun getSetTimeOptions(): List<Pair<String, Int>> {
        return listOf(
            "Now" to 0,
            "15 seconds" to 15,
            "30 seconds" to 30,
            "1 minute" to 60,
            "5 minutes" to 300,
            "10 minutes" to 600
        )
    }

    /**
     * Convert seconds to readable format for set time
     */
    fun formatSetTime(seconds: Int): String {
        return when (seconds) {
            0 -> "Now"
            in 1..59 -> "$seconds second${if (seconds > 1) "s" else ""}"
            60 -> "1 minute"
            in 61..3599 -> {
                val minutes = seconds / 60
                "$minutes minute${if (minutes > 1) "s" else ""}"
            }
            else -> {
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                if (minutes > 0) {
                    "$hours hour${if (hours > 1) "s" else ""} $minutes min"
                } else {
                    "$hours hour${if (hours > 1) "s" else ""}"
                }
            }
        }
    }

    /**
     * Calculate scheduled timestamp from current time + countdown
     */
    fun calculateScheduledTime(setTimeSeconds: Int): Long {
        return System.currentTimeMillis() + (setTimeSeconds * 1000L)
    }
}

/**
 * Utility object for TALK TIME (how long call screen displays)
 */
object TalkTimeUtils {

    /**
     * Get available talk time options (in seconds)
     * These are durations for how long the call screen stays visible
     */
    fun getTalkTimeOptions(): List<Pair<String, Int>> {
        return listOf(
            "15 seconds" to 15,
            "30 seconds" to 30,
            "1 minute" to 60,
            "5 minutes" to 300,
            "10 minutes" to 600
        )
    }

    /**
     * Convert seconds to readable format for talk time
     */
    fun formatTalkTime(seconds: Int): String {
        return when {
            seconds < 60 -> "$seconds second${if (seconds > 1) "s" else ""}"
            seconds < 3600 -> {
                val minutes = seconds / 60
                "$minutes minute${if (minutes > 1) "s" else ""}"
            }
            else -> {
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                if (minutes > 0) {
                    "$hours hour${if (hours > 1) "s" else ""} $minutes min"
                } else {
                    "$hours hour${if (hours > 1) "s" else ""}"
                }
            }
        }
    }
}

/**
 * Extension functions for FakeCall
 */
object FakeCallUtils {

    /**
     * Check if fake call is scheduled for future
     */
    fun isUpcoming(scheduledTime: Long): Boolean {
        return scheduledTime > System.currentTimeMillis()
    }

    /**
     * Check if fake call is in the past
     */
    fun isPast(scheduledTime: Long): Boolean {
        return scheduledTime < System.currentTimeMillis()
    }

    /**
     * Check if fake call should trigger now
     */
    fun shouldTriggerNow(scheduledTime: Long): Boolean {
        val now = System.currentTimeMillis()
        // Allow 5 second window for triggering
        return scheduledTime <= now && scheduledTime >= (now - 5000)
    }

    /**
     * Get status text for fake call
     */
    fun getStatusText(scheduledTime: Long, isActive: Boolean): String {
        return when {
            !isActive -> "Inactive"
            isPast(scheduledTime) -> "Completed"
            isUpcoming(scheduledTime) -> "Scheduled"
            else -> "Unknown"
        }
    }

    /**
     * Validate phone number format
     */
    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Basic validation - adjust regex based on requirements
        val phoneRegex = Regex("^[+]?[0-9]{10,13}$|^[0-9]{3}-[0-9]{3}-[0-9]{3,4}$")
        return phoneRegex.matches(phoneNumber.replace(" ", ""))
    }

    /**
     * Format phone number for display
     */
    fun formatPhoneNumber(phoneNumber: String): String {
        // Remove all non-digits
        val digits = phoneNumber.filter { it.isDigit() }

        return when {
            digits.length == 10 -> {
                // Format as XXX-XXX-XXXX
                "${digits.substring(0, 3)}-${digits.substring(3, 6)}-${digits.substring(6)}"
            }
            digits.length == 11 -> {
                // Format as X-XXX-XXX-XXXX
                "${digits.substring(0, 1)}-${digits.substring(1, 4)}-${digits.substring(4, 7)}-${digits.substring(7)}"
            }
            else -> phoneNumber // Return original if doesn't match expected length
        }
    }
}

/**
 * Device type utilities
 */
object DeviceUtils {

    /**
     * Get available device types based on images
     */
    fun getDeviceTypes(): List<String> {
        return listOf(
            "Pixel 5",
            "Oppo",
            "iOS 12",
            "Samsung S20",
            "Xiaomi",
            "iOS 13",
            "Samsung A10",
            "Vivo"
        )
    }
}

/**
 * Voice type utilities
 */
object VoiceUtils {

    /**
     * Get preset voice types
     */
    fun getPresetVoices(): List<String> {
        return listOf(
            "Mom",
            "My love",
            "Cattry",
            "Male police voice"
        )
    }

    /**
     * Check if voice is custom (recorded)
     */
    fun isCustomVoice(voiceType: String?): Boolean {
        if (voiceType == null) return false
        return voiceType.startsWith("/") || voiceType.contains(".mp3") || voiceType.contains(".wav")
    }

    /**
     * Get voice display name
     */
    fun getVoiceDisplayName(voiceType: String?): String {
        if (voiceType == null) return "No voice"
        return if (isCustomVoice(voiceType)) {
            "Custom Voice"
        } else {
            voiceType
        }
    }
}