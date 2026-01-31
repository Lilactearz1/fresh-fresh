package com.movix.transak_infield.docAnalysis.util

import android.content.Context
import android.net.Uri
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

object FileUtils {

    fun prepareFilePart(context: Context, uri: Uri): MultipartBody.Part {
        val bytes = context.contentResolver.openInputStream(uri)!!.readBytes()

        val requestBody = bytes.toRequestBody()
        return MultipartBody.Part.createFormData(
            "file",
            "invoice.pdf",
            requestBody
        )
    }
}