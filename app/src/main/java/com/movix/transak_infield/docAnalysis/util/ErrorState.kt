package com.movix.transak_infield.docAnalysis.util

sealed class ErrorState {
    object Network : ErrorState()
    object Extraction : ErrorState()
    object Update : ErrorState()
}