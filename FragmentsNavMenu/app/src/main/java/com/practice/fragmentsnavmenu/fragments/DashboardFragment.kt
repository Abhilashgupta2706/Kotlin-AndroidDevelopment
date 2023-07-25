package com.practice.fragmentsnavmenu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.practice.fragmentsnavmenu.R
import com.practice.fragmentsnavmenu.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    // ... (other code remains unchanged)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentDashboardBinding>(
            inflater,
            R.layout.fragment_dashboard,
            container,
            false
        )

        binding.tvDashboard.text = "Changed using Data Binding"

        return binding.root
    }

}
