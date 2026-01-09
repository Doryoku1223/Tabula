package com.tabula.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tabula.viewmodel.TabulaViewModel
import com.tabula.R

@Composable
fun TabulaScreen(
    viewModel: TabulaViewModel
) {
    val state by viewModel.uiState.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
    val bg = MaterialTheme.colorScheme.background
    val fg = MaterialTheme.colorScheme.onBackground

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
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(
                            R.string.session_index_format,
                            state.currentIndex,
                            state.totalCount
                        ),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = fg
                    )
                    Row {
                        IconButton(onClick = { viewModel.openReview() }) {
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

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    if (state.isSessionComplete && state.trashBin.isNotEmpty()) {
                        ReviewScreen(
                            trashBin = state.trashBin,
                            onConfirmBurn = { viewModel.confirmBurn() },
                            onRestoreSelected = { selected -> viewModel.restoreSelected(selected) },
                            onDeleteSelected = { selected -> viewModel.deleteSelected(selected) }
                        )
                    } else if (state.currentPhoto == null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = fg,
                                modifier = Modifier
                                    .size(72.dp)
                                    .padding(bottom = 16.dp)
                            )
                            Text(
                                text = stringResource(R.string.session_complete_title),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = fg
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(
                                    R.string.session_complete_subtitle,
                                    state.totalCount
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = fg
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.refreshSession() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = fg,
                                    contentColor = bg
                                )
                            ) {
                                Text(text = stringResource(R.string.action_change_set))
                            }
                        }
                    } else {
                        SwipeableStack(
                            currentPhoto = state.currentPhoto,
                            nextPhoto = state.nextPhoto,
                            onSwipeUp = {
                                state.currentPhoto?.let { viewModel.markForDeletion(it) }
                            },
                            onSwipeSide = {
                                state.currentPhoto?.let { viewModel.keepPhoto(it) }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (state.currentPhoto != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.refreshSession() },
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text(text = stringResource(R.string.action_change_set), color = fg)
                        }
                    }
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
