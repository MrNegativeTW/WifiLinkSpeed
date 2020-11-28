package com.txwstudio.app.wifilinkspeed.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.txwstudio.app.wifilinkspeed.R
import com.txwstudio.app.wifilinkspeed.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    companion object {
        fun newInstance() = AboutFragment()
    }

    private lateinit var navController: NavController

    private lateinit var binding: FragmentAboutBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = findNavController()
        subscribeUi()
    }

    private fun subscribeUi() {
        binding.buttonAboutFragBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }
}