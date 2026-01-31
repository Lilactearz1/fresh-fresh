package com.movix.transak_infield.docAnalysis.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
//// retrofit instances
object ApiClient {

    private const val BASE_URL = "https://your-api-url.com/"

    val api: ApiService by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}