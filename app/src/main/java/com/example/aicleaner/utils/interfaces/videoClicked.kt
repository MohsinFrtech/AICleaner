package com.example.aicleaner.utils.interfaces

import androidx.navigation.NavDirections
import com.example.aicleaner.models.PictureData
import com.example.aicleaner.models.VideoFileData

////This interface is for controlling navigation between fragments
interface VideoClicked {
    fun videoItem(videoFileData: VideoFileData)
}