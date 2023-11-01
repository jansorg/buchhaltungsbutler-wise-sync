package dev.ja.bhb.requests

import dev.ja.bhb.model.AccountId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias AccountsResponse = List<Account>

@Serializable
data class Account(
    @SerialName("name")
    val name: String,

    @SerialName("postingaccount_number")
    val accountNumber: AccountId?
)
