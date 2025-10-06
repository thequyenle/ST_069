package net.android.st069_fakecallphoneprank.data.model

import androidx.annotation.DrawableRes

data class Voice(
    val id: String,
    val name: String,
    val duration: String = "2s", // Default duration
    val filePath: String? = null, // Path for custom recordings
    @DrawableRes val icon: Int,
    val isCustom: Boolean = false,
    var isSelected: Boolean = false
)