package dev.ja.wise.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FlatBalanceStatement(
        @SerialName("accountHolder")
        val accountHolder: AccountHolder,

        @SerialName("issuer")
        val accountIssuer: AccountIssuer,

        @SerialName("endOfStatementBalance")
        val endOfStatementBalance: Amount,

        @SerialName("transactions")
        val transactions: List<Transaction>,
)
