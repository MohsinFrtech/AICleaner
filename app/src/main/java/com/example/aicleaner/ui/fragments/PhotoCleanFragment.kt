package com.example.aicleaner.ui.fragments

import android.os.Bundle
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
import com.example.aicleaner.databinding.PhotoCleanLayoutBinding
import com.example.aicleaner.models.ImageFolder
import com.example.aicleaner.ui.adapters.FolderItemAdapter
import com.example.aicleaner.utils.interfaces.NavigateData
import com.example.aicleaner.viewmodels.PhotoCleanViewModel

class PhotoCleanFragment : Fragment() ,NavigateData{

    private var bindingPhoto: PhotoCleanLayoutBinding? = null
    private val photoCleanViewModel by lazy {
        ViewModelProvider(requireActivity())[PhotoCleanViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layPhoto = inflater.inflate(R.layout.photo_clean_layout, container, false)
        bindingPhoto = DataBindingUtil.bind(layPhoto)
        bindingPhoto?.backIcon?.setOnClickListener {
            findNavController().popBackStack()
        }
        getAllPicturesPaths()
        return layPhoto
    }

    private fun getAllPicturesPaths() {
        photoCleanViewModel?.getImageFoldersInDevice(requireContext())
        photoCleanViewModel?.imageFoldersList?.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                setAdapter(it)
            }
        })


    }

    private fun setAdapter(imageFolders: List<ImageFolder?>) {
        val adapter = FolderItemAdapter(requireContext(),this)
        bindingPhoto?.photoRecycler?.layoutManager =
            LinearLayoutManager(context)
        bindingPhoto?.photoRecycler?.adapter = adapter
        adapter?.submitList(imageFolders)
    }

    override fun navigation(viewId: NavDirections) {
        findNavController().navigate(viewId)
    }
}