package com.example.aicleaner.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aicleaner.R
import com.example.aicleaner.databinding.FragmentAppManagerBinding
import com.example.aicleaner.models.AppManagerModel
import com.example.aicleaner.ui.adapters.AppManagerItemAdapter
import com.example.aicleaner.viewmodels.PhotoCleanViewModel

class AppManagerFragment : Fragment() {

    private var bindingAppManager: FragmentAppManagerBinding? = null
    private val photoCleanViewModel by lazy {
        ViewModelProvider(requireActivity())[PhotoCleanViewModel::class.java]
    }
    var listApps: MutableList<AppManagerModel>? =
        ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_app_manager, container, false)
        bindingAppManager = DataBindingUtil.bind(view)
        bindingAppManager?.backIcon?.setOnClickListener {
            findNavController().popBackStack()
        }
        bindingAppManager?.lottiePlayer?.visibility=View.VISIBLE
        getAllAppsPresentInOperatingSystem()
        return view
    }

    private fun getAllAppsPresentInOperatingSystem() {
        photoCleanViewModel?.getAllApps(requireContext())
        photoCleanViewModel?.listOfApps?.observe(viewLifecycleOwner, Observer {
           if (!it.isNullOrEmpty())
           {
               it.toMutableList()?.let { it1 -> setUpAdapter(it1) }
           }
            else
           {
               bindingAppManager?.lottiePlayer?.visibility=View.GONE
           }
        })

    }

    private fun setUpAdapter(listApps: MutableList<AppManagerModel?>) {
        bindingAppManager?.lottiePlayer?.visibility=View.GONE
        val adapter = AppManagerItemAdapter(requireContext())
        bindingAppManager?.appManagerRecycler?.layoutManager =
            LinearLayoutManager(context)
        bindingAppManager?.appManagerRecycler?.adapter = adapter
        adapter?.submitList(listApps)

    }

}