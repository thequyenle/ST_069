// 1. API Response Models (Updated for your data format)
// Save to: app/src/main/java/net/android/st069_fakecallphoneprank/data/model/ApiModels.kt

package net.android.st069_fakecallphoneprank.data.model

import com.google.gson.annotations.SerializedName

// Single fake call from API (matching your format)
data class FakeCallApi(
    @SerializedName("id")
    val id: String,

    @SerializedName("category")
    val category: String, // "kid" or "general"

    @SerializedName("mp3")
    val mp3: String, // Path to sound file

    @SerializedName("avatar")
    val avatar: String, // Path to avatar image

    @SerializedName("name")
    val name: String,

    @SerializedName("phone")
    val phone: String
) {
    // Helper function to get full avatar URL
    fun getFullAvatarUrl(baseUrl: String): String {
        return if (avatar.startsWith("http")) {
            avatar
        } else {
            baseUrl + avatar
        }
    }

    // Helper function to get full mp3 URL
    fun getFullMp3Url(baseUrl: String): String {
        return if (mp3.startsWith("http")) {
            mp3
        } else {
            baseUrl + mp3
        }
    }
}

// Category for filtering
enum class CallCategory(val value: String) {
    KID("kid"),
    GENERAL("general"),
    ALL("all")
}