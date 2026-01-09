package com.tabula.domain

import com.tabula.data.PhotoDataSource

class DeletePhotosUseCase(
    private val dataSource: PhotoDataSource
) {
    suspend operator fun invoke(uris: List<String>) {
        dataSource.deletePhotos(uris)
    }
}
