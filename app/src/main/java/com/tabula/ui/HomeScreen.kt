package com.tabula.ui

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tabula.R
import com.tabula.data.Photo
import com.tabula.viewmodel.HomeViewModel
import kotlin.math.abs
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.zIndex

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onSettingsClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val backgroundColor = Color.White
    val current = uiState.photos.firstOrNull()
    val next = uiState.photos.getOrNull(1)

    val context = LocalContext.current
    val permission = remember {
        if (Build.VERSION.SDK_INT >= 33) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }
    val visualPermission = remember {
        if (Build.VERSION.SDK_INT >= 33) {
            android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        } else {
            null
        }
    }
    var hasPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            viewModel.loadSessionIfNeeded()
        }
    }
    val multiPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val readImages = results[android.Manifest.permission.READ_MEDIA_IMAGES] ?: false
        val visual = results[android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED] ?: false
        hasPermission = readImages || visual
        if (hasPermission) {
            viewModel.loadSessionIfNeeded()
        }
    }

    LaunchedEffect(Unit) {
        val readImagesGranted = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val visualGranted = if (Build.VERSION.SDK_INT >= 33 && visualPermission != null) {
            ContextCompat.checkSelfPermission(
                context,
                visualPermission
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        hasPermission = readImagesGranted || visualGranted
        if (hasPermission) {
            viewModel.loadSessionIfNeeded()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 24.dp, end = 24.dp)
                .zIndex(2f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = uiState.monthLabel,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.Black
                    )
                }
                IconButton(onClick = {
                    Toast.makeText(context, "Settings Clicked", Toast.LENGTH_SHORT).show()
                    onSettingsClick()
                }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color.Black
                    )
                }
            }
        }

        when {
            !hasPermission -> {
                HomePermissionCard(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= 33 && visualPermission != null) {
                            multiPermissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.READ_MEDIA_IMAGES,
                                    android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                                )
                            )
                        } else {
                            permissionLauncher.launch(permission)
                        }
                    }
                )
            }
            uiState.isLoading -> {
                Text(
                    text = stringResource(R.string.home_loading),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Black
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.photos.isEmpty() -> {
                SessionSummaryView(
                    total = viewModel.totalReviewed,
                    kept = viewModel.keptCount,
                    onRestart = { viewModel.loadNextBatch() }
                )
            }
            else -> {
                SwipeablePhotoStack(
                    current = current,
                    next = next,
                    onSwipeLeft = { photo -> viewModel.swipeLeft(photo) },
                    onSwipeRight = { photo -> viewModel.swipeRight(photo) }
                )
            }
        }

        Text(
            text = stringResource(R.string.home_photos_left, uiState.photos.size),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.Black
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

@Composable
private fun SwipeablePhotoStack(
    current: Photo?,
    next: Photo?,
    onSwipeLeft: (Photo) -> Unit,
    onSwipeRight: (Photo) -> Unit
) {
    val shape = RoundedCornerShape(32.dp)
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    val scope = rememberCoroutineScope()

    val offsetX = remember(current?.id) { Animatable(0f) }
    val offsetY = remember(current?.id) { Animatable(0f) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (next != null) {
            HomePhotoCard(
                photo = next,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(9f / 16f)
                    .graphicsLayer {
                        scaleX = 0.95f
                        scaleY = 0.95f
                        alpha = 0.9f
                    },
                shape = shape
            )
        }

        if (current != null) {
            HomePhotoCard(
                photo = current,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(9f / 16f)
                    .graphicsLayer {
                        translationX = offsetX.value
                        translationY = offsetY.value
                        rotationZ = offsetX.value / 20f
                    }
                    .alpha(1f)
                    .pointerInput(current.id) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    offsetX.snapTo(offsetX.value + dragAmount.x)
                                    offsetY.snapTo(offsetY.value + dragAmount.y)
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    val target = when {
                                        abs(offsetX.value) > screenWidthPx * 0.25f -> {
                                            if (offsetX.value > 0) {
                                                Offset(screenWidthPx * 1.2f, 0f)
                                            } else {
                                                Offset(-screenWidthPx * 1.2f, 0f)
                                            }
                                        }
                                        else -> Offset.Zero
                                    }

                                    if (target != Offset.Zero) {
                                        offsetX.animateTo(target.x, animationSpec = spring())
                                        offsetY.animateTo(0f, animationSpec = spring())
                                        if (target.x > 0) {
                                            onSwipeRight(current)
                                        } else {
                                            onSwipeLeft(current)
                                        }
                                        offsetX.snapTo(0f)
                                        offsetY.snapTo(0f)
                                    } else {
                                        offsetX.animateTo(0f, animationSpec = spring())
                                        offsetY.animateTo(0f, animationSpec = spring())
                                    }
                                }
                            }
                        )
                    },
                shape = shape
            )
        }
    }
}

@Composable
private fun HomePhotoCard(
    photo: Photo,
    modifier: Modifier,
    shape: RoundedCornerShape
) {
    val context = LocalContext.current
    val request = ImageRequest.Builder(context)
        .data(photo.uri)
        .crossfade(true)
        .build()

    Box(
        modifier = modifier
            .background(Color(0xFFE0E0E0), shape)
            .clip(shape)
    ) {
        AsyncImage(
            model = request,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(Color(0xFFE0E0E0)),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun HomePermissionCard(
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(32.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .aspectRatio(9f / 16f)
            .background(Color(0xFFE0E0E0), shape)
            .clip(shape),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        ) {
            Text(
                text = stringResource(R.string.home_permission_action),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun SessionSummaryView(
    total: Int,
    kept: Int,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Session Complete",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = "Total Reviewed: $total",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = "Kept: $kept",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
        )
        Spacer(modifier = Modifier.size(20.dp))
        Button(
            onClick = onRestart,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            )
        ) {
            Text(text = "Load Next Batch")
        }
    }
}
