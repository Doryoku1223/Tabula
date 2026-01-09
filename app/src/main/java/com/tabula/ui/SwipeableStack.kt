package com.tabula.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.util.lerp
import com.tabula.data.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import kotlin.math.abs
import kotlin.math.min

@Composable
fun SwipeableStack(
    currentPhoto: Photo?,
    nextPhoto: Photo?,
    onSwipeUp: () -> Unit,
    onSwipeSide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    var showZoom by remember { mutableStateOf(false) }
    var forceBackVisible by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthPx = with(LocalDensity.current) { maxWidth.toPx() }.coerceAtLeast(1f)
        val heightPx = with(LocalDensity.current) { maxHeight.toPx() }.coerceAtLeast(1f)
        val progress = if (forceBackVisible) 1f else min(abs(offsetX.value) / widthPx, 1f)
        val backScale = lerp(0.9f, 1.0f, progress)
        val backAlpha = lerp(0.8f, 1.0f, progress)

        Box(modifier = Modifier.fillMaxSize()) {
            if (nextPhoto != null) {
                PhotoCard(
                    photo = nextPhoto,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = backScale,
                            scaleY = backScale,
                            alpha = backAlpha
                        )
                )
            }

            if (currentPhoto != null) {
                key(currentPhoto.id) {
                    PhotoCard(
                        photo = currentPhoto,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                translationX = offsetX.value,
                                translationY = offsetY.value,
                                rotationZ = offsetX.value / 15f
                            )
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { showZoom = true })
                            }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        scope.launch {
                                            offsetX.snapTo(offsetX.value + dragAmount.x)
                                            offsetY.snapTo(offsetY.value + dragAmount.y)
                                        }
                                    },
                                    onDragEnd = {
                                        val endOffsetX = offsetX.value
                                        val endOffsetY = offsetY.value
                                        when {
                                            endOffsetY < -300f -> {
                                                scope.launch(Dispatchers.Default) {
                                                    withContext(Dispatchers.Main) {
                                                        forceBackVisible = true
                                                        offsetY.animateTo(
                                                            targetValue = -heightPx * 1.5f,
                                                            animationSpec = spring(stiffness = Spring.StiffnessLow)
                                                        )
                                                    }
                                                    delay(50)
                                                    withContext(Dispatchers.Main) {
                                                        onSwipeUp()
                                                        offsetX.snapTo(0f)
                                                        offsetY.snapTo(0f)
                                                        forceBackVisible = false
                                                    }
                                                }
                                            }
                                            abs(endOffsetX) > 300f -> {
                                                val direction = if (endOffsetX > 0) 1f else -1f
                                                scope.launch(Dispatchers.Default) {
                                                    withContext(Dispatchers.Main) {
                                                        forceBackVisible = true
                                                        offsetX.animateTo(
                                                            targetValue = direction * widthPx * 1.5f,
                                                            animationSpec = spring(stiffness = Spring.StiffnessLow)
                                                        )
                                                    }
                                                    delay(50)
                                                    withContext(Dispatchers.Main) {
                                                        onSwipeSide()
                                                        offsetX.snapTo(0f)
                                                        offsetY.snapTo(0f)
                                                        forceBackVisible = false
                                                    }
                                                }
                                            }
                                            else -> {
                                                scope.launch {
                                                    offsetX.animateTo(
                                                        targetValue = 0f,
                                                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                                                    )
                                                    offsetY.animateTo(
                                                        targetValue = 0f,
                                                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                    )
                }
            }
        }
    }

    if (showZoom && currentPhoto != null) {
        FullScreenImageDialog(photo = currentPhoto, onDismiss = { showZoom = false })
    }
}
