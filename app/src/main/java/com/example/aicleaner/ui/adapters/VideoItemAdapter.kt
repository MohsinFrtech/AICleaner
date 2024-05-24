package com.example.aicleaner.ui.adapters

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.aicleaner.R
import com.example.aicleaner.databinding.PhotoItemBinding
import com.example.aicleaner.models.PictureData
import com.example.aicleaner.models.VideoFileData
import com.example.aicleaner.utils.interfaces.PhotoClicked
import com.example.aicleaner.utils.interfaces.VideoClicked
import java.io.File


class VideoItemAdapter(
    val context: Context,
    private val videoClicked: VideoClicked
) :
    ListAdapter<VideoFileData, VideoItemAdapter.LiveSliderAdapterViewHolder>(
        LiveSliderAdapterDiffUtilCallback
    ) {


    class LiveSliderAdapterViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.findViewById<ImageView>(R.id.image_selected_icon)
        val videoImage = itemView.findViewById<ImageView>(R.id.appIcon)
        val videoDuration = itemView.findViewById<TextView>(R.id.videoDuration)
    }

    object LiveSliderAdapterDiffUtilCallback : DiffUtil.ItemCallback<VideoFileData>() {

        override fun areItemsTheSame(
            oldItem: VideoFileData,
            newItem: VideoFileData
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: VideoFileData,
            newItem: VideoFileData
        ): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveSliderAdapterViewHolder {
        val lay:View = LayoutInflater.from(context).inflate(R.layout.video_item,parent,false)
        return LiveSliderAdapterViewHolder(lay)
    }

    override fun onBindViewHolder(holder: LiveSliderAdapterViewHolder, position: Int) {
//        holder.bindSeriesData(currentList[position])
        if (currentList[position].videoSelected == true)
        {
            holder.imageView?.setImageResource(R.drawable.check_circle)
        }
        else
        {
            holder.imageView?.setImageResource(R.drawable.check_circle_empty)
        }
        holder?.videoImage?.setImageBitmap(currentList[position].videothumbnail)
        holder.videoDuration.text = currentList[position].videoDuration

        holder.imageView?.setOnClickListener {
            if (currentList[position].videoSelected == false) {
                holder.imageView?.setImageResource(R.drawable.check_circle)
                currentList[position].videoSelected = true
                videoClicked.videoItem(currentList[position])
//                deleteImage(currentList[position].picPath)
            } else {
                holder.imageView?.setImageResource(R.drawable.check_circle_empty)
                currentList[position].videoSelected = false
                videoClicked.videoItem(currentList[position])
            }
        }
    }


}