package net.android.st069_fakecallphoneprank.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fake_calls")
data class FakeCall(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 1. Avatar image path (local URI or resource path)
    val avatar: String? = null,

    // 2. Contact name
    val name: String,

    // 3. Phone number
    val phoneNumber: String,

    // 4. Voice type or path to recorded voice
    val voiceType: String? = null,

    // 5. Selected device model (e.g., "Pixel 5", "Oppo", "Samsung S20")
    val deviceType: String? = null,

    // 6. Set Time - Countdown in SECONDS before fake call appears
    // Options: Now (0), 15s, 30s, 60s (1min), 300s (5min), 600s (10min)
    // Or custom time in seconds
    val setTime: Int = 0, // 0 means "Now"

    // 7. Talk Time - Duration in SECONDS that call screen displays
    // Options: 15s, 30s, 60s (1min), 300s (5min), 600s (10min)
    val talkTime: Int = 15, // Default 15 seconds

    // Additional fields for app functionality
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),

    // Scheduled timestamp - when the call should actually trigger
    // This is calculated as: createdAt + (setTime * 1000)
    val scheduledTime: Long = System.currentTimeMillis() + (setTime * 1000L)
)