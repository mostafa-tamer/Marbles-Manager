package com.example.barcodeReader.utils

class Observable<T>(private var value: T? = null) {

    private var function: (value: T?) -> Unit = {}

    fun setValue(value: T?) {
        this.value = value
        function(value)
    }

    fun getValue() = value

    fun work(function: (value: T?) -> Unit) {
        this.function = function
    }
}