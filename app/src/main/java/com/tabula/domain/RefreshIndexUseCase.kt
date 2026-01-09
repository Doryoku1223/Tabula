package com.tabula.domain

import com.tabula.data.PhotoDataSource

class RefreshIndexUseCase(
    private val dataSource: PhotoDataSource
) {
    suspend operator fun invoke() {
        dataSource.refreshIndex()
    }
}
