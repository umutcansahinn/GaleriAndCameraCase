package com.umutcansahin.galeryandcameracase.camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.umutcansahin.galeryandcameracase.base.BaseFragment
import com.umutcansahin.galeryandcameracase.databinding.FragmentCameraBinding

class CameraFragment : BaseFragment<FragmentCameraBinding>(FragmentCameraBinding::inflate) {

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap: Bitmap? = result.data?.extras?.get("data") as? Bitmap
                binding.imageView.setImageBitmap(imageBitmap)
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // İzin verildiyse
                openCamera()
            } else {
                // İzin reddedildiyse
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonOpenCamera.setOnClickListener {
            checkCameraPermission()
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            openCamera()
        }
    }


    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePhotoLauncher.launch(intent)
    }
}