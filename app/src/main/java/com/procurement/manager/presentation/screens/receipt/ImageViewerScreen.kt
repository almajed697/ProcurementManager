package com.procurement.manager.presentation.screens.receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageViewerScreen(
    imagePath: String,
    onNavigateBack: () -> Unit
) {
    var scale       by remember { mutableFloatStateOf(1f) }
    var offset      by remember { mutableStateOf(Offset.Zero) }
    val minScale    = 1f
    val maxScale    = 5f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("عرض الصورة", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { scale = (scale + 0.5f).coerceAtMost(maxScale) }) {
                        Icon(Icons.Default.ZoomIn, "تكبير", tint = Color.White)
                    }
                    IconButton(onClick = { scale = (scale - 0.5f).coerceAtLeast(minScale); if (scale == minScale) offset = Offset.Zero }) {
                        Icon(Icons.Default.ZoomOut, "تصغير", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = File(imagePath),
                contentDescription = "صورة الوصل",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(minScale, maxScale)
                            val newOffset = offset + pan * scale
                            offset = newOffset
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(onDoubleTap = {
                            if (scale > minScale) { scale = minScale; offset = Offset.Zero }
                            else scale = 2.5f
                        })
                    }
            )

            // Hint
            if (scale == minScale) {
                Text(
                    "انقر مرتين للتكبير • اسحب للتحريك",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
                )
            }
        }
    }
}
