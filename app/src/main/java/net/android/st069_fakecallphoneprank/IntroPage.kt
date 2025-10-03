package net.android.st069_fakecallphoneprank

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
// Intro Page
data class IntroPage(
    @DrawableRes val imageRes: Int,
    val title: String,
    val description: String
)