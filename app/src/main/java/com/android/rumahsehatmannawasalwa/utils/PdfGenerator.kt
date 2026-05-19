package com.android.rumahsehatmannawasalwa.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import okhttp3.ResponseBody
import java.io.InputStream
import java.io.OutputStream

object PdfGenerator {

    /**
     * Menyimpan binary data PDF yang didownload dari server ke folder Downloads/MannaWaSalwa/
     */
    fun savePdfToDownloads(
        context: Context,
        responseBody: ResponseBody,
        reportName: String
    ): Uri? {
        val fileName = "${reportName}_${System.currentTimeMillis()}.pdf"
        var uri: Uri? = null

        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/MannaWaSalwa")
                }
            }

            uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                val inputStream: InputStream = responseBody.byteStream()
                val outputStream: OutputStream? = context.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.flush()
                    outputStream.close()
                    inputStream.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        return uri
    }
}
