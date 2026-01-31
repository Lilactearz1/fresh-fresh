package com.movix.transak_infield.docAnalysis.data.remote

import com.movix.transak_infield.docAnalysis.domain.model.ExtractedItem
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
////end points
data class ExtractionResponse(
    val invoice_id: Int,
    val items: List<ExtractedItem>
)

interface ApiService {

    @Multipart
    @POST("extractee-invoice")
    suspend fun extractInvoice(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Response<ExtractionResponse>

    @PUT("items/{id}")
    suspend fun updateItem(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body item: ExtractedItem
    ): Response<Unit>
}