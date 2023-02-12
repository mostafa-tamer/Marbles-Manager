package com.example.barcodereader.network.properties.get.brances

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Group(
    val groupCode: String,
    val groupName: String,
    val groupNameDe: String,
    val groupNameEn: String,
    val groupNameEs: String,
    val groupNameFr: String,
    val groupNameIt: String,
    val groupNameRu: String,
    val groupNameTr: String
):Parcelable