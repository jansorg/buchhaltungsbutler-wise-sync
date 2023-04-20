package dev.ja.sync.model

import dev.ja.bhb.model.AccountId
import dev.ja.model.Currency
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Yaml
import java.nio.file.Files
import java.nio.file.Path

@Serializable
data class SyncConfig(
        @SerialName("bhb_api_client")
        val bhbApiClient: String,
        @SerialName("bhb_api_secret")
        val bhbApiSecret: String,
        @SerialName("bhb_api_key")
        val bhbApiKey: String,
        @SerialName("bhb_accounts")
        val bhbAccountToWiseCurrency: Map<AccountId, Currency>,
        @SerialName("bhb_fee_id")
        val bhbFeePostingId: Int? = null,
        @SerialName("wise_api_key")
        val wiseApiKey: String,
        /**
         * Private key to support secure SCA operations with the Wise.com API.
         * Balances are secured and require signed data in the request.
         * The matching public key must have been uploaded to Wise.com to make this work.
         */
        @SerialName("wise_private_key")
        val wisePrivateKey: String,
        @SerialName("read_only")
        val readOnly: Boolean? = false
) {
    val unknownSender = "Unbekannt"

    val wiseSenderLabel: String = "Wise.com"
    val wiseFeeLabel: String = "Wise.com Gebühren"
    val wiseCashbackLabel: String = "Wise.com Cashback"
    fun wiseConversionLabel(from: Currency, to: Currency): String {
        return "Wise.com ${from.id} - ${to.id}"
    }

    companion object {
        fun loadFromYaml(filePath: Path): SyncConfig {
            return Yaml.decodeFromString(serializer(), Files.readString(filePath))
        }
    }
}