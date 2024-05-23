package com.example.aicleaner.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
import com.example.aicleaner.databinding.DeleteVideoFragmentBinding
import com.example.aicleaner.models.PictureData
import com.example.aicleaner.models.VideoFileData
import com.example.aicleaner.ui.adapters.PhotoItemAdapter
import com.example.aicleaner.ui.adapters.VideoItemAdapter
import com.example.aicleaner.utils.interfaces.VideoClicked
import com.example.aicleaner.viewmodels.VideoCleanViewModel
import java.math.BigDecimal
import java.math.RoundingMode

private const val DELETE_PERMISSION_REQUEST = 0x1033

class DeleteVideoFragment:Fragment(),VideoClicked {

    private var bindingDeleteVideo:DeleteVideoFragmentBinding?=null
    private val videoCleanViewModel by lazy {
        ViewModelProvider(requireActivity())[VideoCleanViewModel::class.java]
    }
    private var imageCount = 0

    private var videoFolderPath=""
    private var folderName=""
    var adapter: VideoItemAdapter? = null
    var deletedVideosArray = ArrayList<VideoFileData>()
    var totalSize = BigDecimal(0.0)
    var selectAll = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layDelete = inflater.inflate(R.layout.delete_video_fragment,container,false)
        bindingDeleteVideo=DataBindingUtil.bind(layDelete)
        bindingDeleteVideo?.lifecycleOwner=this

        getAllNavigationArgs()
        bindingDeleteVideo?.bottomDeleteLay?.setOnClickListener {
            showSelectedImageArray()
        }
        bindingDeleteVideo?.selectAllOption?.setOnClickListener {
            if (selectAll == 0) {
                bindingDeleteVideo?.allImageSelection?.setImageResource(R.drawable.check_circle)
                selectAllImages()
                selectAll += 1
            } else {
                bindingDeleteVideo?.allImageSelection?.setImageResource(R.drawable.check_circle_empty)
                deselectAllImages()
                selectAll = 0
            }
        }

        videoCleanViewModel.permissionNeededForDelete.observe(
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
        return layDelete
    }

    private fun showSelectedImageArray() {
        if (!deletedVideosArray.isNullOrEmpty()) {
            videoCleanViewModel.deleteImage(deletedVideosArray)
        }
    }
    private fun deselectAllImages() {
        videoCleanViewModel?.videoList?.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                it.forEach { photo ->
                    photo?.videoSelected = false
                }
                bindingDeleteVideo?.videoCollectionRecycler?.layoutManager =
                    GridLayoutManager(requireContext(), 5)
                adapter?.submitList(it.toList())
                bindingDeleteVideo?.textView?.setText("Delete")
                deletedVideosArray?.clear()
            }
            else
            {
                bindingDeleteVideo?.emptyFolder?.visibility=View.VISIBLE
                bindingDeleteVideo?.videoCollectionRecycler?.visibility=View.GONE
                bindingDeleteVideo?.sizeMb?.text="O Mb"
                bindingDeleteVideo?.items?.text="0 items"
                bindingDeleteVideo?.allImageSelection?.setImageResource(R.drawable.check_circle_empty)
            }

        })
    }

    private fun selectAllImages() {
        videoCleanViewModel?.videoList?.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                it.forEach { photo ->
                    photo?.videoSelected = true
                }
                bindingDeleteVideo?.videoCollectionRecycler?.layoutManager =
                    GridLayoutManager(requireContext(), 5)
                adapter?.submitList(it.toList())
                deletedVideosArray?.clear()
                val arrayList = ArrayList(it)

                deletedVideosArray = arrayList
                bindingDeleteVideo?.textView?.text =
                    ("Delete " + "(" + it.size + "items," + bindingDeleteVideo?.sizeMb?.text + ")")
            }
            else
            {
                bindingDeleteVideo?.emptyFolder?.visibility=View.VISIBLE
                bindingDeleteVideo?.videoCollectionRecycler?.visibility=View.GONE
                bindingDeleteVideo?.sizeMb?.text="O Mb"
                bindingDeleteVideo?.items?.text="0 items"
                bindingDeleteVideo?.allImageSelection?.setImageResource(R.drawable.check_circle_empty)
            }
        })
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == DELETE_PERMISSION_REQUEST) {
            videoCleanViewModel.deletePendingImage(deletedVideosArray)
            videoCleanViewModel?.getAllVideosWithPath(requireContext(), videoFolderPath)
            deletedVideosArray.clear()
            bindingDeleteVideo?.textView?.setText("Delete")
        }
    }
    private fun getAllNavigationArgs() {

        val videoData: DeleteVideoFragmentArgs by navArgs()
        if (videoData.folderPath != null) {
            videoFolderPath = videoData.folderPath.toString()
        }
        if (videoData.folderName != null) {
            folderName = videoData.folderName.toString()
        }
        if (videoData?.folderSize != null) {
            bindingDeleteVideo?.sizeMb?.text = videoData?.folderSize + "MB"
        }

        showAllVideosInParticularFolder()
    }

    //Show all videos in particular folder....
    private fun showAllVideosInParticularFolder() {
        videoCleanViewModel?.getAllVideosWithPath(requireContext(),videoFolderPath)
        videoCleanViewModel?.isLoading?.observe(viewLifecycleOwner, Observer {
            if (it){
                bindingDeleteVideo?.videoCollectionRecycler?.visibility=View.GONE
                bindingDeleteVideo?.lottiePlayer?.visibility=View.VISIBLE
            }
            else
            {
                bindingDeleteVideo?.lottiePlayer?.visibility=View.GONE
                bindingDeleteVideo?.videoCollectionRecycler?.visibility=View.VISIBLE

            }
        })
        videoCleanViewModel?.videoList?.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                setUpVideosAdapter(it)
            }
            else
            {
                bindingDeleteVideo?.emptyFolder?.visibility=View.VISIBLE
                bindingDeleteVideo?.videoCollectionRecycler?.visibility=View.GONE
                bindingDeleteVideo?.sizeMb?.text="O Mb"
                bindingDeleteVideo?.items?.text="0 items"
                bindingDeleteVideo?.allImageSelection?.setImageResource(R.drawable.check_circle_empty)
            }
        })
    }

    private fun setUpVideosAdapter(it: List<VideoFileData>) {
        bindingDeleteVideo?.items?.text = it.size.toString() + " items"
        adapter = VideoItemAdapter(requireContext(), this)
        bindingDeleteVideo?.videoCollectionRecycler?.layoutManager =
            GridLayoutManager(requireContext(), 5)
        bindingDeleteVideo?.videoCollectionRecycler?.adapter = adapter
        adapter?.submitList(it)
    }

    override fun videoItem(videoFileData: VideoFileData) {
        if (videoFileData.videoSelected == true) {
            if (!deletedVideosArray.contains(videoFileData)) {
                imageCount = imageCount + 1
                deletedVideosArray.add(videoFileData)
                changeCountsAndMbS(imageCount, videoFileData.videoSize, true)
            }
        } else {
            if (deletedVideosArray.contains(videoFileData)) {
                deletedVideosArray.remove(videoFileData)
                imageCount = imageCount - 1
                changeCountsAndMbS(imageCount, videoFileData.videoSize, false)

            }
        }
    }

    private fun changeCountsAndMbS(imageCount: Int, picSize: BigDecimal?, increase: Boolean) {
        val compare = BigDecimal(0.0)

        val fileSize: BigDecimal? = picSize
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
            bindingDeleteVideo?.textView?.text =
                ("Delete " + "(" + imageCount + "items," + totalSize + "MB" + ")")
        } else {
            bindingDeleteVideo?.textView?.text = "Delete"
        }
    }



}