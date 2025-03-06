package dev.ja.sync.model

import com.charleskorn.kaml.Yaml
import dev.ja.bhb.model.AccountId
import dev.ja.model.Currency
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
        val readOnly: Boolean? = false,

        // labels
        @SerialName("label_unknown_sender")
        val unknownSender: String = "Unbekannt",
        @SerialName("label_wise_sender")
        val wiseSenderLabel: String = "Wise",
        @SerialName("label_wise_fee")
        val wiseFeeLabel: String = "Wise Gebühren",
        @SerialName("label_wise_fee_conversion")
        val wiseFeeLabelConversion: String = "Wise Gebühren Währungstausch",
        @SerialName("label_wise_cashback")
        val wiseCashbackLabel: String = "Wise Cashback",
        @SerialName("label_wise_conversion")
        val wiseConversionLabel: String = "Wise \$from - \$to",
) {
    fun wiseConversionLabel(sourceCurrency: Currency, targetCurrency: Currency): String {
        return this.wiseConversionLabel.replace("\$from", sourceCurrency.id).replace("\$to", targetCurrency.id)
    }

    companion object {
        fun loadFromYaml(filePath: Path): SyncConfig {
            return Yaml.default.decodeFromString(serializer(), Files.readString(filePath))
        }
    }
}