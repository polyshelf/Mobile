package com.command.polyshelf.presentation.screen.scanner.analyzer

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import timber.log.Timber

class BarcodeAnalyzer(
    private val onBarcodeScanned: (ScannedCodeResult) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_DATA_MATRIX,
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_QR_CODE
        )
        .build()

    private val scanner = BarcodeScanning.getClient(options)
    private var isScanningEnabled = true
    private var lastScannedTimestamp = 0L
    private var lastScannedCode = ""

    fun setScanningEnabled(enabled: Boolean) {
        isScanningEnabled = enabled
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || !isScanningEnabled) {
            imageProxy.close()
            return
        }

        val currentTime = System.currentTimeMillis()
        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        processInputImage(inputImage, currentTime, imageProxy)
    }

    private fun processInputImage(
        inputImage: InputImage,
        currentTime: Long,
        imageProxy: ImageProxy
    ) {
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                handleBarcodeResult(barcodes.firstOrNull(), currentTime)
            }
            .addOnFailureListener { ex ->
                Timber.e(ex, "Barcode scanning failed")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun handleBarcodeResult(barcode: Barcode?, currentTime: Long) {
        val rawValue = barcode?.rawValue
        if (barcode != null && !rawValue.isNullOrBlank() && isScanningEnabled) {
            if (rawValue == lastScannedCode && (currentTime - lastScannedTimestamp) < SCAN_COOLDOWN_MS) {
                return
            }

            // Immediately disable scanning synchronously so no subsequent frame triggers
            isScanningEnabled = false
            lastScannedTimestamp = currentTime
            lastScannedCode = rawValue

            val codeType = if (barcode.format == Barcode.FORMAT_DATA_MATRIX) {
                ScannedCodeType.DATA_MATRIX
            } else {
                ScannedCodeType.BARCODE
            }
            Timber.d("Barcode detected: $rawValue (Type: $codeType)")
            onBarcodeScanned(ScannedCodeResult(rawValue, codeType))
        }
    }

    private companion object {
        const val SCAN_COOLDOWN_MS = 2500L
    }
}

enum class ScannedCodeType {
    DATA_MATRIX,
    BARCODE
}

data class ScannedCodeResult(
    val code: String,
    val type: ScannedCodeType
)
