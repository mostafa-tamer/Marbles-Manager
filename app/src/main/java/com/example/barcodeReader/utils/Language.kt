package com.example.barcodeReader


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
    open val amount: String,
    open val number: String,
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
    override val itemName: String = "ItemName",
    override val amount: String = "Amount",
    override val number: String = "Number",
) : Language(
    frz, blockNumber, height, length, width, price, unit, itemCode, itemName, amount, number
)

class Arabic(
    override val frz: String = "الـفــــرز",
    override val blockNumber: String = "رقم البلوك",
    override val height: String = "الارتفاع/ سماكة",
    override val length: String = "الـطـول",
    override val width: String = "الـعرض",
    override val price: String = "السعر",
    override val unit: String = "الـوحدة",
    override val itemCode: String = "كودالصنف",
    override val itemName: String = "اسم الصنف",
    override val amount: String = "الـكـمية",
    override val number: String = "الـعـــدد",
) : Language(
    frz, blockNumber, height, length, width, price, unit, itemCode, itemName, amount, number
)


object EnPack {
    const val server_is_unreachable: String = "Server is unreachable"
    const val error: String = "Error"
    const val ok: String = "Ok"
    const val error_occurred: String = "Error occurred"

}