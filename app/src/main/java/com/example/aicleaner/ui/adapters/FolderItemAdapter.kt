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
import com.example.aicleaner.ui.fragments.PhotoCleanFragmentDirections
import com.example.aicleaner.utils.interfaces.NavigateData


class FolderItemAdapter(
    val context: Context,
    private val navigateData: NavigateData
) :
    ListAdapter<ImageFolder, FolderItemAdapter.LiveSliderAdapterViewHolder>(
        LiveSliderAdapterDiffUtilCallback
    ) {


    class LiveSliderAdapterViewHolder(
        private var binding: PhotoItemLayoutBinding,
        private var context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bindSeriesData(folder: ImageFolder) {
//            binding?.model = seriesScoresModel
            binding?.folderName?.text = folder.folderName
            binding?.picCount?.text = folder.numberOfPics.toString()
            binding?.sizeInMb?.text = folder.folderSize.toString() + " mb"

            Glide.with(context)
                .load(folder.firstPic)
                .into(binding.appIcon)
            binding?.executePendingBindings()

        }

    }

    object LiveSliderAdapterDiffUtilCallback : DiffUtil.ItemCallback<ImageFolder>() {

        override fun areItemsTheSame(
            oldItem: ImageFolder,
            newItem: ImageFolder
        ): Boolean {
            return oldItem.folderName == newItem.folderName
        }

        override fun areContentsTheSame(
            oldItem: ImageFolder,
            newItem: ImageFolder
        ): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveSliderAdapterViewHolder {
        val binding: PhotoItemLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.photo_item_layout, parent, false
        )
        return LiveSliderAdapterViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: LiveSliderAdapterViewHolder, position: Int) {
        holder.bindSeriesData(currentList[position])
        holder.itemView.setOnClickListener {
            val direction =
                PhotoCleanFragmentDirections.actionPhotoCleanFragmentToPhotoDeleteFragment(
                    currentList[position].path,
                    currentList[position].folderName,
                    currentList[position].folderSize.toString()
                )
            navigateData.navigation(direction)
        }

    }


}