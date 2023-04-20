package dev.ja.wise.model

typealias TransactionReferenceNumber = String

fun TransactionReferenceNumber.isTransfer(): Boolean {
    return this.startsWith("TRANSFER-")
}

fun TransactionReferenceNumber.isBalanceCashback(): Boolean {
    return this.startsWith("BALANCE_CASHBACK-")
}

fun TransactionReferenceNumber.isBalance(): Boolean {
    return this.startsWith("BALANCE-")
}

fun TransactionReferenceNumber.asTransferIdString(): TransferIdString {
    if (!isTransfer()) {
        throw IllegalStateException("Not a transfer ID: $this")
    }
    return this
}