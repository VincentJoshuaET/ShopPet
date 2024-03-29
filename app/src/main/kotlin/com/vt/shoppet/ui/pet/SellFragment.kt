package com.vt.shoppet.ui.pet

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import com.google.mlkit.vision.common.InputImage
import com.vt.shoppet.R
import com.vt.shoppet.databinding.FragmentSellBinding
import com.vt.shoppet.util.*
import com.vt.shoppet.viewmodel.DataViewModel
import com.vt.shoppet.viewmodel.StorageViewModel
import com.vt.shoppet.viewmodel.VisionViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class SellFragment : Fragment(R.layout.fragment_sell) {

    private val binding by viewBinding(FragmentSellBinding::bind)

    private val dataViewModel: DataViewModel by activityViewModels()
    private val storage: StorageViewModel by viewModels()
    private val vision: VisionViewModel by viewModels()

    private val args: SellFragmentArgs by navArgs()

    private val progress by lazy { circularProgress }
    private val upload by lazy { getDrawable(R.drawable.ic_upload) }

    private fun uploadImage(uri: Uri) {
        val image = UUID.randomUUID().toString()
        val btnUpload = binding.btnUpload
        val progressIndicator = binding.progress
        progress.start()
        btnUpload.isClickable = false
        btnUpload.icon = progress as Drawable
        progressIndicator.isVisible = true
        storage.uploadPetPhoto(image, uri).observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                btnUpload.icon = upload
                progress.stop()
                progressIndicator.isVisible = false
                dataViewModel.currentUser.observe(viewLifecycleOwner) { user ->
                    val action =
                        SellFragmentDirections.actionSellToDetails(image, user.username)
                    findNavController().navigate(action)
                }
            }
            result.onFailure { exception ->
                binding.snackbar(message = exception.localizedMessage, owner = viewLifecycleOwner) {
                    uploadImage(uri)
                }.show()
                btnUpload.isClickable = true
                btnUpload.icon = upload
                progress.stop()
                progressIndicator.isVisible = false
            }
        }
    }

    private fun processImage(image: InputImage, uri: Uri) {
        val labels = resources.getStringArray(R.array.labels)
        val btnUpload = binding.btnUpload
        val txtLabels = binding.txtLabels
        var isAnimal = false
        vision.process(image).observe(viewLifecycleOwner) { result ->
            result.onSuccess { list ->
                txtLabels.text = null
                for (label in list) {
                    val text = label.text
                    val confidence = label.confidence * 100
                    txtLabels.append("$text | $confidence%\n")
                    isAnimal = labels.contains(text)
                    if (isAnimal) break
                }
                if (isAnimal) uploadImage(uri)
                else {
                    binding.snackbar(getString(R.string.txt_animal_undetected)).show()
                    btnUpload.isClickable = true
                    btnUpload.icon = upload
                    progress.stop()
                }
            }
            result.onFailure { exception ->
                binding.snackbar(message = exception.localizedMessage, owner = viewLifecycleOwner) {
                    processImage(image, uri)
                }.show()
                btnUpload.isClickable = true
                btnUpload.icon = upload
                progress.stop()
            }
        }
    }

    private fun convertImage(uri: Uri) {
        val btnUpload = binding.btnUpload
        progress.start()
        btnUpload.isClickable = false
        btnUpload.icon = progress as Drawable
        vision.convertImage(requireContext(), uri).observe(viewLifecycleOwner) { result ->
            result.onSuccess { image ->
                processImage(image, uri)
            }
            result.onFailure { exception ->
                binding.snackbar(message = exception.localizedMessage, owner = viewLifecycleOwner) {
                    convertImage(uri)
                }.show()
                btnUpload.isClickable = true
                btnUpload.icon = upload
                progress.stop()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnUpload = binding.btnUpload
        val imagePet = binding.imagePet

        val uri = args.uri.toUri()

        loadImage(imagePet, uri)

        btnUpload.setOnClickListener {
            convertImage(uri)
        }
    }

}