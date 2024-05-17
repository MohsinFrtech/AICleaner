package com.example.aicleaner.models

import android.graphics.drawable.Drawable
import java.math.BigDecimal

data class ImageFolder(
    var path:String?="",
    var folderName:String?= "",
    var numberOfPics: Int=0,
    var firstPic:String?="",
    var folderSize:BigDecimal?=BigDecimal(0.0)

)