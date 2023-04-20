package dev.ja.sync.model

import dev.ja.model.Currency
import dev.ja.wise.model.Balance
import dev.ja.wise.model.FlatBalanceStatement
import dev.ja.wise.model.Profile

data class CollectedWiseData(
        val profile: Profile,
        val currencies: Set<Currency>,
        val balances: Map<Currency, Balance>,
        val statements: Map<Currency, FlatBalanceStatement>
)
