package dev.ja.bhb.requests

import dev.ja.bhb.model.Amount
import dev.ja.bhb.model.Vat

data class AddTransactionPosting(
        val id: String,
        val items: List<PostingItem>,
)

data class PostingItem(
        val postingAccount: Int,
        val text: String,
        val vat: Vat,
        val amount: Amount
)
