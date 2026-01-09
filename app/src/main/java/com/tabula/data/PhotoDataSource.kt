package com.tabula.data

import kotlinx.coroutines.flow.StateFlow

interface PhotoDataSource {
    val indexingProgress: StateFlow<Int>
    suspend fun getPhotos(offset: Int, limit: Int, mode: CurationMode): List<Photo>
    suspend fun deletePhotos(uris: List<String>)
    suspend fun refreshIndex()
}
