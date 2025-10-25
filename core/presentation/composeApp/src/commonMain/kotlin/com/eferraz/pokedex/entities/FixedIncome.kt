package com.eferraz.pokedex.entities

import kotlinx.datetime.LocalDate

/**
 * Represents an investment data class.
 *
 * @property id The unique identifier for the investment.
 * @property owner The name of the owner who made the investment purchase. Ex: Enir or Camila.
 * @property brokerageFirm The brokerage firm where the investment was traded. Ex: Inter, NuBank, BMG, etc.
 * @property description A description of the investment, which helps to identify it. Ex: 100% CDB from Banco do Brasil.
 * @property investedAmount The amount invested in the purchase. Ex: R$ 10,000.00.
 * @property currentAmount The current net value of the investment, updated monthly. Ex: R$ 10,100.00.
 * @property purchaseDate The date the investment was purchased. Ex: 24/10/2025.
 * @property maturityDate The investment's maturity date. Ex: 24/10/2025.
 * @property profitability The investment's profitability rate. Ex: 100%.
 * @property profitabilityIndex The investment's profitability index. Ex: Post-fixed, Pre-fixed, or Inflation-linked.
 * @property type The type of investment. Ex: CDB, LCA, Precatórios, etc.
 * @property liquidity The investment's liquidity. Ex: Daily or At Maturity.
 * @property issuerBank The bank that issued the investment. Ex: Banco do Brasil, Inter, NuBank, etc.
 * @property note Observations about the investment.
 */
internal data class FixedIncome(
    val id: Long = 0,
    val owner: Owner,
    val brokerageFirm: BrokerageFirm,
    val description: String,
    val investedAmount: Double,
    val currentAmount: Double,
    val purchaseDate: LocalDate,
    val maturityDate: LocalDate,
    val profitability: Float,
    val profitabilityIndex: ProfitabilityIndex,
    val type: FixedIncomeType,
    val liquidity: Liquidity,
    val issuerBank: IssuerBank,
    val note: String? = null,
)

internal data class Owner(val id: Long = 0, val name: String)
internal data class BrokerageFirm(val id: Long = 0, val name: String)
internal data class FixedIncomeType(val id: Long = 0, val name: String)
internal enum class Liquidity { DAILY, AT_MATURITY }
internal data class IssuerBank(val id: Long = 0, val name: String)
internal enum class ProfitabilityIndex { POST_FIXED, INFLATION_LINKED, PRE_FIXED }

