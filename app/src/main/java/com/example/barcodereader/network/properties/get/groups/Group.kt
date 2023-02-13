package com.example.barcodereader.network.properties.get.groups

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
data class Group(
    @PrimaryKey
    val groupCode: String,
    val groupName: String,
    val groupNameDe: String,
    val groupNameEn: String,
    val groupNameEs: String,
    val groupNameFr: String,
    val groupNameIt: String,
    val groupNameRu: String,
    val groupNameTr: String
) : Parcelable