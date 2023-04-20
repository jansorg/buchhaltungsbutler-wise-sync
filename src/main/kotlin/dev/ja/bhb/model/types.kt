package dev.ja.bhb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias AccountId = Int

typealias Amount = Double

typealias TransactionId = String

@Serializable
enum class Vat {
    // keine Ust.
    @SerialName("0_none")
    None,

    // 19% Ust.
    @SerialName("19_vat")
    Vat_19,

    // 7% Ust.
    @SerialName("7_vat")
    Vat_7,

    // 19% Vst.
    @SerialName("19_pre")
    Pre_19,

    // 7% Vst.
    @SerialName("7_pre")
    Pre_7,

    // §13b 19% USt./VSt.
    @SerialName("19_both_1")
    Both_19_1,

    // I.g.E. 19% USt./VSt.
    @SerialName("19_both_2")
    Both_19_2,

    // I.g.E. 7% USt./VSt.
    @SerialName("7_both")
    Both_7,

    // §13b 19/16% USt.
    @SerialName("19_both_1_no_pre")
    Both_19_1_noPre,

    // i.g.E. 19/16% USt
    @SerialName("19_both_2_no_pre")
    Both_19_2_noPre,

    // i.g.E. 7/5% USt
    @SerialName("7_both_no_pre")
    Both_7_noPre,

    // 19/16% Aufz. VSt.
    @SerialName("19_pre_app")
    Pre_19_App,

    // 7/5% Aufz. VSt.
    @SerialName("7_pre_app")
    Pre_7_App,

    // §13b 19/16% USt./Aufz. VSt
    @SerialName("19_both_app_1")
    Both_19_App_1,

    // i.g.E. 19/16% USt./Aufz. VSt
    @SerialName("19_both_app_2")
    Both_19_App_2,

    // i.g.E. 7/5% USt./Aufz. VSt.
    @SerialName("7_both_app")
    Both_7_App,
}