package com.vt.shoppet.ui.pet

import android.content.ContentValues
import android.os.Bundle
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.provider.MediaStore.MediaColumns
import android.view.View
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import com.google.mlkit.vision.common.InputImage
import com.vt.shoppet.R
import com.vt.shoppet.databinding.FragmentCameraBinding
import com.vt.shoppet.model.Result
import com.vt.shoppet.util.showSnackbar
import com.vt.shoppet.util.viewBinding
import com.vt.shoppet.viewmodel.LabelerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CameraFragment : Fragment(R.layout.fragment_camera) {

    private val binding by viewBinding(FragmentCameraBinding::bind)
    private val labeler: LabelerViewModel by activityViewModels()

    inner class ImageSavedCallback(private val cameraProvider: ProcessCameraProvider) :
        ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(results: ImageCapture.OutputFileResults) {
            cameraProvider.unbindAll()
            val action = CameraFragmentDirections.actionCameraToSell(results.savedUri.toString())
            findNavController().navigate(action)
        }

        override fun onError(exception: ImageCaptureException) {
            showSnackbar(exception)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    @ExperimentalGetImage
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showSnackbar(getString(R.string.txt_camera))

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

        val snackbar =
            Snackbar.make(view, R.string.txt_animal_undetected, Snackbar.LENGTH_SHORT)
        var isAnimal = false

        val analyzer = ImageAnalysis.Analyzer { proxy ->
            val mediaImage = proxy.image ?: return@Analyzer
            val image = InputImage.fromMediaImage(mediaImage, proxy.imageInfo.rotationDegrees)
            labeler.process(image).observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Result.Success -> {
                        val label = result.data.firstOrNull()?.text
                        txtLabel.text = label
                        isAnimal = labels.contains(label)
                        proxy.close()
                    }
                    is Result.Failure -> {
                        txtLabel.text = null
                        proxy.close()
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