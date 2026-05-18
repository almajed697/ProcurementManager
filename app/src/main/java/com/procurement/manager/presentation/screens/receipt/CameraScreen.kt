package com.procurement.manager.presentation.screens.receipt

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.procurement.manager.presentation.theme.PrimaryBlue
import com.procurement.manager.util.ImageUtils
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    receiptNumber: String,
    onImageCaptured: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var imageCaptureUseCase by remember { mutableStateOf<ImageCapture?>(null) }
    var flashEnabled by remember { mutableStateOf(false) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("التقاط صورة الوصل", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { flashEnabled = !flashEnabled }) {
                        Icon(
                            if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            "فلاش",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            // Camera Preview
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    setupCamera(ctx, lifecycleOwner, previewView, flashEnabled) { capture ->
                        imageCaptureUseCase = capture
                    }
                    previewView
                },
                update = { previewView ->
                    setupCamera(context, lifecycleOwner, previewView, flashEnabled) { capture ->
                        imageCaptureUseCase = capture
                    }
                }
            )

            // Capture Button
            Box(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        captureImage(
                            context = context,
                            imageCapture = imageCaptureUseCase,
                            receiptNumber = receiptNumber,
                            executor = cameraExecutor,
                            onImageSaved = onImageCaptured
                        )
                    },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Camera, "التقاط", tint = PrimaryBlue, modifier = Modifier.size(40.dp))
                }
            }
        }
    }
}

private fun setupCamera(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    flashEnabled: Boolean,
    onCaptureReady: (ImageCapture) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        try {
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setFlashMode(if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
                .build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )
            onCaptureReady(imageCapture)
        } catch (e: Exception) {
            Log.e("CameraScreen", "Camera setup failed", e)
        }
    }, ContextCompat.getMainExecutor(context))
}

private fun captureImage(
    context: Context,
    imageCapture: ImageCapture?,
    receiptNumber: String,
    executor: ExecutorService,
    onImageSaved: (String) -> Unit
) {
    val imageCapture = imageCapture ?: return
    val file = ImageUtils.createReceiptImageFile(context, receiptNumber.ifBlank { "photo" })
    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onImageSaved(file.absolutePath)
            }
            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraScreen", "Capture failed", exception)
            }
        }
    )
}
