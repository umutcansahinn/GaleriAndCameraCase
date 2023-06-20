package com.umutcansahin.galeryandcameracase.camera_provider

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.umutcansahin.galeryandcameracase.base.BaseFragment
import com.umutcansahin.galeryandcameracase.camera_controller.CameraControllerFragment
import com.umutcansahin.galeryandcameracase.databinding.FragmentCameraProviderBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class CameraProviderFragment :
    BaseFragment<FragmentCameraProviderBinding>(FragmentCameraProviderBinding::inflate) {

    private var imageCapture: ImageCapture? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!CameraControllerFragment.hasPermissions(requireContext())) {
            //izinlerin daha önceden verilip verilmediği kontrollü
            activityResultLauncher.launch(REQUIRED_PERMISSIONS)
        } else {
            lifecycleScope.launch {
                startCamera()
            }
        }

        binding.imageCaptureButton.setOnClickListener { takePhoto() }
    }

    private suspend fun startCamera() {
        val cameraProvider = ProcessCameraProvider.getInstance(requireContext()).await()
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)

        imageCapture = ImageCapture.Builder().build()

        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        try {
            cameraProvider.unbindAll()
            var camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

        } catch (e: Exception) {
            Log.d(TAG, "Usecase binding failed", e)
        }
    }

    private fun takePhoto() {
        //create time stamped name and mediaStore entry
        //cekilen foto kayıt islemler ve isim atama
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Camerax-Images")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                requireContext().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        //fotograf cekme islemleri
        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${outputFileResults.savedUri}"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.d(TAG, "photo capture failed:${exception.message}", exception)
                }
            }
        )
    }


    //izin alma islemleri
    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value) permissionGranted = true
            }
            if (!permissionGranted) {
                Toast.makeText(requireContext(), "permission request denied", Toast.LENGTH_LONG)
                    .show()
            } else {
                lifecycleScope.launch {
                    startCamera()
                }
            }
        }


    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS = mutableListOf(android.Manifest.permission.CAMERA).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()

        fun hasPermissions(context: Context) = REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}