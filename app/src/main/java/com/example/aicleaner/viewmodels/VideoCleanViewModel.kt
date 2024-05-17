package com.example.aicleaner.viewmodels

import android.app.Application
import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aicleaner.models.AppManagerModel
import com.example.aicleaner.models.ImageFolder
import com.example.aicleaner.models.PictureData
import com.example.aicleaner.models.VideoFolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode


class VideoCleanViewModel(application: Application) : AndroidViewModel(application) {

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.IO)
    private val _videoFoldersList = MutableLiveData<List<VideoFolder?>>()
    val videoFoldersList: LiveData<List<VideoFolder?>>
        get() = _videoFoldersList
    private val _listOfApps = MutableLiveData<List<AppManagerModel?>?>()
    val listOfApps: LiveData<List<AppManagerModel?>?>
        get() = _listOfApps

    private val _imagesList = MutableLiveData<List<PictureData>>()
    val imagesList: LiveData<List<PictureData>>
        get() = _imagesList
    private var pendingDeleteImage: List<PictureData?>? = null

    private val _permissionNeededForDelete = MutableLiveData<IntentSender?>()
    val permissionNeededForDelete: LiveData<IntentSender?> = _permissionNeededForDelete

    var imageBitmap: Bitmap? = null
    fun getAllVideosFromGalleryWithFolders(context: Context) {
        viewModelScope.launch {
            getVideos(context)
        }
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
                                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
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
                                            videoFoldersArray.get(i).videoFolderSize = fileSize?.let {
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
            }
            catch (e:Exception){
                Log.d("ExceptionINNN","msg"+e.message)
            }

        }
    }


}