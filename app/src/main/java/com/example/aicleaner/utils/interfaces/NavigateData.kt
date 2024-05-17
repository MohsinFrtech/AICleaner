package com.example.aicleaner.utils.interfaces

import androidx.navigation.NavDirections

////This interface is for controlling navigation between fragments
interface NavigateData {
    fun navigation(viewId: NavDirections)
}