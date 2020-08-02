package com.vt.shoppet.ui.pet

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.ProgressIndicator
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.vt.shoppet.R
import com.vt.shoppet.databinding.FragmentSellBinding
import com.vt.shoppet.util.*
import com.vt.shoppet.viewmodel.DataViewModel
import com.vt.shoppet.viewmodel.LabelerViewModel
import com.vt.shoppet.viewmodel.StorageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class SellFragment : Fragment(R.layout.fragment_sell) {

    private val binding by viewBinding(FragmentSellBinding::bind)

    private val labeler: LabelerViewModel by activityViewModels()
    private val storage: StorageViewModel by activityViewModels()
    private val dataViewModel: DataViewModel by activityViewModels()

    private val args: SellFragmentArgs by navArgs()

    private lateinit var circularProgress: Animatable
    private lateinit var upload: Drawable
    private lateinit var progress: ProgressIndicator
    private lateinit var btnUpload: MaterialButton
    private lateinit var txtLabels: TextView

    private fun uploadImage(uri: Uri) {
        val image = UUID.randomUUID().toString()
        circularProgress.start()
        btnUpload.isClickable = false
        btnUpload.icon = circularProgress as Drawable
        progress.isVisible = true
        storage.uploadPetPhoto(image, uri).observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                btnUpload.icon = upload
                circularProgress.stop()
                progress.isVisible = false
                dataViewModel.currentUser.observe(viewLifecycleOwner) { user ->
                    val action =
                        SellFragmentDirections.actionSellToDetails(image, user.username)
                    findNavController().navigate(action)
                }
            }
            result.onFailure { exception ->
                showActionSnackbar(exception) {
                    uploadImage(uri)
                }
                btnUpload.isClickable = true
                btnUpload.icon = upload
                circularProgress.stop()
                progress.isVisible = false
            }
        }
    }

    private fun processImage(image: FirebaseVisionImage, uri: Uri) {
        val labels = resources.getStringArray(R.array.labels)
        var isAnimal = false
        labeler.process(image).observe(viewLifecycleOwner) { result ->
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
                    showSnackbar(getString(R.string.txt_animal_undetected))
                    btnUpload.isClickable = true
                    btnUpload.icon = upload
                    circularProgress.stop()
                }
            }
            result.onFailure { exception ->
                showSnackbar(exception)
                btnUpload.isClickable = true
                btnUpload.icon = upload
                circularProgress.stop()
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

        val context = requireContext()

        circularProgress = circularProgress()
        upload = getDrawable(R.drawable.ic_upload)
        progress = binding.progress
        btnUpload = binding.btnUpload
        txtLabels = binding.txtLabels
        val imagePet = binding.imagePet

        val uri = args.uri.toUri()

        loadImage(imagePet, uri)

        btnUpload.setOnClickListener {
            circularProgress.start()
            btnUpload.isClickable = false
            btnUpload.icon = circularProgress as Drawable
            labeler.convertImage(context, uri).observe(viewLifecycleOwner) { result ->
                result.onSuccess { image ->
                    processImage(image, uri)
                }
                result.onFailure { exception ->
                    showSnackbar(exception)
                    btnUpload.isClickable = true
                    btnUpload.icon = upload
                    circularProgress.stop()
                }
            }
        }
    }

}