package dev.ja.bhb

import dev.ja.bhb.model.Amount
import dev.ja.bhb.model.Transaction
import dev.ja.bhb.model.TransactionId
import dev.ja.bhb.model.Vat
import dev.ja.bhb.requests.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

class KtorBhbClient(
        private val apiClient: String,
        private val apiSecret: String,
        private val apiKey: String
) : BhbClient {
    private val basePath = "/api/v1"

    private inline fun newRequest(block: JsonObjectBuilder.() -> Unit): JsonObject {
        return buildJsonObject {
            put("api_key", apiKey)
            this.block()
        }
    }

    private inline fun <reified T> JsonObjectBuilder.merge(data: T) {
        Json.encodeToJsonElement(data).jsonObject.toMap().forEach(::put)
    }

    private val httpClient = HttpClient(Java) {
        install(Logging)
        install(Resources)
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            })
        }

        install(DefaultRequest) {
            url {
                protocol = URLProtocol.HTTPS
                host = "app.buchhaltungsbutler.de"
                path("api/v1")
            }
            basicAuth(apiClient, apiSecret)
            header("Content-Type", "application/json")
        }

        expectSuccess = true
        engine {
            pipelining = true
        }
    }

    override fun close() {
        httpClient.close()
    }

    override suspend fun getAccounts(): AccountsResponse {
        return httpClient.post("$basePath/accounts/get") {
            setBody(newRequest { })
        }.body<ResponseWrapper<AccountsResponse>>().data
    }

    override suspend fun getTransactions(request: GetTransactions): List<Transaction> {
        // BHB's API doesn't support proper paging,
        // thus we're fetching packs of 500 until we receive an empty list of transaction and assume it means
        // that the last has already been fetched

        // fixme /transactions/get returns only partial transaction data, e.g. without property "currency"
        //   we have to workaround this by collecting ids with /transactions/get and then fetching transaction
        //   data one-by-one

        @Serializable
        data class OnlyTransactionId(@SerialName("id_by_customer") val id: Int)

        val transactionIds = mutableListOf<Int>()
        var offset = request.offset ?: 0
        while (true) {
            val resp = httpClient.post("$basePath/transactions/get") {
                setBody(newRequest { merge(request.copy(offset = offset)) })
            }.body<ResponseWrapper<List<OnlyTransactionId>>>()

            val newTransactionIds = resp.data.map { it.id }
            transactionIds += newTransactionIds
            offset += newTransactionIds.size

            if (resp.rows == 0 || newTransactionIds.isEmpty() || request.limit != null && transactionIds.size >= request.limit) {
                break
            }
        }

        // now, fetch one-by-one
        return transactionIds.map { getTransaction(it.toString()) }
    }

    override suspend fun getTransaction(id: TransactionId): Transaction {
        return httpClient.post("$basePath/transactions/get/$id") {
            setBody(newRequest { })
        }.body<ResponseWrapper<Transaction>>().data
    }

    override suspend fun addTransaction(transaction: AddTransaction): TransactionId {
        @Serializable
        data class Response(@SerialName("id_by_customer") val id: String)

        return httpClient.post("$basePath/transactions/add") {
            setBody(newRequest {
                merge(transaction)
            })
        }.body<Response>().id
    }

    override suspend fun addBatchTransactions(transactions: List<AddTransaction>): List<TransactionId> {
        @Serializable
        data class Request(@SerialName("transactions") val transactions: List<AddTransaction>)

        @Serializable
        data class ResponseTransaction(
                @SerialName("success") val success: Boolean,
                @SerialName("message") val message: String,
                @SerialName("id_by_customer") val id: TransactionId
        )

        @Serializable
        data class Response(
                @SerialName("success") val success: Boolean,
                @SerialName("transactions")
                val transactions: List<ResponseTransaction>
        )

        val response = httpClient.post("$basePath/transactions/addBatch") {
            setBody(newRequest {
                merge(Request(transactions))
            })
        }.body<Response>()

        // fixme unclear if we need to validate if all items were created
        return response.transactions.map { it.id }
    }

    override suspend fun addPosting(posting: AddTransactionPosting) {
        @Serializable
        data class Request(
                @SerialName("transaction_id_by_customer")
                val transactionIdByCustomer: String,
                @SerialName("postingaccounts")
                val postingaccounts: List<Int>,
                @SerialName("postingtexts")
                val postingtexts: List<String>,
                @SerialName("vats")
                val vats: List<Vat>,
                @SerialName("amounts")
                val amounts: List<Amount>,
        )

        val accounts = posting.items.map { it.postingAccount }
        val texts = posting.items.map { it.text }
        val vats = posting.items.map { it.vat }
        val amounts = posting.items.map { it.amount }
        val request = Request(posting.id, accounts, texts, vats, amounts)

        return httpClient.post("$basePath/postings/add/transaction") {
            setBody(newRequest {
                merge(request)
            })
        }.body()
    }

    override suspend fun addComment(comment: AddComment) {
        return httpClient.post("$basePath/comments/add") {
            setBody(newRequest {
                merge(comment)
            })
        }.body()
    }

    /**
     * Helper class to unwrap JSON responses, where the needed data is found at property "data".
     */
    @Serializable
    data class ResponseWrapper<T>(
            @SerialName("success")
            val success: Boolean,
            @SerialName("rows")
            val rows: Int? = null,
            @SerialName("message")
            val message: String? = null,
            @SerialName("data")
            val data: T
    )
}