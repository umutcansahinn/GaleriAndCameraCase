package com.umutcansahin.galeryandcameracase

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.umutcansahin.galeryandcameracase.base.BaseFragment
import com.umutcansahin.galeryandcameracase.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.buttonGallery.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToGalleryFragment())
        }
        binding.buttonCamera.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToCameraFragment())
        }
        binding.buttonCameraController.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_cameraControllerFragment)
        }
        binding.buttonCameraProvider.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_cameraProviderFragment)
        }
    }
}