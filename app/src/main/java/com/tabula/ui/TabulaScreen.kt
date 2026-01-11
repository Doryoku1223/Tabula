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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Angle
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.PartyFactory
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Spread
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Size
import java.util.concurrent.TimeUnit

@Composable
fun TabulaScreen(
    viewModel: TabulaViewModel,
    onOpenSettings: () -> Unit,
    onOpenReview: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
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

    if (!hasPermission) {
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
                        IconButton(onClick = onOpenReview) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.content_desc_review),
                                tint = fg
                            )
                        }
                        IconButton(onClick = onOpenSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.content_desc_settings),
                                tint = fg
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.currentPhoto == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            SummaryConfetti(
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

                if (state.currentPhoto != null) {
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
private fun SummaryConfetti(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isActive) return
    val parties = remember(isActive) {
        if (isActive) listOf(createSummaryParty()) else emptyList()
    }
    KonfettiView(
        modifier = modifier,
        parties = parties
    )
}

private fun createSummaryParty(): Party {
    val colors = listOf(
        Color(0xFFFC6DAB).toArgb(),
        Color(0xFFF9A826).toArgb(),
        Color(0xFF5BC0EB).toArgb(),
        Color(0xFF9BE15D).toArgb(),
        Color(0xFFFDE74C).toArgb()
    )
    val emitter = Emitter(duration = 220, timeUnit = TimeUnit.MILLISECONDS)
        .max(140)
    return PartyFactory(emitter)
        .angle(Angle.BOTTOM)
        .spread(Spread.WIDE)
        .setSpeedBetween(6f, 22f)
        .setDamping(0.85f)
        .sizes(listOf(Size.SMALL, Size.MEDIUM, Size.LARGE))
        .shapes(listOf(Shape.Square, Shape.Rectangle(0.6f), Shape.Circle))
        .colors(colors)
        .timeToLive(2200L)
        .fadeOutEnabled(true)
        .position(Position.Relative(0.5, 0.0))
        .build()
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
