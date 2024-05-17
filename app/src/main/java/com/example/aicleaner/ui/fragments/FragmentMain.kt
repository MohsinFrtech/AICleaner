package com.example.aicleaner.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.aicleaner.R
import com.example.aicleaner.databinding.FragmentMainBinding

class FragmentMain : Fragment() {

    private var bindingMain: FragmentMainBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        bindingMain = DataBindingUtil.bind(view)

        bindingMain?.appManager?.setOnClickListener {
            findNavController().navigate(R.id.appManager)
        }
        bindingMain?.videoClean?.setOnClickListener {
            findNavController().navigate(R.id.videoFragment)
        }

        bindingMain?.photoClean?.setOnClickListener {
           findNavController().navigate(R.id.photoCleanFragment)
        }
        return view
    }
}