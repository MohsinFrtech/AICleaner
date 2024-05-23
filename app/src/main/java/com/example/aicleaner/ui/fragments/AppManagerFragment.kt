package com.example.aicleaner.ui.fragments

import android.app.usage.StorageStats
import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager.UUID_DEFAULT
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aicleaner.R
import com.example.aicleaner.databinding.FragmentAppManagerBinding
import com.example.aicleaner.models.AppManagerModel
import com.example.aicleaner.ui.adapters.AppManagerItemAdapter
import com.example.aicleaner.viewmodels.PhotoCleanViewModel

class AppManagerFragment : Fragment() {

    private var bindingAppManager: FragmentAppManagerBinding? = null
    private val photoCleanViewModel by lazy {
        ViewModelProvider(requireActivity())[PhotoCleanViewModel::class.java]
    }
    var listApps: MutableList<AppManagerModel>? =
        ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_app_manager, container, false)
        bindingAppManager = DataBindingUtil.bind(view)
        bindingAppManager?.backIcon?.setOnClickListener {
            findNavController().popBackStack()
        }
        bindingAppManager?.lottiePlayer?.visibility=View.VISIBLE
        getAllAppsPresentInOperatingSystem()
        return view
    }

    private fun getAllAppsPresentInOperatingSystem() {
        photoCleanViewModel?.getAllApps(requireContext())
        photoCleanViewModel?.listOfApps?.observe(viewLifecycleOwner, Observer {
           if (!it.isNullOrEmpty())
           {
               it.toMutableList()?.let { it1 -> setUpAdapter(it1) }
           }
            else
           {
               bindingAppManager?.lottiePlayer?.visibility=View.GONE
           }
        })
         // Usage example:
//        if (checkUsageStatsPermission(context)) {
//            val cacheSizes = getAppCacheSize(requireContext())
//            for ((packageName, cacheSize) in cacheSizes) {
//                Log.d("CacheSize", "Package: $packageName, Cache Size: $cacheSize bytes")
//            }
//        } else {
//            requestUsageStatsPermission(this)
//        }
    }
//
//    private fun checkUsageStatsPermission(context: Context?): Boolean {
//
//    }

//    fun getAppCacheSize(context: Context): Map<String, Long> {
//        val packageManager = context.packageManager
//        val storageStatsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
//        } else {
//            return emptyMap()
//        }
//
//        val appCacheSizes = mutableMapOf<String, Long>()
//
//        for (packageName in getInstalledPackages(context)) {
//            try {
//                val uid = packageManager.getPackageUid(packageName, 0)
//                val storageStats: StorageStats
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    storageStats = storageStatsManager.queryStatsForUid(UUID_DEFAULT, uid)
//                    appCacheSizes[packageName] = storageStats.cacheBytes
//                }
//            } catch (e: Exception) {
//                Log.d("CacheSize", "Error retrieving cache size for package $packageName", e)
//            }
//        }
//
//        return appCacheSizes
//    }


    private fun setUpAdapter(listApps: MutableList<AppManagerModel?>) {
        bindingAppManager?.lottiePlayer?.visibility=View.GONE
        val adapter = AppManagerItemAdapter(requireContext())
        bindingAppManager?.appManagerRecycler?.layoutManager =
            LinearLayoutManager(context)
        bindingAppManager?.appManagerRecycler?.adapter = adapter
        adapter?.submitList(listApps)

    }

}