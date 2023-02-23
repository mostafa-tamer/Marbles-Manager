package com.example.barcodeReader.network.properties.get.marble

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Table(
    val amount: String,
    val brandCode: String,
    val brandLanguages: BrandLanguages,
    val brandName: String,
    val number: String
) : Parcelable