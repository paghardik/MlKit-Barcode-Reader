package com.mlkit.barcode.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.mlkit.barcode.databinding.ActivityBacodeBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class BarcodeActivity : AppCompatActivity() {
    private var _binding: ActivityBacodeBinding? = null
    private val binding: ActivityBacodeBinding get() = _binding!!

    private val barcodeOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_ALL_FORMATS
        )
        .build()

    private val barcodeScanner = BarcodeScanning.getClient(barcodeOptions)

    private val barcodeExecutor: ExecutorService = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors()
    )

    @SuppressLint("UnsafeExperimentalUsageError")
    private val barcodeAnalyser: ImageAnalysis.Analyzer = ImageAnalysis.Analyzer { img ->
        val mediaImage = img.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, img.imageInfo.rotationDegrees)
            Tasks.await(barcodeScanner.process(inputImage))
                .forEach(this@BarcodeActivity::prettyPrintBarcodeResult)
        }
        img.close()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityBacodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.v(TAG, "Number of cores ${Runtime.getRuntime().availableProcessors()}")
        askPermission(*REQUIRED_PERMISSION) {
            startCameraPreview()
        }.onDeclined {
            // Handle denied permissions here
        }
    }

    private fun startCameraPreview() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this@BarcodeActivity)

        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()

                // Camera Preview Setup
                val cameraPreview = Preview.Builder()
                    .build()
                    .also { previewBuilder ->
                        previewBuilder.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
                    }

                // Preview frame analysis
                val imageAnalysis = ImageAnalysis.Builder()
                    .setImageQueueDepth(1)
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(barcodeExecutor, barcodeAnalyser)

                // Hook every thing in camera preview
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this@BarcodeActivity,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    imageAnalysis,
                    cameraPreview
                )
            },
            ContextCompat.getMainExecutor(this@BarcodeActivity)
        )
    }

    override fun onDestroy() {
        if (!barcodeExecutor.isShutdown) {
            barcodeExecutor.shutdown()
        }
        super.onDestroy()
    }

    private fun prettyPrintBarcodeResult(barcodeResult: Barcode) {
        val typeStr = when (barcodeResult.format) {
            Barcode.FORMAT_AZTEC -> "Aztec"
            Barcode.FORMAT_CODABAR -> "Codabar"
            Barcode.FORMAT_CODE_128 -> "Code 128"
            Barcode.FORMAT_CODE_39 -> "Code 39"
            Barcode.FORMAT_CODE_93 -> "Code 93"
            Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
            Barcode.FORMAT_EAN_13 -> "Ean 13"
            Barcode.FORMAT_EAN_8 -> "Ean 8"
            Barcode.FORMAT_ITF -> "Itf"
            Barcode.FORMAT_PDF417 -> "Pdf 417"
            Barcode.FORMAT_QR_CODE -> "Qr Code"
            Barcode.FORMAT_UPC_A -> "Upc A"
            Barcode.FORMAT_UPC_E -> "Upc E"
            Barcode.FORMAT_UNKNOWN -> "Unknown"
            else -> "Unknown"
        }
        Log.d(TAG, "Type $typeStr, Value ${barcodeResult.displayValue}")

        val intent = Intent()
        intent.putExtra("MESSAGE", "Type $typeStr, Value ${barcodeResult.displayValue}")
        setResult(RESULT_OK, intent)
        finish() //finishing activity

    }

    companion object {
        const val TAG = "BarcodeActivity"
        private val REQUIRED_PERMISSION = arrayOf(Manifest.permission.CAMERA)
    }
}