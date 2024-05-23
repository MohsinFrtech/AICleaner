package com.example.aicleaner.ui.fragments

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.aicleaner.R
import com.example.aicleaner.databinding.DeletePhotoFragmentBinding
import com.example.aicleaner.models.PictureData
import com.example.aicleaner.ui.adapters.PhotoItemAdapter
import com.example.aicleaner.utils.interfaces.PhotoClicked
import com.example.aicleaner.viewmodels.PhotoCleanViewModel
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Collections

private const val DELETE_PERMISSION_REQUEST = 0x1033

class DeletePhotoFragment : Fragment(), PhotoClicked {

    private val photoCleanViewModel by lazy {
        ViewModelProvider(requireActivity())[PhotoCleanViewModel::class.java]
    }

    private var bindingDelete: DeletePhotoFragmentBinding? = null
    private var folderPath = ""
    private var folderName = ""
    private var imageCount = 0
    var selectAll = 0
    var deletedImagesArray = ArrayList<PictureData>()
    var totalSize = BigDecimal(0.0)
    var totalSizeInMb = BigDecimal(0.0)
    var adapter: PhotoItemAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val deleteLay = inflater.inflate(R.layout.delete_photo_fragment, container, false)
        bindingDelete = DataBindingUtil.bind(deleteLay)
        bindingDelete?.lifecycleOwner=this
        imageCount = 0
        totalSize = BigDecimal(0.0)
        getNavArgs()
        photoCleanViewModel.permissionNeededForDelete.observe(
            viewLifecycleOwner,
            Observer { intentSender ->

                intentSender?.let {
                    // On Android 10+, if the app doesn't have permission to modify
                    // or delete an item, it returns an `IntentSender` that we can
                    // use here to prompt the user to grant permission to delete (or modify)
                    // the image.
                    startIntentSenderForResult(
                        intentSender,
                        DELETE_PERMISSION_REQUEST,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                }
            })
        bindingDelete?.bottomDeleteLay?.setOnClickListener {
            showSelectedImageArray()
        }
        bindingDelete?.selectAllOption?.setOnClickListener {
            if (selectAll == 0) {
                bindingDelete?.allImageSelection?.setImageResource(R.drawable.check_circle)
                selectAllImages()
                selectAll += 1
            } else {
                bindingDelete?.allImageSelection?.setImageResource(R.drawable.check_circle_empty)
                deselectAllImages()
                selectAll = 0
            }
        }

        return deleteLay
    }

    private fun deselectAllImages() {
        photoCleanViewModel?.imagesList?.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                it.forEach { photo ->
                    photo?.picSelected = false
                }
                bindingDelete?.photoCollectionRecycler?.layoutManager =
                    GridLayoutManager(requireContext(), 5)
                adapter?.submitList(it.toList())
                bindingDelete?.textView?.setText("Delete")
                deletedImagesArray?.clear()
            }
            else
            {
                bindingDelete?.emptyFolder?.visibility=View.VISIBLE
                bindingDelete?.photoCollectionRecycler?.visibility=View.GONE
                bindingDelete?.sizeMb?.text="O Mb"
                bindingDelete?.items?.text="0 items"
                bindingDelete?.allImageSelection?.setImageResource(R.drawable.check_circle_empty)
            }

        })
    }

    private fun selectAllImages() {
        photoCleanViewModel?.imagesList?.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                it.forEach { photo ->
                    photo?.picSelected = true
                }
                bindingDelete?.photoCollectionRecycler?.layoutManager =
                    GridLayoutManager(requireContext(), 5)
                adapter?.submitList(it.toList())
                deletedImagesArray?.clear()
                val arrayList = ArrayList(it)

                deletedImagesArray = arrayList
                bindingDelete?.textView?.text =
                    ("Delete " + "(" + it.size + "items," + bindingDelete?.sizeMb?.text + ")")
            }
            else
            {
                bindingDelete?.emptyFolder?.visibility=View.VISIBLE
                bindingDelete?.photoCollectionRecycler?.visibility=View.GONE
                bindingDelete?.sizeMb?.text="O Mb"
                bindingDelete?.items?.text="0 items"
                bindingDelete?.allImageSelection?.setImageResource(R.drawable.check_circle_empty)
            }
        })
    }

    //
    private fun showSelectedImageArray() {
        if (!deletedImagesArray.isNullOrEmpty()) {
            photoCleanViewModel.deleteImage(deletedImagesArray)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == DELETE_PERMISSION_REQUEST) {
            photoCleanViewModel.deletePendingImage(deletedImagesArray)
            photoCleanViewModel?.getAllImagesOfFolder(requireContext(), folderPath)
            deletedImagesArray.clear()
            bindingDelete?.textView?.setText("Delete")
        }
    }

    private fun getNavArgs() {
        val photoData: DeletePhotoFragmentArgs by navArgs()
        if (photoData.path != null) {
            folderPath = photoData.path.toString()
        }
        if (photoData.foldername != null) {
            folderName = photoData.foldername.toString()
        }
        if (photoData?.size != null) {
            bindingDelete?.sizeMb?.text = photoData?.size + "MB"
        }

        getAllImagesInParticularFolder(folderPath)

    }

    private fun getAllImagesInParticularFolder(folderPath: String) {
        photoCleanViewModel?.getAllImagesOfFolder(requireContext(), folderPath)
        photoCleanViewModel?.isLoading?.observe(viewLifecycleOwner, Observer {
            if (it){
                bindingDelete?.photoCollectionRecycler?.visibility=View.GONE
                bindingDelete?.lottiePlayer?.visibility=View.VISIBLE
            }
            else
            {
                bindingDelete?.photoCollectionRecycler?.visibility=View.VISIBLE
                bindingDelete?.lottiePlayer?.visibility=View.GONE
            }
        })
        photoCleanViewModel?.imagesList?.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                setUpImagesAdapter(it)
            }
            else
            {
                bindingDelete?.emptyFolder?.visibility=View.VISIBLE
                bindingDelete?.photoCollectionRecycler?.visibility=View.GONE
                bindingDelete?.sizeMb?.text="O Mb"
                bindingDelete?.items?.text="0 items"
                bindingDelete?.allImageSelection?.setImageResource(R.drawable.check_circle_empty)
            }
        })
    }

    private fun setUpImagesAdapter(it: List<PictureData?>) {
        bindingDelete?.items?.text = it.size.toString() + " items"
        adapter = PhotoItemAdapter(requireContext(), this)
        bindingDelete?.photoCollectionRecycler?.layoutManager =
            GridLayoutManager(requireContext(), 5)
        bindingDelete?.photoCollectionRecycler?.adapter = adapter
        adapter?.submitList(it)
    }

    override fun photoItem(pictureData: PictureData) {
        if (pictureData.picSelected == true) {
            if (!deletedImagesArray.contains(pictureData)) {
                imageCount = imageCount + 1
                deletedImagesArray.add(pictureData)
                changeCountsAndMbS(imageCount, pictureData.picSize, true)
            }
        } else {
            if (deletedImagesArray.contains(pictureData)) {
                deletedImagesArray.remove(pictureData)
                imageCount = imageCount - 1
                changeCountsAndMbS(imageCount, pictureData.picSize, false)

            }
        }
//        photoCleanViewModel?.deleteImage(pictureData)
//        deleteImage(pictureData.picPath)
    }

    private fun changeCountsAndMbS(imageCount: Int, picSize: String?, increase: Boolean) {
        val sizeInDouble = picSize?.toBigDecimal()
        val compare = BigDecimal(0.0)

        val a = BigDecimal(1024)
        val b = BigDecimal(1024)
        val fileSize: BigDecimal? =
            (sizeInDouble
                ?.divide(a.multiply(b), 2, RoundingMode.HALF_UP))
        if (fileSize != null) {
            if (increase == true) {
                totalSize = totalSize.plus(fileSize)
            } else {
                if (totalSize > compare) {
                    if (totalSize >= fileSize) {
                        totalSize = totalSize.minus(fileSize)
                    }
                }
            }
        }
        if (imageCount > 0) {
            bindingDelete?.textView?.text =
                ("Delete " + "(" + imageCount + "items," + totalSize + "MB" + ")")
        } else {
            bindingDelete?.textView?.text = "Delete"
        }
    }

    private fun deleteImage(picPath: String?) {
        val file = File(picPath) //PATH is: /storage/sdcard0/DCIM/Camera/IMG_20160913_165933.jpg


        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection = MediaStore.Images.Media.DATA + " = ?"
        val selectionArgs = arrayOf<String>(file.getAbsolutePath())
        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val contentResolver: ContentResolver = requireActivity().contentResolver
        val c = contentResolver.query(queryUri, projection, selection, selectionArgs, null)
        if (c!!.moveToFirst()) {
            // We found the ID. Deleting the item via the content provider will also remove the file
            val id = c!!.getLong(c!!.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
            val deleteUri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            contentResolver.delete(deleteUri, null, null)
        } else {
            // File not found in media store DB
        }
        c!!.close()
    }

}