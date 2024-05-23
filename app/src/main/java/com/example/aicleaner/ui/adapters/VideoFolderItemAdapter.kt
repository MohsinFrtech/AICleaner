package com.example.aicleaner.ui.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.aicleaner.R
import com.example.aicleaner.databinding.AppManagerItemBinding
import com.example.aicleaner.databinding.PhotoItemLayoutBinding
import com.example.aicleaner.models.ImageFolder
import com.example.aicleaner.models.VideoFolder
import com.example.aicleaner.ui.fragments.PhotoCleanFragmentDirections
import com.example.aicleaner.ui.fragments.VideoFragmentDirections
import com.example.aicleaner.utils.interfaces.NavigateData


class VideoFolderItemAdapter(
    val context: Context,
    private val navigateData: NavigateData
) :
    ListAdapter<VideoFolder, VideoFolderItemAdapter.VideoViewHolder>(
        LiveSliderAdapterDiffUtilCallback
    ) {


    class VideoViewHolder(
        private var binding: PhotoItemLayoutBinding,
        private var context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bindSeriesData(folder: VideoFolder) {
//            binding?.model = seriesScoresModel
            binding?.folderName?.text = folder.videoFolderName
            binding?.picCount?.text = folder.numberOfVideos.toString()
            binding?.sizeInMb?.text = folder.videoFolderSize.toString() + " mb"

            binding?.appIcon?.setImageBitmap(folder.videothumbnail)
//            Glide.with(context)
//                .load(folder.firstPic)
//                .into(binding.appIcon)
            binding?.executePendingBindings()

        }

    }

    object LiveSliderAdapterDiffUtilCallback : DiffUtil.ItemCallback<VideoFolder>() {

        override fun areItemsTheSame(
            oldItem: VideoFolder,
            newItem: VideoFolder
        ): Boolean {
            return oldItem.videoFolderName == newItem.videoFolderName
        }

        override fun areContentsTheSame(
            oldItem: VideoFolder,
            newItem: VideoFolder
        ): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding: PhotoItemLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.photo_item_layout, parent, false
        )
        return VideoViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bindSeriesData(currentList[position])
        holder.itemView.setOnClickListener {
            val direction =
                VideoFragmentDirections.actionVideoFragmentToVideoDeleteFragment(
                    currentList[position].path,
                    currentList[position].videoFolderName,
                    currentList[position].videoFolderSize.toString()
                )
            navigateData.navigation(direction)
        }

    }


}