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
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aicleaner.models.AppManagerModel
import com.example.aicleaner.models.ImageFolder
import com.example.aicleaner.models.PictureData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode


class PhotoCleanViewModel(application: Application) : AndroidViewModel(application) {

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.IO)
    private val _imageFoldersList = MutableLiveData<List<ImageFolder?>>()
    val imageFoldersList: LiveData<List<ImageFolder?>>
        get() = _imageFoldersList
    private val _listOfApps = MutableLiveData<List<AppManagerModel?>?>()
    val listOfApps: LiveData<List<AppManagerModel?>?>
        get() = _listOfApps

    private val _imagesList = MutableLiveData<List<PictureData>>()

    private val _totalCache = MutableLiveData<String>()

    val totalCache: LiveData<String> get() = _totalCache

    val imagesList: LiveData<List<PictureData>>
        get() = _imagesList
    private var pendingDeleteImage: List<PictureData?>? = null

    private val _permissionNeededForDelete = MutableLiveData<IntentSender?>()
    val permissionNeededForDelete: LiveData<IntentSender?> = _permissionNeededForDelete
    fun getImageFoldersInDevice(context: Context) {
        coroutineScope.launch {
            val imagePathArray = ArrayList<String>()
            val imageFoldersArray = ArrayList<ImageFolder>()

            val allImagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.SIZE
            )


            val cursor: Cursor? =
                context.contentResolver.query(allImagesUri, projection, null, null, null)
            cursor.use { cursorObj ->
                try {
                    if (cursorObj != null) {
                        cursorObj.moveToFirst()
                    } else {

                    }
                    if (cursorObj != null) {

                        do {
                            val name =
                                cursor?.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
                            val folderName =
                                cursor?.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))
                            val dataPath =
                                cursor?.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                            val folderSize =
                                cursor?.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))
                            val a = BigDecimal(1024)
                            val b = BigDecimal(1024)
                            val fileSize: BigDecimal? =
                                (folderSize?.toBigDecimal()
                                    ?.divide(a.multiply(b), 2, RoundingMode.HALF_UP))


                            var folderpaths: String? = ""
                            try {
                                folderpaths =
                                    dataPath?.substring(0, dataPath.lastIndexOf(folderName + "/"))

                            } catch (e: Exception) {

                            }

                            folderpaths = "$folderpaths$folderName/"

                            if (!imagePathArray.contains(folderpaths)) {
                                imagePathArray.add(folderpaths)
                                val count = 1
                                val imageFolder =
                                    ImageFolder(folderpaths, folderName, count, dataPath, fileSize)
                                imageFoldersArray.add(imageFolder)
                            } else {

                                for (i in 0 until imageFoldersArray.size) {
                                    if (imageFoldersArray.get(i).path.equals(
                                            folderpaths,
                                            true
                                        )
                                    ) {
                                        imageFoldersArray.get(i).numberOfPics =
                                            imageFoldersArray.get(i).numberOfPics + 1
                                        imageFoldersArray.get(i).folderSize = fileSize?.let {
                                            imageFoldersArray.get(i).folderSize?.plus(
                                                it
                                            )
                                        }

                                        break
//                                        imageFoldersArray.set(
//                                            i,
//                                            ImageFolder(
//                                                folderPaths,
//                                                folderName,
//                                                imageFoldersArray.get(i).numberOfPics + 1,
//                                                dataPath
//                                            )
//                                        )
                                    }
                                }
                            }


                        } while (cursorObj.moveToNext())

                        withContext(Dispatchers.Main) {
                            _imageFoldersList.value = imageFoldersArray
                        }

                        cursorObj?.close()
                    } else {

                    }


                } catch (e: Exception) {
                    Log.d("ExceptionIn", "msg" + e.message)
                }
            }

        }
    }

    fun getAllImagesOfFolder(context: Context, folderPath: String) {
        coroutineScope.launch {
            val imagesArray = ArrayList<PictureData>()

            val allImagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE
            )


            val cursor: Cursor? =
                context.contentResolver.query(
                    allImagesUri,
                    projection,
                    MediaStore.Images.Media.DATA + " like ? ",
                    arrayOf<String>("%$folderPath%"),
                    null
                )
            cursor.use { cursorObj ->
                try {
                    if (cursorObj != null) {
                        cursorObj.moveToFirst()
                    } else {

                    }
                    if (cursorObj != null) {

                        do {
                            val idColumn =
                                cursor?.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                            val name =
                                cursor?.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
                            val dataPath =
                                cursor?.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                            val pictureSize =
                                cursor?.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))
                            val id = idColumn?.let { cursor?.getLong(it) }

                            val contentUri = id?.let {
                                ContentUris.withAppendedId(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    it
                                )
                            }
                            val pictureData =
                                PictureData(id, contentUri, name, dataPath, pictureSize, "", false)
                            imagesArray.add(pictureData)
                        } while (cursorObj.moveToNext())

                        withContext(Dispatchers.Main) {
                            _imagesList.value = imagesArray
                        }

                        cursorObj?.close()
                    } else {

                        withContext(Dispatchers.Main) {
                            _imagesList.value = imagesArray
                        }
                        cursorObj?.close()
                    }


                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        _imagesList.value = imagesArray
                    }
                    cursorObj?.close()
                    Log.d("ExceptionIn", "msg" + e.message)
                }
            }

        }
    }

    private fun checkSecurityExceptionEitherGrantedOrNot() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            _permissionNeededForDelete.postValue(
//                recoverableSecurityException.userAction.actionIntent.intentSender
//            )
//        } else {
//
//        }
    }

    private suspend fun performDeleteImage(imageList2: List<PictureData?>?) {
        withContext(Dispatchers.IO) {

            val arrayList2: ArrayList<Uri?> = ArrayList()
            if (imageList2 != null) {
                for (iab in imageList2) {
                    arrayList2.add(iab?.contentUri)
                }
            }

            if (imageList2 != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        /**
                         * In [Build.VERSION_CODES.Q] and above, it isn't possible to modify
                         * or delete items in MediaStore directly, and explicit permission
                         * must usually be obtained to do this.
                         *
                         * The way it works is the OS will throw a [RecoverableSecurityException],
                         * which we can catch here. Inside there's an [IntentSender] which the
                         * activity can use to prompt the user to grant permission to the item
                         * so it can be either updated or deleted.
                         */
                        imageList2.get(0)?.contentUri?.let {
                            getApplication<Application>().contentResolver.delete(
                                it,
                                "${MediaStore.Images.Media._ID} = ?",
                                arrayOf(imageList2.get(0)?.id.toString())
                            )
                        }
                    } catch (securityException: SecurityException) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val recoverableSecurityException =
                                securityException as? RecoverableSecurityException
                                    ?: throw securityException

                            // Signal to the Activity that it needs to request permission and
                            // try the delete again if it succeeds.
                            pendingDeleteImage = imageList2
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                //                               val collec=Collections.addAll(imageList2.toMutableList())
                                val intentSender = MediaStore.createDeleteRequest(
                                    getApplication<Application>().contentResolver,
                                    arrayList2
                                ).intentSender

                                _permissionNeededForDelete.postValue(
                                    intentSender
                                )
                            } else {
                                _permissionNeededForDelete.postValue(
                                    recoverableSecurityException.userAction.actionIntent.intentSender
                                )
                            }

                        } else {
                            //                        throw securityException
                        }
                    }
                } else {
                    for (image in imageList2) {
                        try {
                            /**
                             * In [Build.VERSION_CODES.Q] and above, it isn't possible to modify
                             * or delete items in MediaStore directly, and explicit permission
                             * must usually be obtained to do this.
                             *
                             * The way it works is the OS will throw a [RecoverableSecurityException],
                             * which we can catch here. Inside there's an [IntentSender] which the
                             * activity can use to prompt the user to grant permission to the item
                             * so it can be either updated or deleted.
                             */
                            image?.contentUri?.let {
                                getApplication<Application>().contentResolver.delete(
                                    it,
                                    "${MediaStore.Images.Media._ID} = ?",
                                    arrayOf(image.id.toString())
                                )
                            }
                        } catch (securityException: SecurityException) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                val recoverableSecurityException =
                                    securityException as? RecoverableSecurityException
                                        ?: throw securityException

                                // Signal to the Activity that it needs to request permission and
                                // try the delete again if it succeeds.
                                pendingDeleteImage = imageList2
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    //                               val collec=Collections.addAll(imageList2.toMutableList())
                                    val intentSender = MediaStore.createDeleteRequest(
                                        getApplication<Application>().contentResolver,
                                        arrayList2
                                    ).intentSender

                                    _permissionNeededForDelete.postValue(
                                        intentSender
                                    )
                                } else {
                                    _permissionNeededForDelete.postValue(
                                        recoverableSecurityException.userAction.actionIntent.intentSender
                                    )
                                }

                            } else {
                                //                        throw securityException
                            }
                        }
                    }
                }

            }

        }
    }

    fun deletePendingImage(deletedImagesArray: ArrayList<PictureData>) {

        pendingDeleteImage?.let { image ->
            pendingDeleteImage = null
            deleteImage(pendingDeleteImage)
        }
    }


    private suspend fun getAllAppsPresentInOperatingSystem(context: Context) {
        withContext(Dispatchers.IO) {
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
            withContext(Dispatchers.Main) {
                _listOfApps.value = listApps
            }
        }
    }

     fun getSpace(context: Context){
        viewModelScope.launch {
            getCache(context)
        }
    }

    private suspend fun getCache(context: Context) {
        withContext(Dispatchers.IO){
            try {
                val cacheSizeInBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    getCacheSize(context)
                } else {
                    0L // Handle cases for API level below 26 if needed
                }
                val cacheSizeInMB = bytesToMB(cacheSizeInBytes)
                withContext(Dispatchers.Main){
                    _totalCache.value = cacheSizeInMB.toString()
                }
            }
            catch (e:Exception){
                Log.d("CacheData",e.message.toString())
            }
        }
    }
    private fun bytesToMB(bytes: Long): Double {
        return bytes / (1024.0 * 1024.0)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCacheSize(context: Context): Long {
        var totalCacheSize: Long = 0
        val storageStatsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
        val packageManager = context.packageManager
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        Log.d("Apps",""+apps.size)

        val user = UserHandle.getUserHandleForUid(android.os.Process.myUid())

        apps.forEach { app ->
            try {
                val uuid = packageManager.getApplicationInfo(app.packageName, 0).storageUuid
                val storageStats: StorageStats = storageStatsManager.queryStatsForPackage(uuid, app.packageName, user)
                totalCacheSize += storageStats.cacheBytes
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return totalCacheSize
    }
    fun deleteImage(imageList: List<PictureData?>?) {
        viewModelScope.launch {
            performDeleteImage(imageList)
        }
    }

    fun getAllApps(context: Context) {
        viewModelScope.launch {
            getAllAppsPresentInOperatingSystem(context)
        }
    }
}