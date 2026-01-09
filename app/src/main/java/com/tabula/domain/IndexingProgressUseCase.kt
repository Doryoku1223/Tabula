package com.tabula.domain

import com.tabula.data.PhotoDataSource
import kotlinx.coroutines.flow.StateFlow

class IndexingProgressUseCase(
    private val dataSource: PhotoDataSource
) {
    val progress: StateFlow<Int> = dataSource.indexingProgress
}
