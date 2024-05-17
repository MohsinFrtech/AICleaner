package com.example.aicleaner.models

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import java.math.BigDecimal

data class VideoFolder(
    var path:String?="",
    var videothumbnail:Bitmap?,
    var videoFolderName:String?= "",
    var numberOfVideos: Int=0,
    var videoFolderSize:BigDecimal?=BigDecimal(0.0)
)