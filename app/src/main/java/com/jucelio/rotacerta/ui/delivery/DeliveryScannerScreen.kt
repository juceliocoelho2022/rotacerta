package com.jucelio.rotacerta.ui.delivery

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.jucelio.rotacerta.ui.pix.QrCodeAnalyzer
import com.jucelio.rotacerta.ui.pix.QrScannerOverlay
import java.util.concurrent.Executors

private val ScannerDark = RotaColors.Background
private val ScannerAccent = RotaColors.Accent

/**
 * Tela de leitura do endereço do cliente para a rota de entrega.
 *
 * Lê tanto QR Code quanto os principais códigos de barras 1D,
 * atendendo etiquetas de iFood e de encomendas.
 */
@Composable
fun DeliveryScannerScreen(
    onBack: () -> Unit,
    onCodeRead: (String) -> Unit
) {
    val context = LocalContext.current

    var cameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var permissionDenied by remember {
        mutableStateOf(false)
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            cameraPermissionGranted = granted
            permissionDenied = !granted
        }

    LaunchedEffect(Unit) {
        if (!cameraPermissionGranted) {
            permissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            cameraPermissionGranted -> {
                CameraScannerContent(
                    onCodeRead = onCodeRead
                )

                ScannerHeader(
                    onBack = onBack
                )

                ScannerInstructions(
                    modifier = Modifier.align(
                        Alignment.BottomCenter
                    )
                )
            }

            permissionDenied -> {
                PermissionDeniedContent(
                    onBack = onBack,
                    onRequestAgain = {
                        permissionDenied = false

                        permissionLauncher.launch(
                            Manifest.permission.CAMERA
                        )
                    }
                )
            }

            else -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(
                        Alignment.Center
                    ),
                    color = ScannerAccent
                )
            }
        }
    }
}

@Composable
private fun CameraScannerContent(
    onCodeRead: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraExecutor = remember {
        Executors.newSingleThreadExecutor()
    }

    val scannerOptions = remember {
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC,
                Barcode.FORMAT_DATA_MATRIX,
                Barcode.FORMAT_PDF417,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_CODE_93,
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_ITF,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E
            )
            .build()
    }

    val barcodeScanner = remember {
        BarcodeScanning.getClient(scannerOptions)
    }

    var cameraControl by remember {
        mutableStateOf<CameraControl?>(null)
    }

    var flashEnabled by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(
        flashEnabled,
        cameraControl
    ) {
        cameraControl?.enableTorch(flashEnabled)
    }

    DisposableEffect(Unit) {
        onDispose {
            barcodeScanner.close()
            cameraExecutor.shutdown()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),

            factory = { androidContext ->
                val previewView = PreviewView(
                    androidContext
                ).apply {
                    scaleType =
                        PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture =
                    ProcessCameraProvider.getInstance(
                        androidContext
                    )

                cameraProviderFuture.addListener(
                    {
                        try {
                            val cameraProvider =
                                cameraProviderFuture.get()

                            val preview = Preview.Builder()
                                .build()
                                .also {
                                    it.setSurfaceProvider(
                                        previewView.surfaceProvider
                                    )
                                }

                            val imageAnalysis =
                                ImageAnalysis.Builder()
                                    .setBackpressureStrategy(
                                        ImageAnalysis
                                            .STRATEGY_KEEP_ONLY_LATEST
                                    )
                                    .build()
                                    .also { analysis ->
                                        analysis.setAnalyzer(
                                            cameraExecutor,
                                            QrCodeAnalyzer(
                                                scanner = barcodeScanner,
                                                onQrCodeDetected = onCodeRead,
                                                onFailure = {
                                                    it.printStackTrace()
                                                }
                                            )
                                        )
                                    }

                            cameraProvider.unbindAll()

                            val camera =
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector
                                        .DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalysis
                                )

                            cameraControl = camera.cameraControl

                        } catch (error: Exception) {
                            error.printStackTrace()
                        }
                    },
                    ContextCompat.getMainExecutor(
                        androidContext
                    )
                )

                previewView
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(alpha = 0.18f)
                )
        )

        QrScannerOverlay()

        IconButton(
            onClick = {
                flashEnabled = !flashEnabled
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    top = 72.dp,
                    end = 18.dp
                )
        ) {
            Icon(
                imageVector = if (flashEnabled) {
                    Icons.Default.FlashOn
                } else {
                    Icons.Default.FlashOff
                },
                contentDescription =
                    "Ativar ou desativar lanterna",
                tint = ScannerAccent
            )
        }
    }
}

@Composable
private fun ScannerHeader(
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.Black.copy(alpha = 0.58f)
            )
            .padding(
                horizontal = 8.dp,
                vertical = 14.dp
            )
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(
                Alignment.CenterStart
            )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White
            )
        }

        Text(
            text = "Ler endereço da entrega",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(
                Alignment.Center
            )
        )
    }
}

@Composable
private fun ScannerInstructions(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Color.Black.copy(alpha = 0.68f)
            )
            .padding(
                horizontal = 24.dp,
                vertical = 24.dp
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = null,
            tint = ScannerAccent,
            modifier = Modifier.size(38.dp)
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Text(
            text = "Aponte para o QR Code ou código de barras",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = "A parada é adicionada à rota automaticamente.",
            color = Color.White.copy(alpha = 0.78f),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun PermissionDeniedContent(
    onBack: () -> Unit,
    onRequestAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = null,
            tint = ScannerAccent,
            modifier = Modifier.size(68.dp)
        )

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        Text(
            text = "Permissão da câmera necessária",
            color = Color.White,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Text(
            text = "O JBank precisa da câmera para ler o endereço da entrega.",
            color = Color.White.copy(alpha = 0.80f),
            fontSize = 15.sp
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Button(
            onClick = onRequestAgain,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ScannerAccent,
                contentColor = RotaColors.OnAccent
            )
        ) {
            Text("Permitir câmera")
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ScannerDark
            )
        ) {
            Text("Voltar")
        }
    }
}
