package com.example.aicleaner.ui.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.aicleaner.R
import com.example.aicleaner.databinding.AppManagerItemBinding
import com.example.aicleaner.models.AppManagerModel


class AppManagerItemAdapter(
    val context: Context
) :
    ListAdapter<AppManagerModel, AppManagerItemAdapter.LiveSliderAdapterViewHolder>(
        LiveSliderAdapterDiffUtilCallback
    ) {


    class LiveSliderAdapterViewHolder(
        private var binding: AppManagerItemBinding,
        private var context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bindSeriesData(appManagerModel: AppManagerModel) {
//            binding?.model = seriesScoresModel
            binding?.appName?.text = appManagerModel.appName
            binding?.appIcon?.setImageDrawable(appManagerModel?.appIcon)
            binding?.executePendingBindings()

        }
    }

    object LiveSliderAdapterDiffUtilCallback : DiffUtil.ItemCallback<AppManagerModel>() {

        override fun areItemsTheSame(
            oldItem: AppManagerModel,
            newItem: AppManagerModel
        ): Boolean {
            return oldItem.appName == newItem.appName
        }

        override fun areContentsTheSame(
            oldItem: AppManagerModel,
            newItem: AppManagerModel
        ): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveSliderAdapterViewHolder {
        val binding: AppManagerItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.app_manager_item, parent, false
        )
        return LiveSliderAdapterViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: LiveSliderAdapterViewHolder, position: Int) {
        holder.bindSeriesData(currentList[position])
        holder.itemView.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val uri = Uri.fromParts("package", currentList[position].appPackageName, null)
            intent.data = uri
            context.startActivity(intent)
        }

    }


}