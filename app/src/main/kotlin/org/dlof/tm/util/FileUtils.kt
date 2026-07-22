package org.dlof.tm.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.File

object FileUtils {

    /** يستخرج اسم الملف المعروض من Uri محتوى (content://). */
    fun displayName(context: Context, uri: Uri): String {
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) {
                name = cursor.getString(idx)
            }
        }
        return name ?: (uri.lastPathSegment ?: "file_${System.currentTimeMillis()}")
    }

    /** يهيّئ اسم ملف آمن لاستخدامه كإدخال ZIP (يُبقي الامتداد). */
    fun sanitizeFileName(name: String): String {
        val dot = name.lastIndexOf('.')
        val base = if (dot > 0) name.substring(0, dot) else name
        val ext = if (dot > 0) name.substring(dot) else ""
        val safeBase = base.replace(Regex("[\\\\/:*?\"<>|]"), "_").ifBlank { "file" }
        return safeBase + ext
    }

    /** رابط قابل للمشاركة عبر FileProvider لملف مُصدَّر داخل cache/output. */
    fun shareUriFor(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    fun humanFileSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val units = arrayOf("KB", "MB", "GB")
        var value = bytes / 1024.0
        var unitIndex = 0
        while (value >= 1024 && unitIndex < units.lastIndex) {
            value /= 1024.0
            unitIndex++
        }
        return "%.1f %s".format(value, units[unitIndex])
    }
}
