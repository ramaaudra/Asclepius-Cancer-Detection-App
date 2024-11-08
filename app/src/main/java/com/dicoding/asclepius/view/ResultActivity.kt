package com.dicoding.asclepius.view

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    companion object {
        const val IMAGE_URI = "img_uri"
        const val PREDICTION = "prediction"
        const val CONFIDENCE = "confidence"
        const val TAG = "ResultActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val imageUriString = intent.getStringExtra(IMAGE_URI)
        val prediction = intent.getStringExtra(PREDICTION)
        val confidence = intent.getFloatExtra(CONFIDENCE, 0.0f)

        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            displayImage(imageUri)
            displayResults(prediction, confidence)
        } else {
            Log.e(TAG, "No image URI provided")
            finish()
        }
    }

        private fun displayImage(uri: Uri) {
            Log.d(TAG, "Displaying image: $uri")
            binding.resultImage.setImageURI(uri)
        }


    private fun displayResults(prediction: String?, confidence: Float) {
        fun Float.formatToString(): String {
            return String.format("%.2f%%", this * 100)
        }
        binding.resultText.text = "$prediction ${confidence.formatToString()}"
    }
}