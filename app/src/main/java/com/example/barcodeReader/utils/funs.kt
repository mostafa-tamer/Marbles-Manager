package com.example.barcodeReader.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.example.barcodeReader.EnPack.ok
import com.example.barcodeReader.network.properties.get.marble.MetaData
import com.example.barcodeReader.userData

fun makeupBarcode(barcode: String): String {
    if (barcode.isEmpty()) return ""
    return if (barcode[0] == '0') {
        var counter = 0
        for (element in barcode) {
            if (element == '0' && counter < barcode.length - 1) {
                counter++
            } else {
                break
            }
        }
        barcode.substring(counter, barcode.length - 1)
    } else {
        barcode
    }
}

fun alertDialogErrorMessageObserver(
    alertDialogErrorMessageLiveData: MutableLiveData<AlertDialogErrorMessage>,
    viewLifecycleOwner: LifecycleOwner,
    exceptionErrorMessageAlertDialog: CustomAlertDialog
) {
    alertDialogErrorMessageLiveData.observe(viewLifecycleOwner) {
        if (it.errorExist) {
            exceptionErrorMessageAlertDialog
                .setTitle(it.title)
                .setMessage(it.message)
                .setPositiveButton(ok)
                .showDialog()
            alertDialogErrorMessageLiveData.value = AlertDialogErrorMessage()
        }
    }
}

fun nameFilter(data: MetaData): String {
    return when (userData.loginLanguage) {
        "ar" -> {
            data.itemName
        }
        "en" -> {
            data.itemNameLanguages.En
        }
        "de" -> {
            data.itemNameLanguages.De
        }
        "es" -> {
            data.itemNameLanguages.Es
        }
        "fr" -> {
            data.itemNameLanguages.Fr
        }
        "it" -> {
            data.itemNameLanguages.It
        }
        "ru" -> {
            data.itemNameLanguages.Ru
        }
        else -> {
            data.itemNameLanguages.Tr
        }
    }
}

fun uniteFilter(data: MetaData): String {
    return when (userData.loginLanguage) {
        "ar" -> {
            data.unit
        }
        "en" -> {
            data.unitLanguages.En
        }
        "de" -> {
            data.unitLanguages.De
        }
        "es" -> {
            data.unitLanguages.Es
        }
        "fr" -> {
            data.unitLanguages.Fr
        }
        "it" -> {
            data.unitLanguages.It
        }
        "ru" -> {
            data.unitLanguages.Ru
        }
        else -> {
            data.unitLanguages.Tr
        }
    }
}