package com.tabula.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.app.RecoverableSecurityException
import android.app.PendingIntent
import android.os.Build
import androidx.annotation.RequiresApi
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PhotoRepository(private val context: Context) : PhotoDataSource {

    private val database = TabulaDatabase.getInstance(context)
    private val photoDao = database.photoDao()

    private val _indexingProgress = MutableStateFlow(100)
    override val indexingProgress: StateFlow<Int> = _indexingProgress.asStateFlow()

    override suspend fun getPhotos(offset: Int, limit: Int, mode: CurationMode): List<Photo> {
        return when (mode) {
            CurationMode.RANDOM -> {
                photoDao.getRandomPhotos(limit).map { it.toPhoto() }
            }
            CurationMode.BURST -> {
                getBurstSession(limit)
            }
        }
    }

    override suspend fun deletePhotos(uris: List<String>) {
        if (uris.isEmpty()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pendingIntent = createBatchDeleteRequest(uris)
            throw DeletePermissionRequired(pendingIntent)
        }
        for (uriValue in uris) {
            try {
                val deleted = context.contentResolver.delete(Uri.parse(uriValue), null, null)
                if (deleted <= 0) {
                    throw IllegalStateException("Failed to delete uri=$uriValue")
                }
            } catch (e: RecoverableSecurityException) {
                throw e
            }
        }
    }

    override suspend fun refreshIndex() {
        _indexingProgress.value = 0

        val projection = arrayOf(
            Images.Media._ID,
            Images.Media.DATE_TAKEN,
            Images.Media.DATE_ADDED
        )

        val cursor = context.contentResolver.query(
            Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${Images.Media.DATE_TAKEN} ASC"
        )

        if (cursor == null) {
            _indexingProgress.value = 100
            return
        }

        val idIndex = cursor.getColumnIndexOrThrow(Images.Media._ID)
        val dateTakenIndex = cursor.getColumnIndexOrThrow(Images.Media.DATE_TAKEN)
        val dateAddedIndex = cursor.getColumnIndexOrThrow(Images.Media.DATE_ADDED)
        val total = cursor.count.coerceAtLeast(1)
        val buffer = ArrayList<PhotoEntity>(total)
        var processed = 0

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val dateTaken = it.getLong(dateTakenIndex).let { value ->
                    if (value > 0) value else it.getLong(dateAddedIndex) * 1000L
                }
                val uri = ContentUris.withAppendedId(Images.Media.EXTERNAL_CONTENT_URI, id).toString()
                buffer.add(PhotoEntity(id = id, dateTaken = dateTaken, uri = uri))

                processed++
                val progress = (processed * 100) / total
                if (progress != _indexingProgress.value) {
                    _indexingProgress.value = progress
                }
            }
        }

        photoDao.deleteAll()
        if (buffer.isNotEmpty()) {
            photoDao.insertAll(buffer)
        }

        _indexingProgress.value = 100
    }

    private suspend fun getBurstSession(limit: Int): List<Photo> {
        val allPhotos = photoDao.getAllPhotosByDateAsc()
        if (allPhotos.isEmpty()) return emptyList()

        val groups = ArrayList<List<PhotoEntity>>()
        var current = ArrayList<PhotoEntity>()

        for (photo in allPhotos) {
            if (current.isEmpty()) {
                current.add(photo)
                continue
            }
            val prev = current.last()
            val diff = photo.dateTaken - prev.dateTaken
            if (diff < 2000L) {
                current.add(photo)
            } else {
                if (current.size > 3) {
                    groups.add(current.toList())
                }
                current = arrayListOf(photo)
            }
        }
        if (current.size > 3) {
            groups.add(current.toList())
        }

        if (groups.isEmpty()) return emptyList()

        val session = ArrayList<Photo>(limit)
        for (group in groups) {
            for (photo in group) {
                if (session.size >= limit) break
                session.add(photo.toPhoto())
            }
            if (session.size >= limit) break
        }
        return session
    }

    private fun PhotoEntity.toPhoto(): Photo {
        val uriValue = ContentUris.withAppendedId(
            Images.Media.EXTERNAL_CONTENT_URI,
            id
        )
        val dateAddedSeconds = dateTaken / 1000L
        return Photo(id = id, uri = uriValue, dateAdded = dateAddedSeconds)
    }
    @RequiresApi(Build.VERSION_CODES.R)
    private fun createBatchDeleteRequest(uris: List<String>): PendingIntent {
        val uriList = uris.map { Uri.parse(it) }
        return MediaStore.createDeleteRequest(context.contentResolver, uriList)
    }
}

class DeletePermissionRequired(val pendingIntent: PendingIntent) : Exception()
