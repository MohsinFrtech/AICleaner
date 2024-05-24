package com.example.aicleaner.viewmodels

import android.app.Application
import android.app.RecoverableSecurityException
import android.app.usage.StorageStats
import android.app.usage.StorageStatsManager
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.UserHandle
import android.os.storage.StorageManager.UUID_DEFAULT
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aicleaner.models.AppManagerModel
import com.example.aicleaner.models.CacheCleanModel
import com.example.aicleaner.models.ImageFolder
import com.example.aicleaner.models.PictureData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.reflect.Proxy
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat


class CacheCleanViewModel(application: Application) : AndroidViewModel(application) {

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.IO)
    private val _appsCacheList = MutableLiveData<List<CacheCleanModel?>?>()
    val appsCacheList: LiveData<List<CacheCleanModel?>?>
        get() = _appsCacheList

    var calculateTotalCache=0.0
    val isLoading = MutableLiveData<Boolean>()

    init {
       isLoading.value=false
    }

    fun getAppsCache(context: Context) {
        isLoading.value=true
        viewModelScope.launch {
            getAllAppsWithCache(context)
        }

    }


    private suspend fun getAllAppsWithCache(context: Context) {
        coroutineScope.launch {
            val cacheSizes = getAppCacheSize(context)
            if (!cacheSizes.isNullOrEmpty()) {
                withContext(Dispatchers.Main) {
                    _appsCacheList.value = cacheSizes
                    isLoading.value=false

                }
            }

        }

    }

    fun listSystemTempFiles() {
        val tmpDirPath = System.getProperty("java.io.tmpdir")
        val tmpDir = File(tmpDirPath)
        val tmpFiles = tmpDir.listFiles()
        Log.d("TempFiles", "System Temp Directory: $tmpDirPath")
        tmpFiles?.forEach {
            Log.d("TempFiles", "File: ${it.name}, Size: ${it.length()} bytes")
        }
    }
    fun listAppCacheFiles(context: Context) {
        // List files in internal cache directory
        val internalCacheDir = context.cacheDir
        val internalFiles = internalCacheDir.listFiles()
        Log.d("TempFiles", "Internal Cache Directory: ${internalCacheDir.absolutePath}")
        internalFiles?.forEach {
            Log.d("TempFiles", "File: ${it.name}, Size: ${it.length()} bytes")
        }

        // List files in external cache directory
        val externalCacheDir = context.externalCacheDir
        externalCacheDir?.let {
            val externalFiles = it.listFiles()
            Log.d("TempFiles", "External Cache Directory: ${it.absolutePath}")
            externalFiles?.forEach { file ->
                Log.d("TempFiles", "File: ${file.name}, Size: ${file.length()} bytes")
            }
        }
    }

    private fun getAppCacheSize(context: Context): MutableList<CacheCleanModel>? {
        val packageManager = context.packageManager
        val storageStatsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
        } else {
            return mutableListOf()
        }
        var listAppsWithCache: MutableList<CacheCleanModel>? =
            ArrayList()
        val df = DecimalFormat("0.000")
        calculateTotalCache=0.0
        val appCacheSizes = mutableMapOf<String, Double>()
        if (!getInstalledPackages(context).isNullOrEmpty()) {
            var appCacheSize = 0.0
            for (appInfo in getInstalledPackages(context)!!) {
                try {
                    if (appInfo.appPackageName != null) {
                        val uid = packageManager.getPackageUid(appInfo.appPackageName!!, 0)
                        val storageStats: StorageStats
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            storageStats = storageStatsManager.queryStatsForUid(UUID_DEFAULT, uid)
                            if (storageStats.cacheBytes > 0) {
                                val cacheSizeInMb =
                                    storageStats.cacheBytes.toDouble().div(1024 * 1024)
                                appCacheSize = cacheSizeInMb
                                appCacheSizes[appInfo.appPackageName!!] = cacheSizeInMb
                            } else {
                                appCacheSize = storageStats.cacheBytes.toDouble()
                                appCacheSizes[appInfo.appPackageName!!] =
                                    storageStats.cacheBytes.toDouble()
                            }
                            val cacheCleanModel = CacheCleanModel(
                                appInfo.appName, appInfo.appIcon,
                                appInfo.appPackageName, df.format(appCacheSize)
                            )
                            calculateTotalCache += appCacheSize
                            listAppsWithCache?.add(cacheCleanModel)
                        }
                    }

                } catch (e: Exception) {
                    Log.d(
                        "CacheSize",
                        "Error retrieving cache size for package ${appInfo.appPackageName}",
                        e
                    )
                }
            }
        }


        return listAppsWithCache
    }

    private fun getInstalledPackages(context: Context): MutableList<AppManagerModel>? {
//        val pm = context.packageManager
//        return pm.getInstalledPackages(PackageManager.GET_META_DATA).map { it.packageName }
        var listApps: MutableList<AppManagerModel>? =
            ArrayList()
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        val resolvedInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            pm.queryIntentActivities(mainIntent, 0)
        }
        for (info in resolvedInfos) {
            val name = info.activityInfo.applicationInfo.loadLabel(pm).toString()
            val packageName = info.activityInfo.packageName
            val iconDrawable = info.activityInfo.loadIcon(pm)
            listApps?.add(AppManagerModel(name, iconDrawable, packageName))

        }
        return listApps
    }

}