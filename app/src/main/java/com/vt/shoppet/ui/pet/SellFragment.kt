package com.vt.shoppet.ui.pet

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.common.InputImage
import com.vt.shoppet.R
import com.vt.shoppet.databinding.FragmentSellBinding
import com.vt.shoppet.model.Result
import com.vt.shoppet.repo.LabelerRepo
import com.vt.shoppet.repo.StorageRepo
import com.vt.shoppet.util.*
import com.vt.shoppet.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SellFragment : Fragment(R.layout.fragment_sell) {

    private val binding by viewBinding(FragmentSellBinding::bind)

    private val viewModel: DataViewModel by activityViewModels()

    private val args: SellFragmentArgs by navArgs()

    @Inject
    lateinit var labeler: LabelerRepo

    @Inject
    lateinit var storage: StorageRepo

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        val labels = resources.getStringArray(R.array.labels)

        val progress = binding.progress
        val circularProgress = circularProgress(context)
        val imagePet = binding.imagePet
        val btnUpload = binding.btnUpload as MaterialButton
        val txtLabels = binding.txtLabels

        val upload = resources.getDrawable(R.drawable.ic_upload, context.theme)

        val uri = args.uri.toUri()

        loadImage(imagePet, uri)

        fun uploadImage(uri: Uri) {
            val image = UUID.randomUUID().toString()
            storage.uploadPetPhoto(image, uri)
                .observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is Result.Loading -> {
                            circularProgress.start()
                            btnUpload.isClickable = false
                            btnUpload.icon = circularProgress as Drawable
                            progress.isVisible = true
                        }
                        is Result.Success -> {
                            btnUpload.icon = upload
                            circularProgress.stop()
                            progress.isVisible = false
                            viewModel.getCurrentUser().observe(viewLifecycleOwner) { user ->
                                val action = SellFragmentDirections.actionSellToDetails(image, user.username)
                                findNavController().navigate(action)
                            }
                        }
                        is Result.Failure -> {
                            showSnackbar(result.exception)
                            btnUpload.isClickable = true
                            btnUpload.icon = upload
                            circularProgress.stop()
                            progress.isVisible = false
                        }
                    }
                }
        }

        btnUpload.setOnClickListener {
            var isAnimal = false
            val image = InputImage.fromFilePath(context, uri)
            labeler.process(image)
                .observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is Result.Loading -> {
                            circularProgress.start()
                            btnUpload.isClickable = false
                            btnUpload.icon = circularProgress as Drawable
                        }
                        is Result.Success -> {
                            txtLabels.text = null
                            for (label in result.data) {
                                val text = label.text
                                val confidence = label.confidence * 100
                                txtLabels.append("$text | $confidence%\n")
                                isAnimal = labels.contains(text)
                                if (isAnimal) break
                            }
                            if (isAnimal) uploadImage(uri)
                            else {
                                showSnackbar(getString(R.string.txt_animal_undetected))
                                btnUpload.isClickable = true
                                btnUpload.icon = upload
                                circularProgress.stop()
                            }
                        }
                        is Result.Failure -> {
                            showSnackbar(result.exception)
                            btnUpload.isClickable = true
                            btnUpload.icon = upload
                            circularProgress.stop()
                        }
                    }
                }
        }
    }

}