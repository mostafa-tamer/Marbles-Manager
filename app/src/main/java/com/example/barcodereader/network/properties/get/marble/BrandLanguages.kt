package com.example.barcodereader.network.properties.get.marble

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BrandLanguages(
    val De: String,
    val En: String,
    val Es: String,
    val Fr: String,
    val It: String,
    val Ru: String,
    val Tr: String
):Parcelable