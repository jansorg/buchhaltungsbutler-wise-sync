package dev.ja.wise

import dev.ja.model.Currency
import dev.ja.wise.model.*
import kotlinx.datetime.Instant

interface WiseClient {
    /**
     * @return List of available Wise profiles
     */
    suspend fun getProfiles(): List<Profile>

    /**
     * @param profileId Wise profile, e.g. your business profile
     * @param balanceType The type of balance to load. By default, this is "STANDARD".
     * @return Balances, e.g. for `USD, EUR`, for the given Wise profile.
     */
    suspend fun getBalances(profileId: ProfileId, balanceType: BalanceType = BalanceType.Standard): List<Balance>

    /**
     * @return [Transfer] details for the given transfer id
     */
    suspend fun getTransfer(id: TransferIdString): Transfer

    /**
     * Balance statements contains transactional activities on a Wise Multi-Currency Account.
     *
     * The Wise API request is SCA-protected, which means it'll only work if your private/public key setup is correct.
     * This API automatically takes care of the multiple requests, which are needed to work with SCA-protected endpoints.
     *
     * @param currency Currency of the balance statement requested
     * @param start Statement interval start time
     * @param end Statement interval end time
     *
     * @return Balance statement in FLAT format. The flat format is suitable for accounting statements.
     */
    suspend fun getBalanceStatement(
            profileId: ProfileId,
            balanceId: BalanceId,
            currency: Currency,
            start: Instant,
            end: Instant
    ): FlatBalanceStatement

    /**
     * Shortcut for [getBalanceStatement] using id and currency of the given [Balance].
     */
    suspend fun getBalanceStatement(profileId: ProfileId, balance: Balance, start: Instant, end: Instant): FlatBalanceStatement {
        return getBalanceStatement(profileId, balance.id, balance.currency, start, end)
    }
}