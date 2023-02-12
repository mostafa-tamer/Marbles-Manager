package com.example.barcodereader.network.properties.get

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Table(
    val amount: String,
    val brandLanguages: BrandLanguages,
    val brandName: String,
    val number: String
) : Parcelable