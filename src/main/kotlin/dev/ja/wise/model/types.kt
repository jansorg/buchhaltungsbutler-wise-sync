package dev.ja.wise.model

typealias ProfileId = Int
typealias BalanceId = Int
typealias TransferIdString = String
typealias TransferId = Int
typealias AmountValue = Double

fun TransferIdString.asTransferId(): TransferId {
    return removePrefix("TRANSFER-").toInt()
}
