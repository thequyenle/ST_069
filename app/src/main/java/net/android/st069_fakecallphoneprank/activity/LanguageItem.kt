package net.android.st069_fakecallphoneprank.activity

import androidx.annotation.DrawableRes

// Language Item
data class LanguageItem(
    val name: String,
    @DrawableRes val flagResId: Int,
    var isSelected: Boolean = false,
    val code: String  // ISO language code: en, es, fr, hi, pt, de, id
)