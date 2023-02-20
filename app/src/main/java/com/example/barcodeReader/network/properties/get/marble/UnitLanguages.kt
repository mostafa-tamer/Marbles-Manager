package com.example.barcodeReader.network.properties.get.marble

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UnitLanguages(
    val De: String,
    val En: String,
    val Es: String,
    val Fr: String,
    val It: String,
    val Ru: String,
    val Tr: String
): Parcelable