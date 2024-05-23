package com.example.aicleaner.viewmodels

import android.app.Application
import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.IntentSender
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.remember
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aicleaner.models.AppManagerModel
import com.example.aicleaner.models.PictureData
import com.example.aicleaner.models.VideoFileData
import com.example.aicleaner.models.VideoFolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.TimeUnit


class VideoCleanViewModel(application: Application) : AndroidViewModel(application) {

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.IO)
    private val _videoFoldersList = MutableLiveData<List<VideoFolder?>>()
    val videoFoldersList: LiveData<List<VideoFolder?>>
        get() = _videoFoldersList
    private val _listOfApps = MutableLiveData<List<AppManagerModel?>?>()
    val listOfApps: LiveData<List<AppManagerModel?>?>
        get() = _listOfApps

    private val _videoList = MutableLiveData<List<VideoFileData>>()
    val videoList: LiveData<List<VideoFileData>>
        get() = _videoList
    private var pendingDeleteImage: List<VideoFileData?>? = null

    private val _permissionNeededForDelete = MutableLiveData<IntentSender?>()
    val permissionNeededForDelete: LiveData<IntentSender?> = _permissionNeededForDelete
    val isLoading = MutableLiveData<Boolean>()

    var imageBitmap: Bitmap? = null
    fun getAllVideosFromGalleryWithFolders(context: Context) {
        viewModelScope.launch {
            getVideos(context)
        }
    }

    init {
        isLoading.value=false
    }
    private suspend fun getVideos(context: Context) {
        withContext(Dispatchers.IO) {
            val videoPathArray = ArrayList<String>()
            val videoFoldersArray = ArrayList<VideoFolder>()
            val contentResolver: ContentResolver = context.contentResolver
            // Projection for the videos query
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.SIZE
            )

            //Query to fetch Videos....
            val query = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

//            val query = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            // Sorting videos based on date added
            try {
                val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"
                contentResolver.query(query, projection, null, null, sortOrder)?.use { cursor ->

                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            if (cursor.getCount() > 0) {
//                                Log.d("BucketNames", "error" + cursor.count)
                            }
                            do {
                                val idColumn =
                                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                                val displayNameColumn =
                                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                                val dataColumn =
                                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
//                            val dateAddedColumn =
//                                cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION)
                                val bucketColumn =
                                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                                val videoSize =
                                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                                val size = cursor.getLong(videoSize)
                                val a = BigDecimal(1024)
                                val b = BigDecimal(1024)
                                val fileSize: BigDecimal? =
                                    (size?.toBigDecimal()
                                        ?.divide(a.multiply(b), 2, RoundingMode.HALF_UP))
                                val data = cursor.getString(dataColumn)
                                val bucket = cursor.getString(bucketColumn)
                                var folderpaths: String? = ""
                                try {
                                    folderpaths =
                                        data?.substring(0, data.lastIndexOf(bucket + "/"))

                                } catch (e: Exception) {

                                }

                                folderpaths = "$folderpaths$bucket/"
                                val id = cursor.getLong(idColumn)
                                val displayName = cursor.getString(displayNameColumn)

                                val contentUri = id?.let {
                                    ContentUris.withAppendedId(
                                        query,
                                        it
                                    )
                                }
                                val mediaMetadataRetriever = MediaMetadataRetriever()
                                mediaMetadataRetriever.setDataSource(context, contentUri)
                                val bitmap = mediaMetadataRetriever.frameAtTime
                                if (!videoPathArray.contains(folderpaths)) {
                                    videoPathArray.add(folderpaths)
                                    val count = 1
                                    val videoFolder =
                                        VideoFolder(
                                            folderpaths,
                                            bitmap,
                                            bucket,
                                            count,
                                            fileSize
                                        )
                                    videoFoldersArray.add(videoFolder)
                                } else {

                                    for (i in 0 until videoFoldersArray.size) {
                                        if (videoFoldersArray.get(i).path.equals(
                                                folderpaths,
                                                true
                                            )
                                        ) {
                                            videoFoldersArray.get(i).numberOfVideos =
                                                videoFoldersArray.get(i).numberOfVideos + 1
                                            videoFoldersArray.get(i).videoFolderSize =
                                                fileSize?.let {
                                                    videoFoldersArray.get(i).videoFolderSize?.plus(
                                                        it
                                                    )
                                                }

                                            break
                                        }
                                    }
                                }

                            } while (cursor.moveToNext())

                            withContext(Dispatchers.Main) {
                                _videoFoldersList.value = videoFoldersArray
                            }

                            cursor?.close()

                        } else {
                            Log.d("BucketNames", "cursor is empty")
                            withContext(Dispatchers.Main) {
                                _videoFoldersList.value = videoFoldersArray
                            }
                        }

                    } else {
                        withContext(Dispatchers.Main) {
                            _videoFoldersList.value = videoFoldersArray
                        }

                        cursor?.close()
                    }


                }
            } catch (e: Exception) {
                Log.d("ExceptionINNN", "msg" + e.message)
            }

        }
    }

    fun getAllVideosWithPath(context: Context, folderPath: String) {
        isLoading.value=true
        viewModelScope.launch {
            getAllVideosOfFolder(context, folderPath)
        }
    }

    private fun getAllVideosOfFolder(context: Context, folderPath: String) {

        coroutineScope.launch {

            val videosArray = ArrayList<VideoFileData>()
            val contentResolver: ContentResolver = context.contentResolver
            // Projection for the videos query
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.VideoColumns.DURATION,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.SIZE
            )

            //Query to fetch Videos....
            val query = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"
            val cursor: Cursor? =
                context.contentResolver.query(
                    query,
                    projection,
                    MediaStore.Video.Media.DATA + " like ? ",
                    arrayOf<String>("%$folderPath%"),
                    sortOrder
                )
            cursor.use { cursorObj ->
                try {
                    if (cursorObj != null) {
                        cursorObj.moveToFirst()
                    } else {
                        withContext(Dispatchers.Main) {
                            isLoading.value=false
                            _videoList.value = videosArray
                        }
                    }
                    if (cursorObj != null) {

                        do {
                            val idColumn = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                            val displayNameColumn =
                                cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                            val dataColumn =
                                cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                            val dateAddedColumn =
                                cursor?.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION)
                            val bucketColumn =
                                cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                            val videoSize =
                                cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                            val size = videoSize?.let { cursor.getLong(it) }
                            val a = BigDecimal(1024)
                            val b = BigDecimal(1024)
                            val fileSize: BigDecimal? =
                                (size?.toBigDecimal()
                                    ?.divide(a.multiply(b), 2, RoundingMode.HALF_UP))
                            val id = idColumn?.let { cursor?.getLong(it) }

                            val videoPath = dataColumn?.let { cursor?.getString(it) }
                            val contentUri = id?.let {
                                ContentUris.withAppendedId(
                                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                    it
                                )
                            }
                            val videoDuration = dateAddedColumn?.let { cursor?.getLong(it) }
                            val getVideoTime = videoDuration?.let {
                                convertMillisecondsToHMS(
                                    it
                                )
                            }
                            val mediaMetadataRetriever = MediaMetadataRetriever()
                            mediaMetadataRetriever.setDataSource(context, contentUri)
                            val bitmap = mediaMetadataRetriever.frameAtTime

                            val videoFileData =
                                VideoFileData(id, contentUri, videoPath, bitmap, getVideoTime,false,fileSize)
                            videosArray.add(videoFileData)

                        } while (cursor?.moveToNext() == true)

                        withContext(Dispatchers.Main) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                                mediaMetadataRetriever?.close()
                            }
                            isLoading.value=false
                            _videoList.value = videosArray
                        }

                        cursorObj?.close()
                    } else {

                        withContext(Dispatchers.Main) {
                            isLoading.value=false
                            _videoList.value = videosArray
                        }
                        cursorObj?.close()
                    }


                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        isLoading.value=false
                        _videoList.value = videosArray
                    }
                    cursorObj?.close()
                    Log.d("ExceptionIn", "msg" + e.message)
                }
            }

        }
    }
    private suspend fun performDeleteImage(imageList2: List<VideoFileData?>?) {
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

    fun deleteImage(imageList: List<VideoFileData?>?) {
        viewModelScope.launch {
            performDeleteImage(imageList)
        }
    }

//    private fun getVideoTime(videoDuration: String?): String {
//        if (videoDuration != null) {
//            try {
//                return String.format(
//                    "%d min, %d sec",
//                    TimeUnit.MILLISECONDS.toMinutes(videoDuration?.toLong()!!),
//                    TimeUnit.MILLISECONDS.toSeconds(videoDuration?.toLong()!!) -
//                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(videoDuration?.toLong()!!))
//                )
//            } catch (e: Exception) {
//                return ""
//            }
//
//        } else {
//            return ""
//        }
//
//    }

    private fun convertMillisecondsToHMS(milliseconds: Long): String {
        if (milliseconds > 0) {
            try {
                val seconds = (milliseconds / 1000).toInt()
                val minutes = seconds / 60
                val hours = minutes / 60

                val remainingMinutes = minutes % 60
                val remainingSeconds = seconds % 60
                if (hours > 0) {
                    return "$hours:$remainingMinutes:$remainingSeconds"
                } else {

                    return "$remainingMinutes:$remainingSeconds"
                }

            } catch (e: Exception) {
                return ""
                Log.d("Exception", "mdg")
            }

        } else {
            return ""
        }

    }
    fun deletePendingImage(deletedImagesArray: ArrayList<VideoFileData>) {

        pendingDeleteImage?.let { image ->
            pendingDeleteImage = null
            deleteImage(pendingDeleteImage)
        }
    }

    private fun getVideoDuration(videoPath: String): Int {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            val d = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            var duration = 0
            if (d != null) duration = d.toInt()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                retriever.close()
            }
            duration
        } catch (e: java.lang.Exception) {
            // Handle exception
            0
        }
    }
}