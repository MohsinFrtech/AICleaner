package com.example.aicleaner.utils.interfaces

import androidx.navigation.NavDirections
import com.example.aicleaner.models.PictureData

////This interface is for controlling navigation between fragments
interface PhotoClicked {
    fun photoItem(pictureData: PictureData)
}