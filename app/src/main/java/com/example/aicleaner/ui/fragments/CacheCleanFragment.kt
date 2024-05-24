package com.example.aicleaner.ui.fragments

import android.Manifest
import android.app.Activity
import android.app.usage.StorageStats
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager.UUID_DEFAULT
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.layout.LookaheadLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aicleaner.R
import com.example.aicleaner.databinding.CacheCleanFragmentBinding
import com.example.aicleaner.databinding.FragmentAppManagerBinding
import com.example.aicleaner.models.AppManagerModel
import com.example.aicleaner.models.CacheCleanModel
import com.example.aicleaner.ui.adapters.AppCacheAdapterAdapter
import com.example.aicleaner.ui.adapters.AppManagerItemAdapter
import com.example.aicleaner.ui.adapters.FolderItemAdapter
import com.example.aicleaner.utils.Constants.permissionGranted
import com.example.aicleaner.utils.interfaces.AppCacheItemClick
import com.example.aicleaner.viewmodels.CacheCleanViewModel
import com.example.aicleaner.viewmodels.PhotoCleanViewModel
import java.math.BigDecimal
import java.text.DecimalFormat

class CacheCleanFragment : Fragment(),AppCacheItemClick {

    private var bindingCacheClean: CacheCleanFragmentBinding? = null
    private val cacheCleanViewModel by lazy {
        ViewModelProvider(requireActivity())[CacheCleanViewModel::class.java]
    }
    var totalSize = BigDecimal(0.0)
    var totalSizeAll = BigDecimal(0.0)

    var listApps: MutableList<AppManagerModel>? =
        ArrayList()
    val df = DecimalFormat("0.000")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.cache_clean_fragment, container, false)
        bindingCacheClean = DataBindingUtil.bind(view)
        bindingCacheClean?.lifecycleOwner=this
        bindingCacheClean?.backIcon?.setOnClickListener {
            findNavController().popBackStack()
        }


        checkPermissionIfGranted()
        getAllAppsWithCacheSize()
        return view
    }

    private fun getAllAppsWithCacheSize() {
//        cacheCleanViewModel?.listSystemTempFiles()
//        cacheCleanViewModel?.listAppCacheFiles(requireContext())
        cacheCleanViewModel?.isLoading?.observe(viewLifecycleOwner, Observer {
            if (it){
                bindingCacheClean?.lottiePlayer?.visibility=View.VISIBLE

            }
            else
            {
                bindingCacheClean?.lottiePlayer?.visibility=View.GONE

            }
        })
        cacheCleanViewModel.appsCacheList.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()){
                bindingCacheClean?.expectedSize?.text = df.format(cacheCleanViewModel?.calculateTotalCache)
                totalSizeAll = df.format(cacheCleanViewModel?.calculateTotalCache).toBigDecimal()
                setUpCacheItemAdapter(it)
            }
        })
    }

    private fun setUpCacheItemAdapter(it: List<CacheCleanModel?>) {
        val adapter = AppCacheAdapterAdapter(requireContext(),this)
        bindingCacheClean?.appManagerRecycler?.layoutManager =
            LinearLayoutManager(context)
        bindingCacheClean?.appManagerRecycler?.adapter = adapter
        adapter?.submitList(it)
    }

    private fun checkPermissionIfGranted() {
        if (permissionGranted) {
            cacheCleanViewModel.getAppsCache(requireContext())

        } else {

//            requireActivity().startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            val startForResult = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                cacheCleanViewModel.getAppsCache(requireContext())
                if (result.resultCode == Activity.RESULT_CANCELED) {
                    //  you will get result here in result.data
                    permissionGranted=true
                }
                else
                {
                    permissionGranted=true
                }
            }

            startForResult.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

    override fun cacheClick(model: CacheCleanModel) {
        if (model.cacheAppSelected == true) {
            changeCountsAndMbS( model.appCacheSize?.toBigDecimal(), true)
        } else {
            changeCountsAndMbS( model.appCacheSize?.toBigDecimal(), false)
        }

    }
    private fun changeCountsAndMbS(picSize: BigDecimal?, increase: Boolean) {
        val compare = BigDecimal(0.0)

        val fileSize: BigDecimal? = picSize
        if (fileSize != null) {
            if (increase == true) {
                totalSizeAll = totalSizeAll.plus(fileSize)
            } else {
                if (totalSizeAll > compare) {
                    if (totalSizeAll >= fileSize) {
                        totalSizeAll = totalSizeAll.minus(fileSize)
                    }
                }
            }
        }
       bindingCacheClean?.expectedSize?.text = totalSizeAll.toString()
    }


}