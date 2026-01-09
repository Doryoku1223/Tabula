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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min

@Composable
fun SwipeableStack(
    previousPhoto: Photo?,
    currentPhoto: Photo?,
    nextPhoto: Photo?,
    onSwipeUp: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val animX = remember(currentPhoto?.id) { Animatable(0f) }
    val animY = remember(currentPhoto?.id) { Animatable(0f) }
    var dragX by remember(currentPhoto?.id) { mutableStateOf(0f) }
    var dragY by remember(currentPhoto?.id) { mutableStateOf(0f) }
    var isDragging by remember(currentPhoto?.id) { mutableStateOf(false) }
    var showZoom by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier) {
        val widthPx = with(LocalDensity.current) { maxWidth.toPx() }.coerceAtLeast(1f)
        val heightPx = with(LocalDensity.current) { maxHeight.toPx() }.coerceAtLeast(1f)
        val offsetX = if (isDragging) dragX else animX.value
        val offsetY = if (isDragging) dragY else animY.value
        val progress = min(abs(offsetX) / widthPx, 1f)
        val backScale = lerp(0.92f, 1.0f, progress)
        val backAlpha = lerp(0.7f, 1.0f, progress)
        val backPhoto = if (offsetX > 0f) previousPhoto else nextPhoto

        Box(modifier = Modifier.fillMaxSize()) {
            if (backPhoto != null) {
                PhotoCard(
                    photo = backPhoto,
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
                        translationX = offsetX,
                        translationY = offsetY,
                        rotationZ = offsetX / 18f
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { showZoom = true })
                    }
                    .pointerInput(currentPhoto.id) {
                        detectDragGestures(
                            onDragStart = {
                                scope.launch {
                                    isDragging = true
                                    dragX = animX.value
                                    dragY = animY.value
                                    animX.stop()
                                    animY.stop()
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragX += dragAmount.x
                                dragY += dragAmount.y
                            },
                            onDragEnd = {
                                settleDrag(
                                    endOffsetX = dragX,
                                    endOffsetY = dragY,
                                    widthPx = widthPx,
                                    heightPx = heightPx,
                                    animX = animX,
                                    animY = animY,
                                    onSwipeUp = {
                                        onSwipeUp()
                                        dragX = 0f
                                        dragY = 0f
                                    },
                                    onSwipeLeft = {
                                        onSwipeLeft()
                                        dragX = 0f
                                        dragY = 0f
                                    },
                                    onSwipeRight = {
                                        onSwipeRight()
                                        dragX = 0f
                                        dragY = 0f
                                    },
                                    onReset = {
                                        dragX = 0f
                                        dragY = 0f
                                    },
                                    setDragging = { isDragging = it },
                                    scope = scope
                                )
                            },
                            onDragCancel = {
                                settleDrag(
                                    endOffsetX = dragX,
                                    endOffsetY = dragY,
                                    widthPx = widthPx,
                                    heightPx = heightPx,
                                    animX = animX,
                                    animY = animY,
                                    onSwipeUp = { dragX = 0f; dragY = 0f },
                                    onSwipeLeft = { dragX = 0f; dragY = 0f },
                                    onSwipeRight = { dragX = 0f; dragY = 0f },
                                    onReset = { dragX = 0f; dragY = 0f },
                                    setDragging = { isDragging = it },
                                    scope = scope
                                )
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

private fun settleDrag(
    endOffsetX: Float,
    endOffsetY: Float,
    widthPx: Float,
    heightPx: Float,
    animX: Animatable<Float, *>,
    animY: Animatable<Float, *>,
    onSwipeUp: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onReset: () -> Unit,
    setDragging: (Boolean) -> Unit,
    scope: kotlinx.coroutines.CoroutineScope
) {
    scope.launch {
        animX.snapTo(endOffsetX)
        animY.snapTo(endOffsetY)
        setDragging(false)
        when {
            endOffsetY < -heightPx * 0.22f -> {
                animY.animateTo(
                    targetValue = -heightPx * 1.2f,
                    animationSpec = tween(
                        durationMillis = 180,
                        easing = FastOutLinearInEasing
                    )
                )
                onSwipeUp()
                animX.snapTo(0f)
                animY.snapTo(0f)
            }
            abs(endOffsetX) > widthPx * 0.22f -> {
                val direction = if (endOffsetX > 0) 1f else -1f
                animX.animateTo(
                    targetValue = direction * widthPx * 1.2f,
                    animationSpec = tween(
                        durationMillis = 180,
                        easing = FastOutLinearInEasing
                    )
                )
                animY.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing)
                )
                if (direction > 0) {
                    onSwipeRight()
                } else {
                    onSwipeLeft()
                }
                animX.snapTo(0f)
                animY.snapTo(0f)
            }
            else -> {
                animX.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing)
                )
                animY.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing)
                )
                onReset()
            }
        }
    }
}
