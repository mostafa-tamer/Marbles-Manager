package com.example.barcodeReader.network.properties.get.marble

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MetaData(
    val blockNumber: String,
    val frz: String,
    val itemCode: String,
    val itemName: String,
    val itemNameLanguages: ItemNameLanguages,
    val price: String,
    val table: List<Table>,
    val unit: String,
    val unitCode: String,
    val unitLanguages: UnitLanguages,
    val xdimension: String,
    val ydimension: String,
    val zdimension: String
) : Parcelable