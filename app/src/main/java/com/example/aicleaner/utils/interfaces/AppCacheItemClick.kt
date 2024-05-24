package com.example.aicleaner.utils.interfaces

import androidx.navigation.NavDirections
import com.example.aicleaner.models.CacheCleanModel

////This interface is for controlling navigation between fragments
interface AppCacheItemClick {
    fun cacheClick(model: CacheCleanModel)
}