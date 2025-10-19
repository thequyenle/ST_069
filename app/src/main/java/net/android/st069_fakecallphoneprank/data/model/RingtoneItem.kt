package net.android.st069_fakecallphoneprank.data.model

import android.net.Uri

data class RingtoneItem(
    val name: String,
    val uri: Uri,
    var isSelected: Boolean = false
)
