package com.example.aicleaner.models

import android.graphics.drawable.Drawable

data class AppManagerModel(
    var appName:String="",
    var appIcon:Drawable?= null,
    var appPackageName:String?=""
)