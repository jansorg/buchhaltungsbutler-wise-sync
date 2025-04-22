package dev.ja.wise.camt053

import dev.ja.model.Currency
import dev.ja.wise.model.CreditType
import jakarta.xml.bind.annotation.*
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter
import kotlinx.datetime.Instant
import org.w3c.dom.Element
import org.w3c.dom.NodeList

// Root element
@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
@XmlAccessorType(XmlAccessType.FIELD)
data class Document(
    @field:XmlElement(name = "BkToCstmrStmt", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val bankToCustomerStatement: BankToCustomerStatement? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class BankToCustomerStatement(
    @field:XmlElement(name = "GrpHdr", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val groupHeader: GroupHeader? = null,

    @field:XmlElement(name = "Stmt", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val statement: Statement? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class GroupHeader(
    @field:XmlElement(name = "MsgId", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val msgId: String? = null,

    @field:XmlElement(name = "CreDtTm", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    @field:XmlJavaTypeAdapter(InstantAdapter::class)
    val createdAt: Instant? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class Statement(
    @field:XmlElement(name = "Id", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val id: String? = null,

    @field:XmlElement(name = "CreDtTm", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    @field:XmlJavaTypeAdapter(InstantAdapter::class)
    val createdAt: Instant? = null,

    @field:XmlElement(name = "FrToDt", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val fromToDateRange: FromToDateRange? = null,

    @field:XmlElement(name = "Acct", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val account: Account? = null,

    @field:XmlElement(name = "Bal", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val balance: List<Balance>? = null,

    @field:XmlElement(name = "TxsSummry", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val transactionsSummary: TransactionsSummary? = null,

    @field:XmlElement(name = "Ntry", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val statementEntries: List<StatementEntry>? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class FromToDateRange(
    @field:XmlElement(name = "FrDtTm", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    @field:XmlJavaTypeAdapter(InstantAdapter::class)
    val fromDate: Instant? = null,

    @field:XmlElement(name = "ToDtTm", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    @field:XmlJavaTypeAdapter(InstantAdapter::class)
    val toDate: Instant? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class Account(
    @field:XmlElement(name = "Id", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val id: AccountId? = null,

    @field:XmlElement(name = "Ccy", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    @field:XmlJavaTypeAdapter(CurrencyAdapter::class)
    val currency: Currency? = null,

    @field:XmlElement(name = "Ownr", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val owner: AccountOwner? = null,

    @field:XmlElement(name = "Svcr", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val accountServicer: AccountServicer? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class AccountId(
    @field:XmlElement(name = "Othr", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val otherIdentification: OtherIdentification? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class OtherIdentification(
    @field:XmlElement(name = "Id", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val id: String? = null,

    @field:XmlElement(name = "SchmeNm", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val schmeName: SchmeName? = null,

    @field:XmlElement(name = "Issr", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val issuer: String? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class SchmeName(
    @field:XmlElement(name = "Cd", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val cd: String? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class AccountOwner(
    @field:XmlElement(name = "Nm", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val nm: String? = null,

    @field:XmlElement(name = "PstlAdr", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val pstlAdr: PostalAddress? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class PostalAddress(
    @field:XmlElement(name = "AdrTp", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val type: AddressType? = null,

    @field:XmlElement(name = "PstCd", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val postalCode: String? = null,

    @field:XmlElement(name = "TwnNm", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val town: String? = null,

    @field:XmlElement(name = "AdrLine", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val addressLine: String? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class AddressType(
    @field:XmlElement(name = "Cd", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val code: String? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class AccountServicer(
    @field:XmlElement(name = "FinInstnId", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val institutionId: FinancialInstitutionId? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class FinancialInstitutionId(
    @field:XmlElement(name = "Nm", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val name: String? = null,

    @field:XmlElement(name = "PstlAdr", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val postalAddress: PostalAddress? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class Balance(
    @field:XmlElement(name = "Tp", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val balanceType: BalanceType? = null,

    @field:XmlElement(name = "Amt", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val amount: Amount? = null,

    @field:XmlElement(name = "CdtDbtInd", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val creditDebitIndicator: CreditType? = null,

    @field:XmlElement(name = "Dt", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val date: DateTime? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class BalanceType(
    @field:XmlElement(name = "CdOrPrtry", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val codeOrProperty: CodeOrProperty? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class CodeOrProperty(
    @field:XmlElement(name = "Cd", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val code: String? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class Amount(
    @field:XmlAttribute(name = "Ccy")
    @field:XmlJavaTypeAdapter(CurrencyAdapter::class)
    val currency: Currency? = null,

    @field:XmlValue
    val value: Double? = null
) {
    fun toWiseAmount(): dev.ja.wise.model.Amount {
        val isZero = value == null || value == 0.0
        return dev.ja.wise.model.Amount(currency!!, value ?: 0.0, isZero)
    }
}

@XmlAccessorType(XmlAccessType.FIELD)
data class DateTime(
    @field:XmlElement(name = "DtTm", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    @field:XmlJavaTypeAdapter(InstantAdapter::class)
    val value: Instant? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class TransactionsSummary(
    @field:XmlElement(name = "TtlNtries", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val totalEntries: TotalEntries? = null,

    @field:XmlElement(name = "TtlCdtNtries", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val totalCreditEntries: TotalCreditEntries? = null,

    @field:XmlElement(name = "TtlDbtNtries", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val totalDebitEntries: TotalDebitEntries? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class TotalEntries(
    @field:XmlElement(name = "NbOfNtries", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val numberOfEntries: Int? = null,

    @field:XmlElement(name = "Sum", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val sum: Double? = null,

    @field:XmlElement(name = "TtlNetNtry", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val ttlNetNtry: TotalNetEntry? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class TotalNetEntry(
    @field:XmlElement(name = "Amt", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val amount: Double? = null,

    @field:XmlElement(name = "CdtDbtInd", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val creditDebitIndicator: CreditType? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class TotalCreditEntries(
    @field:XmlElement(name = "NbOfNtries", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val nbOfNtries: String? = null,

    @field:XmlElement(name = "Sum", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val sum: Double? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class TotalDebitEntries(
    @field:XmlElement(name = "NbOfNtries", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val numberOfEntries: Int? = null,

    @field:XmlElement(name = "Sum", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val sum: Double? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class StatementEntry(
    @field:XmlElement(name = "NtryRef", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val entityReference: String? = null,

    @field:XmlElement(name = "Amt", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val amount: Amount? = null,

    @field:XmlElement(name = "CdtDbtInd", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val creditDebitIndicator: CreditType? = null,

    @field:XmlElement(name = "Sts", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val status: Status? = null,

    @field:XmlElement(name = "BookgDt", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val bookingDate: DateTime? = null,

    @field:XmlElement(name = "BkTxCd", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val bankTransactionCode: BankTransactionCode? = null,

    @field:XmlElement(name = "AddtlNtryInf", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val additionalInformation: String? = null,

    @field:XmlElement(name = "AmtDtls", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val amountDetails: StatementEntryAmountDetails? = null,

    @field:XmlAnyAttribute
    val otherAttributes: Map<String, Any?>? = null,

    @field:XmlAnyElement()
    val otherElements: List<Element>? = null,
) {
    val creditorName: String?
        get() {
            val name = otherElements?.jaxbQuery("NtryDtls", "TxDtls", "RltdPties", "Cdtr", "Pty", "Nm") as? String
            return name
        }
}

@XmlAccessorType(XmlAccessType.FIELD)
data class StatementEntryAmountDetails(
    @field:XmlElement(name = "TxAmt", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val transactionAmount: StatementEntryTransactionAmount? = null,
)

@XmlAccessorType(XmlAccessType.FIELD)
data class StatementEntryTransactionAmount(
    @field:XmlElement(name = "Amt", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val amount: Amount? = null,

    @field:XmlElement(name = "CcyXchg", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val currencyExchange: StatementEntryCurrencyExchange? = null,
)

@XmlAccessorType(XmlAccessType.FIELD)
data class StatementEntryCurrencyExchange(
    @field:XmlElement(name = "SrcCcy", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    @field:XmlJavaTypeAdapter(CurrencyAdapter::class)
    val sourceCurrency: Currency? = null,

    @field:XmlElement(name = "TrgtCcy", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    @field:XmlJavaTypeAdapter(CurrencyAdapter::class)
    val targetCurrency: Currency? = null,

    @field:XmlElement(name = "UnitCcy", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    @field:XmlJavaTypeAdapter(CurrencyAdapter::class)
    val unitCurrency: Currency? = null,

    @field:XmlElement(name = "XchgRate", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val exchangeRate: Double? = null,
)

@XmlAccessorType(XmlAccessType.FIELD)
data class Status(
    @field:XmlElement(name = "Cd", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val code: String? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class BankTransactionCode(
    @field:XmlElement(name = "Prtry", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val proprietary: Proprietary? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class Proprietary(
    @field:XmlElement(name = "Cd", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.10")
    val code: String? = null
)