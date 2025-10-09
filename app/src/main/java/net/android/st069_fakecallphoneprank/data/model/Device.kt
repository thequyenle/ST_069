package net.android.st069_fakecallphoneprank.data.model

data class Device(
    val id: Int,
    val name: String,
    val iconRes: Int, // This should be device preview drawable like devices_pixel5
    val isSelected: Boolean = false
)