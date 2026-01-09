package com.tabula.ui

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.aspectRatio
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.tabula.viewmodel.TabulaViewModel
import com.tabula.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas

@Composable
fun TabulaScreen(
    viewModel: TabulaViewModel
) {
    val state by viewModel.uiState.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
    var showReview by remember { mutableStateOf(false) }
    val bg = MaterialTheme.colorScheme.background
    val fg = MaterialTheme.colorScheme.onBackground
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val readImages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            results[Manifest.permission.READ_MEDIA_IMAGES] ?: false
        } else {
            results[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
        }
        val visualSelected = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            results[Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED] ?: false
        } else {
            false
        }
        hasPermission = readImages || visualSelected
        viewModel.updateMediaAccess(readImages, visualSelected)
        if (hasPermission) {
            viewModel.rescanLibrary()
        }
    }

    LaunchedEffect(Unit) {
        val readImagesGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        val visualGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        hasPermission = readImagesGranted || visualGranted
        viewModel.updateMediaAccess(readImagesGranted, visualGranted)
        if (hasPermission) {
            viewModel.rescanLibrary()
        }
    }

    LaunchedEffect(state.isSessionComplete) {
        if (state.isSessionComplete) {
            showReview = false
        }
    }

    if (showSettings) {
        SettingsScreen(
            sessionSize = state.sessionSize,
            curationMode = state.curationMode,
            themeMode = state.themeMode,
            languageMode = state.languageMode,
            isLimitedAccess = state.isLimitedAccess,
            onBack = { showSettings = false },
            onSessionSizeChange = { viewModel.updateSessionSize(it) },
            onCurationModeChange = { viewModel.updateCurationMode(it) },
            onThemeModeChange = { viewModel.updateThemeMode(it) },
            onLanguageModeChange = { viewModel.updateLanguage(it) },
            onRescanLibrary = { viewModel.rescanLibrary() }
        )
    } else if (!hasPermission) {
        PermissionRequest(
            onRequestPermission = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                        )
                    )
                } else {
                    permissionLauncher.launch(
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    )
                }
            }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val titleText = remember(state.currentPhoto?.id, state.currentPhoto?.dateAdded) {
                        val timestamp = state.currentPhoto?.dateAdded?.times(1000L)
                        val formatter = SimpleDateFormat("yyyy MMM", Locale.ENGLISH)
                        if (timestamp != null) {
                            formatter.format(Date(timestamp))
                        } else {
                            formatter.format(Date())
                        }
                    }
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = fg
                    )
                    Row {
                        IconButton(onClick = { showReview = !showReview }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.content_desc_review),
                                tint = fg
                            )
                        }
                        IconButton(onClick = { showSettings = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.content_desc_settings),
                                tint = fg
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val shouldShowReview = showReview
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (shouldShowReview) {
                        ReviewScreen(
                            trashBin = state.trashBin,
                            onConfirmBurn = { viewModel.confirmBurn() },
                            onRestoreSelected = { selected -> viewModel.restoreSelected(selected) },
                            onDeleteSelected = { selected -> viewModel.deleteSelected(selected) }
                        )
                    } else if (state.currentPhoto == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            ConfettiOverlay(
                                isActive = state.isSessionComplete,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(0.9f)
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.session_summary_title),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = fg
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(
                                        R.string.session_summary_reviewed,
                                        state.totalCount
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = fg
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stringResource(
                                        R.string.session_summary_marked,
                                        state.sessionMarkedCount
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = fg
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = {
                                        showReview = false
                                        viewModel.refreshSession()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = fg,
                                        contentColor = bg
                                    )
                                ) {
                                    Text(text = stringResource(R.string.action_next_set))
                                }
                            }
                        }
                    } else {
                        SwipeableStack(
                            previousPhoto = state.previousPhoto,
                            currentPhoto = state.currentPhoto,
                            nextPhoto = state.nextPhoto,
                            onSwipeUp = {
                                state.currentPhoto?.let { viewModel.markForDeletion(it) }
                            },
                            onSwipeLeft = { viewModel.showNext() },
                            onSwipeRight = { viewModel.showPrevious() },
                            modifier = Modifier
                                .fillMaxWidth(0.94f)
                                .aspectRatio(3f / 4f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (state.currentPhoto != null && !shouldShowReview) {
                    val remaining = (state.photoStack.size - state.currentIndex + 1).coerceAtLeast(0)
                    Text(
                        text = stringResource(R.string.session_remaining_format, remaining),
                        style = MaterialTheme.typography.bodyLarge,
                        color = fg,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            AnimatedVisibility(
                visible = state.isIndexing,
                enter = slideInVertically { it / 2 } + fadeIn(),
                exit = slideOutVertically { it / 2 } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding(),
                        start = 24.dp,
                        end = 24.dp
                    )
            ) {
                Surface(
                    color = bg,
                    contentColor = fg,
                    shadowElevation = 6.dp,
                    tonalElevation = 0.dp,
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.indexing_library),
                            style = MaterialTheme.typography.bodyMedium,
                            color = fg
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { state.indexingProgress / 100f },
                            color = fg,
                            trackColor = fg.copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfettiOverlay(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isActive) return
    val progress = remember(isActive) { Animatable(0f) }
    val particles = remember(isActive) { generateConfetti() }

    LaunchedEffect(isActive) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200)
        )
    }

    Canvas(modifier = modifier) {
        val canvasSize = size
        particles.forEach { particle ->
            val y = particle.startY + (canvasSize.height + 200f) * progress.value
            val x = particle.startX * canvasSize.width + sin(progress.value * 6f + particle.phase) * 24f
            val alpha = 1f - abs(progress.value - 0.6f).coerceIn(0f, 1f)
            rotate(particle.rotation + progress.value * 180f, Offset(x, y)) {
                drawRect(
                    color = particle.color.copy(alpha = alpha.coerceIn(0.15f, 1f)),
                    topLeft = Offset(x, y),
                    size = Size(particle.size, particle.size * 0.6f)
                )
            }
        }
    }
}

private data class ConfettiParticle(
    val startX: Float,
    val startY: Float,
    val size: Float,
    val color: Color,
    val rotation: Float,
    val phase: Float
)

private fun generateConfetti(): List<ConfettiParticle> {
    val palette = listOf(
        Color(0xFFFC6DAB),
        Color(0xFFF9A826),
        Color(0xFF5BC0EB),
        Color(0xFF9BE15D),
        Color(0xFFFDE74C)
    )
    return List(36) {
        ConfettiParticle(
            startX = Random.nextFloat(),
            startY = -Random.nextFloat() * 400f,
            size = 10f + Random.nextFloat() * 10f,
            color = palette[Random.nextInt(palette.size)],
            rotation = Random.nextFloat() * 360f,
            phase = Random.nextFloat() * 6f
        )
    }
}

@Composable
private fun PermissionRequest(
    onRequestPermission: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.permission_photos),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text(text = stringResource(R.string.permission_grant))
            }
        }
    }
}
