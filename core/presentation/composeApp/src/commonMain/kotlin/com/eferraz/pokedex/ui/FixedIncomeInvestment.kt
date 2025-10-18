package com.eferraz.pokedex.ui

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
internal data class FixedIncomeInvestment(
    val id: String,
    val owner: String,
    val brokerageFirm: String,
    val description: String,
    val investedAmount: String,
    val currentAmount: String,
    val purchaseDate: String,
    val maturityDate: String,
    val profitability: String,
    val profitabilityIndex: String,
    val type: String,
    val liquidity: String,
    val issuerBank: String,
    val note: String,
)