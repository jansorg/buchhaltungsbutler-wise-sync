package dev.ja.wise

import dev.ja.model.Currency
import dev.ja.wise.json.asWiseFormat
import dev.ja.wise.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import java.io.StringReader
import java.nio.charset.StandardCharsets
import java.security.Security
import java.security.Signature
import java.util.*


class KtorWiseClient(apiKey: String, private val privateKey: String) : WiseClient {
    companion object {
        init {
            Security.addProvider(BouncyCastleProvider())
        }
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
                host = "api.wise.com"
                path("v1")
            }
            bearerAuth(apiKey)
            header("Content-Type", "application/json")
        }

        expectSuccess = true
        engine {
            pipelining = true
        }
    }

    override suspend fun getProfiles(): List<Profile> {
        return httpClient.get("/v2/profiles").body()
    }

    override suspend fun getBalances(profileId: ProfileId, balanceType: BalanceType): List<Balance> {
        return httpClient.get("/v4/profiles/$profileId/balances") {
            parameter("types", balanceType.wiseId)
        }.body()
    }

    override suspend fun getTransfer(id: TransferIdString): Transfer {
        return httpClient.get("/v1/transfers/${id.asTransferId()}").body()
    }

    override suspend fun getBalanceStatement(
            profileId: ProfileId,
            balanceId: BalanceId,
            currency: Currency,
            start: Instant,
            end: Instant
    ): FlatBalanceStatement {
        suspend fun requestStatement(requestBuilder: HttpRequestBuilder.() -> Unit = {}): HttpResponse {
            return httpClient.get("v1/profiles/$profileId/balance-statements/$balanceId/statement.json") {
                expectSuccess = false
                parameter("currency", currency.id)
                parameter("intervalStart", start.asWiseFormat())
                parameter("intervalEnd", end.asWiseFormat())
                parameter("type", "COMPACT")
                requestBuilder()
            }
        }

        var response = requestStatement()

        // SCA workflow
        if (response.requiredWiseScaWorkflow()) {
            val scaCode = response.headers["x-2fa-approval"]
                    ?: throw IllegalStateException("Expected SCA header x-2fa-approval")

            val signature = signForWiseSca(scaCode)
            response = requestStatement {
                header("x-2fa-approval", scaCode)
                header("X-Signature", signature)
                expectSuccess = true
            }
        }

        return response.body()
    }

    private fun HttpResponse.requiredWiseScaWorkflow(): Boolean {
        return status == HttpStatusCode.Forbidden && headers["x-2fa-approval-result"] == "REJECTED"
    }

    private fun signForWiseSca(value: String): String {
        val parsedPrivateKey = when (val parsedKey = PEMParser(StringReader(privateKey)).readObject()) {
            is PrivateKeyInfo -> {
                JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getPrivateKey(parsedKey)
            }

            else -> throw IllegalStateException("Unable to find private key for $parsedKey")
        }

        val signer = Signature.getInstance("SHA256withRSA", BouncyCastleProvider.PROVIDER_NAME)
        signer.initSign(parsedPrivateKey)
        signer.update(value.toByteArray(StandardCharsets.UTF_8))
        val signature = signer.sign()

        return Base64.getEncoder().encodeToString(signature)
    }
}