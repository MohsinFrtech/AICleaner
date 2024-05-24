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
import com.example.aicleaner.models.CacheCleanModel
import com.example.aicleaner.models.PictureData
import com.example.aicleaner.models.VideoFileData
import com.example.aicleaner.utils.interfaces.AppCacheItemClick
import com.example.aicleaner.utils.interfaces.PhotoClicked
import com.example.aicleaner.utils.interfaces.VideoClicked
import java.io.File


class AppCacheAdapterAdapter(
    val context: Context,
    private val cacheItemClicked: AppCacheItemClick
) :
    ListAdapter<CacheCleanModel, AppCacheAdapterAdapter.LiveSliderAdapterViewHolder>(
        LiveSliderAdapterDiffUtilCallback
    ) {


    class LiveSliderAdapterViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.findViewById<ImageView>(R.id.image_selected_icon)
        val appImage = itemView.findViewById<ImageView>(R.id.appIcon)
        val textInMb = itemView.findViewById<TextView>(R.id.sizeInMbApp)
        val appName = itemView.findViewById<TextView>(R.id.appName)
    }

    object LiveSliderAdapterDiffUtilCallback : DiffUtil.ItemCallback<CacheCleanModel>() {

        override fun areItemsTheSame(
            oldItem: CacheCleanModel,
            newItem: CacheCleanModel
        ): Boolean {
            return oldItem.appName == newItem.appName
        }

        override fun areContentsTheSame(
            oldItem: CacheCleanModel,
            newItem: CacheCleanModel
        ): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveSliderAdapterViewHolder {
        val lay:View = LayoutInflater.from(context).inflate(R.layout.apps_cache_item,parent,false)
        return LiveSliderAdapterViewHolder(lay)
    }

    override fun onBindViewHolder(holder: LiveSliderAdapterViewHolder, position: Int) {
//        holder.bindSeriesData(currentList[position])
        if (currentList[position].cacheAppSelected == true)
        {
            holder.imageView?.setImageResource(R.drawable.check_circle)
        }
        else
        {
            holder.imageView?.setImageResource(R.drawable.check_circle_empty)
        }
        holder?.appImage?.setImageDrawable(currentList[position]?.appIcon)
        holder.appName.text = currentList[position].appName
        holder.textInMb.text = currentList[position].appCacheSize+" mb"

        holder.imageView?.setOnClickListener {
            if (currentList[position].cacheAppSelected == false) {
                holder.imageView?.setImageResource(R.drawable.check_circle)
                currentList[position].cacheAppSelected = true
                cacheItemClicked.cacheClick(currentList[position])
            } else {
                holder.imageView?.setImageResource(R.drawable.check_circle_empty)
                currentList[position].cacheAppSelected = false
                cacheItemClicked.cacheClick(currentList[position])
            }
        }
    }


}