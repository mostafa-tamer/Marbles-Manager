package com.example.barcodereader


abstract class Language(
    open val frz: String,
    open val blockNumber: String,
    open val height: String,
    open val length: String,
    open val width: String,
    open val price: String,
    open val unit: String,
    open val itemCode: String,
    open val itemName: String,
)

class English(
    override val frz: String = "Frz",
    override val blockNumber: String = "BlockNumber",
    override val height: String = "Height",
    override val length: String = "Length",
    override val width: String = "Width",
    override val price: String = "Price",
    override val unit: String = "Unit",
    override val itemCode: String = "ItemCode",
    override val itemName: String = "ItemName"
) : Language(
    frz,
    blockNumber,
    height,
    length,
    width,
    price,
    unit,
    itemCode,
    itemName,
)

class Arabic(
    override val frz: String = "فرز",
    override val blockNumber: String = "رقم البلوك",
    override val height: String = "الارتفاع/ سماكة",
    override val length: String = "الطول",
    override val width: String = "العرض",
    override val price: String = "السعر",
    override val unit: String = "الوحدة",
    override val itemCode: String = "كودالصنف",
    override val itemName: String = "اسم الصنف"
) : Language(
    frz,
    blockNumber,
    height,
    length,
    width,
    price,
    unit,
    itemCode,
    itemName,
)