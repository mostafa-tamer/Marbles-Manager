package com.example.barcodereader.network.properties.get.groups

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Group(
    val branchList: List<Branch>,
    val pillTypeList: List<PillType>
) : Parcelable