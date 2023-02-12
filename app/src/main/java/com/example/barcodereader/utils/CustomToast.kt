package com.example.barcodereader.utils

import android.content.Context
import android.widget.Toast

object CustomToast {

    private lateinit var toast: Toast
    fun show(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT)
            .show()
    }
}