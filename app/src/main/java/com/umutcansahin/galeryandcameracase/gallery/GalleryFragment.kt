package com.umutcansahin.galeryandcameracase.gallery

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.umutcansahin.galeryandcameracase.base.BaseFragment
import com.umutcansahin.galeryandcameracase.databinding.FragmentGalleryBinding


class GalleryFragment : BaseFragment<FragmentGalleryBinding>(FragmentGalleryBinding::inflate) {

    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val selectedImage: Uri? = data?.data
                binding.imageView.setImageURI(selectedImage)
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // İzin verildiyse
                openGallery()
            } else {
                // İzin reddedildiyse
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonOpenGallery.setOnClickListener {
            checkStoragePermission()
        }
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // İzin verilmemişse izin iste
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            // İzin verilmişse galeriyi aç
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectImageLauncher.launch(intent)
    }
}