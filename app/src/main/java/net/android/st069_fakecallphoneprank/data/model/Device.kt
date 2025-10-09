package net.android.st069_fakecallphoneprank.data.model

data class Device(
    val id: Int,
    val name: String,
    val iconRes: Int,
    val isSelected: Boolean = false
)