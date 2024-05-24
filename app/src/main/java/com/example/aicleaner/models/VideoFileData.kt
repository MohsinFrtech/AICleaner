package com.example.aicleaner.models

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import java.math.BigDecimal

data class VideoFileData(
    val id: Long?,
    val contentUri: Uri?,
    var videopath:String?="",
    var videothumbnail:Bitmap?,
    var videoDuration:String?="",
    var videoSelected:Boolean=false,
    var videoSize:BigDecimal?=BigDecimal(0.0)
)