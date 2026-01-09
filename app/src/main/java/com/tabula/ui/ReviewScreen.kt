package com.tabula.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import com.tabula.data.Photo
import com.tabula.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReviewScreen(
    trashBin: List<Photo>,
    onConfirmBurn: () -> Unit,
    onRestoreSelected: (List<Photo>) -> Unit,
    onDeleteSelected: (List<Photo>) -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = MaterialTheme.colorScheme.background
    val fg = MaterialTheme.colorScheme.onBackground
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var zoomPhoto by remember { mutableStateOf<Photo?>(null) }
    var showBurnConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.review_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = fg
            )
            val countText = if (selectionMode) {
                stringResource(R.string.review_selected_format, selectedIds.size, trashBin.size)
            } else {
                stringResource(R.string.review_count_format, trashBin.size)
            }
            Text(
                text = countText,
                style = MaterialTheme.typography.bodyMedium,
                color = fg
            )
        }

        if (trashBin.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.review_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = fg.copy(alpha = 0.7f)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(trashBin, key = { it.id }) { photo ->
                    val isSelected = selectedIds.contains(photo.id)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .combinedClickable(
                                onClick = {
                                    if (selectionMode) {
                                        selectedIds = toggleSelected(selectedIds, photo.id)
                                    }
                                },
                                onLongClick = {
                                    if (!selectionMode) {
                                        selectionMode = true
                                        selectedIds = selectedIds + photo.id
                                    }
                                },
                                onDoubleClick = { zoomPhoto = photo }
                            )
                    ) {
                        AsyncImage(
                            model = photo.uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        if (selectionMode) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .border(1.5.dp, fg, CircleShape)
                                    .background(
                                        color = if (isSelected) fg else bg,
                                        shape = CircleShape
                                    )
                                    .padding(4.dp)
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = stringResource(R.string.content_desc_selected),
                                        tint = bg
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectionMode) {
            val selectedPhotos = trashBin.filter { selectedIds.contains(it.id) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onRestoreSelected(selectedPhotos)
                        selectionMode = false
                        selectedIds = emptySet()
                    },
                    enabled = selectedPhotos.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.action_restore_format, selectedPhotos.size),
                        color = fg
                    )
                }
                Button(
                    onClick = {
                        onDeleteSelected(selectedPhotos)
                        selectionMode = false
                        selectedIds = emptySet()
                    },
                    enabled = selectedPhotos.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = fg,
                        contentColor = bg
                    )
                ) {
                    Text(text = stringResource(R.string.action_delete_format, selectedPhotos.size))
                }
            }
        } else if (trashBin.isNotEmpty()) {
            Button(
                onClick = { showBurnConfirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = fg,
                    contentColor = bg
                )
            ) {
                Text(text = stringResource(R.string.action_burn_trash))
            }
        }
    }

    if (zoomPhoto != null) {
        FullScreenImageDialog(photo = zoomPhoto!!, onDismiss = { zoomPhoto = null })
    }

    if (showBurnConfirm) {
        AlertDialog(
            onDismissRequest = { showBurnConfirm = false },
            title = {
                Text(text = stringResource(R.string.confirm_delete_title, trashBin.size))
            },
            text = {
                Text(text = stringResource(R.string.confirm_delete_message))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBurnConfirm = false
                        onConfirmBurn()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = fg,
                        contentColor = bg
                    )
                ) {
                    Text(text = stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showBurnConfirm = false }) {
                    Text(text = stringResource(R.string.action_cancel), color = fg)
                }
            }
        )
    }
}

private fun toggleSelected(current: Set<Long>, id: Long): Set<Long> {
    return if (current.contains(id)) current - id else current + id
}

// FullScreenImageDialog handles zooming and tap-to-dismiss.
