package com.jucelio.rotacerta.ui.pix

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Analyzer do CameraX responsável por entregar o primeiro código válido
 * detectado pelo ML Kit. O bloqueio atômico evita múltiplos callbacks do mesmo
 * objeto enquanto frames consecutivos ainda estão sendo processados.
 */
class QrCodeAnalyzer(
    private val scanner: BarcodeScanner,
    private val onQrCodeDetected: (String) -> Unit,
    private val onFailure: (Exception) -> Unit = {}
) : ImageAnalysis.Analyzer {

    private val resultDelivered = AtomicBoolean(false)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image

        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val value = barcodes
                    .asSequence()
                    .mapNotNull { barcode -> barcode.rawValue?.trim() }
                    .firstOrNull { it.isNotEmpty() }

                if (value != null && resultDelivered.compareAndSet(false, true)) {
                    onQrCodeDetected(value)
                }
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
