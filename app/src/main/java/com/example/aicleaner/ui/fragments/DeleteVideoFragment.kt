package com.example.aicleaner.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.aicleaner.R
import com.example.aicleaner.databinding.DeleteVideoFragmentBinding


class DeleteVideoFragment:Fragment() {

    private var bindingDeleteVideo:DeleteVideoFragmentBinding?=null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layDelete = inflater.inflate(R.layout.delete_video_fragment,container,false)
        bindingDeleteVideo=DataBindingUtil.bind(layDelete)
        showAllVideosInParticularFolder()
        return layDelete
    }

    //Show all videos in particular folder....
    private fun showAllVideosInParticularFolder() {

    }


}