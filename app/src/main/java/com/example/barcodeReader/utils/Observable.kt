package com.example.barcodeReader.utils

class Observable<T>(var value: T? = null) {

    private var function: (value: T?) -> Unit = {}

    @JvmName("setValue1")
    fun setValue(value: T?) {
        this.value = value
        function(value)
    }

    @JvmName("getValue1")
    fun getValue() = value

    fun work(function: (value: T?) -> Unit) {
        this.function = function
    }
}