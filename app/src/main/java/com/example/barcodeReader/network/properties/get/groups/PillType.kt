package com.example.barcodeReader.network.properties.get.groups

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class PillType(
    val code: String,
    val nameAr: String,
    val nameDe: String,
    val nameEn: String,
    val nameEs: String,
    val nameFr: String,
    val nameIt: String,
    val nameRu: String,
    val nameTr: String
) : Parcelable