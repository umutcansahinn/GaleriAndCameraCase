package com.umutcansahin.galeryandcameracase.camera_controller

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
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.umutcansahin.galeryandcameracase.base.BaseFragment
import com.umutcansahin.galeryandcameracase.databinding.FragmentCameraControllerBinding
import java.text.SimpleDateFormat
import java.util.*

class CameraControllerFragment :
    BaseFragment<FragmentCameraControllerBinding>(FragmentCameraControllerBinding::inflate) {

    private lateinit var cameraController: LifecycleCameraController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!hasPermissions(requireContext())) {
            //izinlerin daha önceden verilip verilmediği kontrollü
            activityResultLauncher.launch(REQUIRED_PERMISSIONS)
        } else {
            startCamera()
        }

        binding.imageCaptureButton.setOnClickListener { takePhoto() }
    }

    private fun startCamera() {
        val previewView: PreviewView = binding.viewFinder
        cameraController = LifecycleCameraController(requireContext())
        cameraController.bindToLifecycle(this)
        cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        previewView.controller = cameraController
    }

    private fun takePhoto() {
        //create time stamped name and mediaStore entry
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME,name)
            put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg")
            if (Build.VERSION.SDK_INT>Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/Camerax-Images")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(requireContext().contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues)
            .build()

        cameraController.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object: ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${outputFileResults.savedUri}"
                    Toast.makeText(requireContext(),msg,Toast.LENGTH_SHORT).show()
                    Log.d(TAG,msg)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.d(TAG,"photo capture failed:${exception.message}",exception)
                }
            }
        )
    }

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
                startCamera()
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