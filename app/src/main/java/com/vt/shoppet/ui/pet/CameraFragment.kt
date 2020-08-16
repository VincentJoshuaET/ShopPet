package com.vt.shoppet.ui.pet

import android.content.ContentValues
import android.os.Bundle
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.provider.MediaStore.MediaColumns
import android.view.Gravity
import android.view.View
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.vt.shoppet.R
import com.vt.shoppet.databinding.FragmentCameraBinding
import com.vt.shoppet.util.snackbar
import com.vt.shoppet.util.viewBinding
import com.vt.shoppet.viewmodel.VisionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CameraFragment : Fragment(R.layout.fragment_camera) {

    private val binding by viewBinding(FragmentCameraBinding::bind)
    private val vision: VisionViewModel by viewModels()

    inner class ImageSavedCallback(private val cameraProvider: ProcessCameraProvider) :
        ImageCapture.OnImageSavedCallback {

        override fun onImageSaved(results: ImageCapture.OutputFileResults) {
            cameraProvider.unbindAll()
            val action = CameraFragmentDirections.actionCameraToSell(results.savedUri.toString())
            findNavController().navigate(action)
        }

        override fun onError(exception: ImageCaptureException) =
            binding.snackbar(message = exception.localizedMessage, gravity = Gravity.TOP).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    @ExperimentalGetImage
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.snackbar(message = getString(R.string.txt_camera), gravity = Gravity.TOP).show()

        val context = requireContext()

        val cameraView = binding.cameraView
        val txtLabel = binding.txtLabel
        val btnCapture = binding.btnCapture

        val executor = ContextCompat.getMainExecutor(context)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val labels = resources.getStringArray(R.array.labels)

        val preview =
            Preview.Builder()
                .build()
        val selector =
            CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()
        val analysis =
            ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
        val capture =
            ImageCapture.Builder()
                .build()
        val values =
            ContentValues().apply {
                put(MediaColumns.DISPLAY_NAME, "Take Picture")
                put(MediaColumns.MIME_TYPE, "image/jpg")
            }
        val options =
            ImageCapture.OutputFileOptions
                .Builder(context.contentResolver, EXTERNAL_CONTENT_URI, values)
                .build()
        val callback = ImageSavedCallback(cameraProviderFuture.get())

        val snackbar = binding.snackbar(
            message = getString(R.string.txt_animal_undetected),
            gravity = Gravity.TOP
        )
        var isAnimal = false

        val analyzer = ImageAnalysis.Analyzer { proxy ->
            vision.convertImage(proxy).observe(viewLifecycleOwner) { result ->
                result.onSuccess { image ->
                    vision.process(image).observe(viewLifecycleOwner) { result ->
                        result.onSuccess { list ->
                            val label = list.firstOrNull()?.text
                            txtLabel.text = label
                            isAnimal = labels.contains(label)
                            proxy.close()
                        }
                        result.onFailure {
                            txtLabel.text = null
                            proxy.close()
                        }
                    }
                }
            }
        }

        analysis.setAnalyzer(executor, analyzer)

        btnCapture.setOnClickListener {
            btnCapture.isEnabled = false
            if (isAnimal) capture.takePicture(options, executor, callback)
            else {
                snackbar.show()
                btnCapture.isEnabled = true
            }
        }

        val runnable = Runnable {
            preview.setSurfaceProvider(cameraView.createSurfaceProvider())
            cameraProviderFuture.get()
                .bindToLifecycle(this, selector, preview, analysis, capture)
        }

        cameraProviderFuture.addListener(runnable, executor)
    }

}