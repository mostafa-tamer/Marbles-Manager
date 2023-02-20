package com.example.barcodeReader.network.properties.get.groups

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MetaData(
    val branchList: List<Branch>,
    val pillTypeList: List<PillType>
) : Parcelable