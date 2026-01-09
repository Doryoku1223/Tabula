package com.tabula.domain

import com.tabula.data.CurationMode
import com.tabula.data.Photo
import com.tabula.data.PhotoDataSource

class GetSessionUseCase(
    private val dataSource: PhotoDataSource
) {
    suspend operator fun invoke(
        offset: Int,
        mode: CurationMode = CurationMode.RANDOM,
        sessionSize: Int = 15
    ): List<Photo> {
        return dataSource.getPhotos(offset, limit = sessionSize, mode = mode)
    }
}
