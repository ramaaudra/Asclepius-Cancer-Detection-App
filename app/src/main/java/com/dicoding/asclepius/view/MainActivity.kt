package com.dicoding.asclepius.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import org.tensorflow.lite.task.vision.classifier.Classifications
import androidx.lifecycle.ViewModelProvider



class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
//    private var currentImageUri: Uri? = null
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.currentImageUri?.let { uri ->
            showImage()
        }

        binding.galleryButton.setOnClickListener {
            checkPermissionAndStartGallery()
        }

        binding.analyzeButton.setOnClickListener {
            viewModel.currentImageUri?.let {
                analyzeImage(it)
            } ?: run {
                showToast(getString(R.string.image_classifier_failed))
            }
        }
    }

    private fun checkPermissionAndStartGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            startGallery()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startGallery()
        } else {
            showToast("Permission denied. Unable to access gallery.")
        }
    }

    private fun startGallery() {
        // TODO: Mendapatkan gambar dari Gallery.
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun showImage() {
        // TODO: Menampilkan gambar sesuai Gallery yang dipilih.
        viewModel.currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun analyzeImage(imageUri: Uri) {
        val imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(errorMessage: String) {
                    Log.d(TAG, "Error: $errorMessage")
                    showToast("error: $errorMessage")
                }
                override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
                    results?.let { showResults(it) }
                }
            }
        )
        imageClassifierHelper.classifyStaticImage(imageUri)
    }

    private fun showResults(results: List<Classifications>) {
        val topResult = results[0]
        val label = topResult.categories[0].label
        val score = topResult.categories[0].score

        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra(ResultActivity.IMAGE_URI, viewModel.currentImageUri.toString())
            putExtra(ResultActivity.PREDICTION, label)
            putExtra(ResultActivity.CONFIDENCE, score)
        }
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}