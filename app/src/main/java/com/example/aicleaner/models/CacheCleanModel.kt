package com.example.aicleaner.models

import android.graphics.drawable.Drawable

data class CacheCleanModel(
    var appName:String="",
    var appIcon:Drawable?= null,
    var appPackageName:String?="",
    var appCacheSize:String?="",
    var cacheAppSelected:Boolean=true
)