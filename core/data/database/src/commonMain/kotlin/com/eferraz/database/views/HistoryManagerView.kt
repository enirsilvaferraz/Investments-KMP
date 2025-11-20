package com.eferraz.database.views

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import com.eferraz.entities.Liquidity
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/**
 * View do Room que combina holding_history com todos os dados relacionados necessários
 * para a tela de consulta de posicionamento do ativo por período.
 * 
 * Esta view retorna cada entrada de histórico com:
 * - Dados do holding (asset_holdings)
 * - Dados do asset (assets)
 * - Dados da corretora (brokerages)
 * - Dados do emissor (issuers)
 * - Dados dos tipos específicos de ativos (fixed_income_assets, variable_income_assets, investment_fund_assets)
 * - Dados do histórico (holding_history)
 * 
 * O DAO pode então fazer queries que filtram por período e fazem JOIN entre
 * a view para o período atual e a view para o período anterior.
 */
@DatabaseView(
    viewName = "history_manager_view",
    value = """
        SELECT 
            -- Dados do holding
            h.id AS holding_id,
            h.assetId AS holding_assetId,
            h.ownerId AS holding_ownerId,
            h.brokerageId AS holding_brokerageId,
            h.quantity AS holding_quantity,
            h.averageCost AS holding_averageCost,
            h.investedValue AS holding_investedValue,
            h.currentValue AS holding_currentValue,
            
            -- Dados do asset
            a.id AS asset_id,
            a.name AS asset_name,
            a.issuerId AS asset_issuerId,
            a.category AS asset_category,
            a.liquidity AS asset_liquidity,
            a.observations AS asset_observations,
            
            -- Dados da corretora
            b.id AS brokerage_id,
            b.name AS brokerage_name,
            
            -- Dados do emissor
            i.id AS issuer_id,
            i.name AS issuer_name,
            
            -- Dados do histórico
            hist.id AS history_id,
            hist.holdingId AS history_holdingId,
            hist.referenceDate AS history_referenceDate,
            hist.endOfMonthValue AS history_endOfMonthValue,
            hist.endOfMonthQuantity AS history_endOfMonthQuantity,
            hist.endOfMonthAverageCost AS history_endOfMonthAverageCost,
            hist.totalInvested AS history_totalInvested,
            
            -- Dados de renda fixa (se aplicável)
            fia.type AS fixed_income_type,
            fia.subType AS fixed_income_subType,
            fia.expirationDate AS fixed_income_expirationDate,
            fia.contractedYield AS fixed_income_contractedYield,
            fia.cdiRelativeYield AS fixed_income_cdiRelativeYield,
            
            -- Dados de renda variável (se aplicável)
            via.type AS variable_income_type,
            via.ticker AS variable_income_ticker,
            
            -- Dados de fundos (se aplicável)
            ifa.type AS investment_fund_type,
            ifa.liquidityDays AS investment_fund_liquidityDays,
            ifa.expirationDate AS investment_fund_expirationDate
            
        FROM holding_history hist
        INNER JOIN asset_holdings h ON hist.holdingId = h.id
        INNER JOIN assets a ON h.assetId = a.id
        INNER JOIN brokerages b ON h.brokerageId = b.id
        INNER JOIN issuers i ON a.issuerId = i.id
        LEFT JOIN fixed_income_assets fia ON a.id = fia.assetId
        LEFT JOIN variable_income_assets via ON a.id = via.assetId
        LEFT JOIN investment_fund_assets ifa ON a.id = ifa.assetId
    """
)
internal data class HistoryManagerView(
    // Dados do holding
    @ColumnInfo(name = "holding_id")
    val holdingId: Long,
    @ColumnInfo(name = "holding_assetId")
    val holdingAssetId: Long,
    @ColumnInfo(name = "holding_ownerId")
    val holdingOwnerId: Long,
    @ColumnInfo(name = "holding_brokerageId")
    val holdingBrokerageId: Long,
    @ColumnInfo(name = "holding_quantity")
    val holdingQuantity: Double,
    @ColumnInfo(name = "holding_averageCost")
    val holdingAverageCost: Double,
    @ColumnInfo(name = "holding_investedValue")
    val holdingInvestedValue: Double,
    @ColumnInfo(name = "holding_currentValue")
    val holdingCurrentValue: Double,
    
    // Dados do asset
    @ColumnInfo(name = "asset_id")
    val assetId: Long,
    @ColumnInfo(name = "asset_name")
    val assetName: String,
    @ColumnInfo(name = "asset_issuerId")
    val assetIssuerId: Long,
    @ColumnInfo(name = "asset_category")
    val assetCategory: String,
    @ColumnInfo(name = "asset_liquidity")
    val assetLiquidity: Liquidity,
    @ColumnInfo(name = "asset_observations")
    val assetObservations: String?,
    
    // Dados da corretora
    @ColumnInfo(name = "brokerage_id")
    val brokerageId: Long,
    @ColumnInfo(name = "brokerage_name")
    val brokerageName: String,
    
    // Dados do emissor
    @ColumnInfo(name = "issuer_id")
    val issuerId: Long,
    @ColumnInfo(name = "issuer_name")
    val issuerName: String,
    
    // Dados do histórico
    @ColumnInfo(name = "history_id")
    val historyId: Long,
    @ColumnInfo(name = "history_holdingId")
    val historyHoldingId: Long,
    @ColumnInfo(name = "history_referenceDate")
    val historyReferenceDate: YearMonth,
    @ColumnInfo(name = "history_endOfMonthValue")
    val historyEndOfMonthValue: Double,
    @ColumnInfo(name = "history_endOfMonthQuantity")
    val historyEndOfMonthQuantity: Double,
    @ColumnInfo(name = "history_endOfMonthAverageCost")
    val historyEndOfMonthAverageCost: Double,
    @ColumnInfo(name = "history_totalInvested")
    val historyTotalInvested: Double,
    
    // Dados de renda fixa (opcionais)
    @ColumnInfo(name = "fixed_income_type")
    val fixedIncomeType: String?,
    @ColumnInfo(name = "fixed_income_subType")
    val fixedIncomeSubType: String?,
    @ColumnInfo(name = "fixed_income_expirationDate")
    val fixedIncomeExpirationDate: LocalDate?,
    @ColumnInfo(name = "fixed_income_contractedYield")
    val fixedIncomeContractedYield: Double?,
    @ColumnInfo(name = "fixed_income_cdiRelativeYield")
    val fixedIncomeCdiRelativeYield: Double?,
    
    // Dados de renda variável (opcionais)
    @ColumnInfo(name = "variable_income_type")
    val variableIncomeType: String?,
    @ColumnInfo(name = "variable_income_ticker")
    val variableIncomeTicker: String?,
    
    // Dados de fundos (opcionais)
    @ColumnInfo(name = "investment_fund_type")
    val investmentFundType: String?,
    @ColumnInfo(name = "investment_fund_liquidityDays")
    val investmentFundLiquidityDays: Int?,
    @ColumnInfo(name = "investment_fund_expirationDate")
    val investmentFundExpirationDate: LocalDate?
)

