package net.android.st069_fakecallphoneprank.data.model

import androidx.annotation.DrawableRes

data class Device(
    val id: String,
    val name: String,
    @DrawableRes val previewImage: Int,
    var isSelected: Boolean = false
)