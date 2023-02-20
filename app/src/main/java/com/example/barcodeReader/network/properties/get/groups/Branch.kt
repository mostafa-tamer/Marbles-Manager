package com.example.barcodeReader.network.properties.get.groups

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Branch(
    val brCode: String,
    val groupCode: String,
    val groupMgr: String,
    val groupName: String,
    val groupNameDe: String,
    val groupNameEn: String,
    val groupNameEs: String,
    val groupNameFr: String,
    val groupNameIt: String,
    val groupNameRu: String,
    val groupNameTr: String
) : Parcelable