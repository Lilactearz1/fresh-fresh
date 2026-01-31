package com.movix.transak_infield.docAnalysis.data.repository

import com.movix.transak_infield.docAnalysis.data.remote.*
import com.movix.transak_infield.docAnalysis.domain.model.ExtractedItem
import okhttp3.MultipartBody

class InvoiceRepository {

    suspend fun extractInvoice(
        token: String,
        filePart: MultipartBody.Part
    ): NetworkResults<List<ExtractedItem>> {

        return try {

            val response = ApiClient.api.extractInvoice(token, filePart)

            if (response.isSuccessful && response.body() != null) {
                NetworkResults.Success(response.body()!!.items)
            } else {
                NetworkResults.Error("Extraction failed: ${response.message()}")
            }

        } catch (e: Exception) {
            NetworkResults.Error("Extraction failed: ${e.message}")
        }
    }

    suspend fun updateItem(
        token: String,
        item: ExtractedItem
    ): NetworkResults<Unit> {

        return try {

            val response = ApiClient.api.updateItem(token, item.id, item)

            if (response.isSuccessful) {
                NetworkResults.Success(Unit)
            } else {
                NetworkResults.Error("Update failed: ${response.message()}")
            }

        } catch (e: Exception) {
            NetworkResults.Error("Update failed: ${e.message}")
        }
    }
}