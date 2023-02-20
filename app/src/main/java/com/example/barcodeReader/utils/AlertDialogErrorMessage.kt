package com.example.barcodeReader.utils

data class AlertDialogErrorMessage(
    var errorExist: Boolean = false,
    var title: String = "",
    var message: String = ""
)