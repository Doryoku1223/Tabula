package com.tabula.data

import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Photo(
    val id: Long,
    val uri: Uri,
    val dateAdded: Long
) {
    val dateString: String
        get() {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            return formatter.format(Date(dateAdded * 1000L))
        }
}
