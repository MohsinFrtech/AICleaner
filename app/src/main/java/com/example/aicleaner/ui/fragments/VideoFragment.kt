package com.example.aicleaner.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aicleaner.R
import com.example.aicleaner.databinding.VideoFragmentBinding
import com.example.aicleaner.models.ImageFolder
import com.example.aicleaner.models.VideoFolder
import com.example.aicleaner.ui.adapters.FolderItemAdapter
import com.example.aicleaner.ui.adapters.VideoFolderItemAdapter
import com.example.aicleaner.utils.interfaces.NavigateData
import com.example.aicleaner.viewmodels.PhotoCleanViewModel
import com.example.aicleaner.viewmodels.VideoCleanViewModel

class VideoFragment:Fragment(),NavigateData {

    var bindingVideo:VideoFragmentBinding?=null
    private val videoCleanViewModel by lazy {
        ViewModelProvider(requireActivity())[VideoCleanViewModel::class.java]
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val lay = inflater.inflate(R.layout.video_fragment,container,false)
        bindingVideo = DataBindingUtil.bind(lay)
        bindingVideo?.backIcon?.setOnClickListener {
            findNavController()?.popBackStack()
        }
        bindingVideo?.lottiePlayer?.visibility=View.VISIBLE
        getAllVideosFromStorage()
        return lay
    }

    private fun getAllVideosFromStorage() {
        videoCleanViewModel?.getAllVideosFromGalleryWithFolders(requireContext())
        videoCleanViewModel?.videoFoldersList?.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()){
                it.forEach {
                    Log.d("VideoFolderPaths","path"+it?.path)
                }
                setAdapter(it)
            }
        })

    }

    private fun setAdapter(imageFolders: List<VideoFolder?>) {
        bindingVideo?.lottiePlayer?.visibility=View.GONE
        val adapter = VideoFolderItemAdapter(requireContext(),this)
        bindingVideo?.photoRecycler?.layoutManager =
            LinearLayoutManager(context)
        bindingVideo?.photoRecycler?.adapter = adapter
        adapter?.submitList(imageFolders)
    }

    override fun navigation(viewId: NavDirections) {
      findNavController().navigate(viewId)
    }
}