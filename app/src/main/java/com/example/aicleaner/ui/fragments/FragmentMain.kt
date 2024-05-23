package com.example.aicleaner.ui.fragments

import android.app.usage.StorageStats
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.os.UserHandle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.aicleaner.R
import com.example.aicleaner.databinding.FragmentMainBinding
import com.example.aicleaner.viewmodels.PhotoCleanViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

class FragmentMain : Fragment() {

    private var bindingMain: FragmentMainBinding? = null
    private val photoCleanViewModel by lazy {
        ViewModelProvider(requireActivity())[PhotoCleanViewModel::class.java]
    }
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

        bindingMain?.cacheClean?.setOnClickListener {
            findNavController().navigate(R.id.cacheCleanFragment)
        }

//        requireActivity().startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))


        calculateStorage()

        photoCleanViewModel.getSpace(requireContext())
        photoCleanViewModel.totalCache.observe(viewLifecycleOwner, Observer {
            Log.d("CacheData",it.toString())

            bindingMain?.txtData?.text = it.toString()
        })
       // bindingMain?.txtData?.text = photoCleanViewModel.totalCache.toString()


        return view
    }

    //<editor-fold desc="Calculate Storage">
    private fun calculateStorage(){
        val totalStorage = getTotalStorage()
        val availableStorage = getAvailableStorage()
        val usedStorage = totalStorage - availableStorage
        val usedStoragePercentage = (usedStorage.toDouble() / totalStorage.toDouble() * 100).toInt()

        bindingMain?.storageProgressBar?.progress = usedStoragePercentage

        val formatter = DecimalFormat("#,###")

        bindingMain?.txtAvailableStorage?.text = "Used  ${formatter.format(usedStorage / (1024 * 1024 * 1024))} GB / ${formatter.format(totalStorage / (1024 * 1024 * 1024))} GB"
    }
    private fun getTotalStorage(): Long {
        val stat = StatFs(Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        return totalBlocks * blockSize
    }
    private fun getAvailableStorage(): Long {
        val stat = StatFs(Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        return availableBlocks * blockSize
    }

    //</editor-fold>


//    @RequiresApi(Build.VERSION_CODES.O)


    private fun bytesToMB(bytes: Long): Double {
        return bytes / (1024.0 * 1024.0)
    }



}