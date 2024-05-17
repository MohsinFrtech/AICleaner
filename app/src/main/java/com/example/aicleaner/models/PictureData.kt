package com.example.aicleaner.models

import android.graphics.drawable.Drawable
import android.net.Uri
import java.math.BigDecimal

data class PictureData(
    val id: Long?,
    val contentUri: Uri?,
    var picName: String? = "",
    var picPath: String? = "",
    var picSize: String? = "",
    var picUri: String? = "",
    var picSelected: Boolean? = false
)