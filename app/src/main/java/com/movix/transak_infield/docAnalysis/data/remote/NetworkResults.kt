package com.movix.transak_infield.docAnalysis.data.remote

sealed class NetworkResults<out T> {

    data class Success<T>(val data: T) : NetworkResults<T>()

    data class Error(val message: String) : NetworkResults<Nothing>()

    data object Loading : NetworkResults<Nothing>()
}