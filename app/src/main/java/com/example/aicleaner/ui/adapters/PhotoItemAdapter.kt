package com.example.aicleaner.ui.adapters

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.aicleaner.R
import com.example.aicleaner.databinding.PhotoItemBinding
import com.example.aicleaner.models.PictureData
import com.example.aicleaner.utils.interfaces.PhotoClicked
import java.io.File


class PhotoItemAdapter(
    val context: Context,
    private val photoClicked: PhotoClicked
) :
    ListAdapter<PictureData, PhotoItemAdapter.LiveSliderAdapterViewHolder>(
        LiveSliderAdapterDiffUtilCallback
    ) {


    class LiveSliderAdapterViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.findViewById<ImageView>(R.id.image_selected_icon)
        val photoImage = itemView.findViewById<ImageView>(R.id.appIcon)
//        fun bindSeriesData(pictureData: PictureData) {
//            Glide.with(context)
//                .load(pictureData.picPath)
//                .into(binding.appIcon)
//            binding?.executePendingBindings()
//
//        }

    }

    object LiveSliderAdapterDiffUtilCallback : DiffUtil.ItemCallback<PictureData>() {

        override fun areItemsTheSame(
            oldItem: PictureData,
            newItem: PictureData
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: PictureData,
            newItem: PictureData
        ): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveSliderAdapterViewHolder {
        val lay:View = LayoutInflater.from(context).inflate(R.layout.photo_item,parent,false)
        return LiveSliderAdapterViewHolder(lay)
    }

    override fun onBindViewHolder(holder: LiveSliderAdapterViewHolder, position: Int) {
//        holder.bindSeriesData(currentList[position])
        if (currentList[position].picSelected == true)
        {
            holder.imageView?.setImageResource(R.drawable.check_circle)
        }
        else
        {
            holder.imageView?.setImageResource(R.drawable.check_circle_empty)
        }
        Glide.with(context)
            .load(currentList[position].picPath)
            .into(holder.photoImage)
        holder.imageView?.setOnClickListener {
            if (currentList[position].picSelected == false) {
                holder.imageView?.setImageResource(R.drawable.check_circle)
                currentList[position].picSelected = true
                photoClicked.photoItem(currentList[position])
//                deleteImage(currentList[position].picPath)
            } else {
                holder.imageView?.setImageResource(R.drawable.check_circle_empty)
                currentList[position].picSelected = false
                photoClicked.photoItem(currentList[position])
            }
        }
    }


}