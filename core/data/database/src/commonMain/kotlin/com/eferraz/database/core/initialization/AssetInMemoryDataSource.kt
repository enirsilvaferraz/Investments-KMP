package com.eferraz.database.core.initialization

import com.eferraz.database.entities.AssetHoldingEntity
import com.eferraz.entities.Asset
import com.eferraz.entities.AssetHolding
import com.eferraz.entities.Brokerage
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.FixedIncomeAssetType
import com.eferraz.entities.FixedIncomeAssetType.INFLATION_LINKED
import com.eferraz.entities.FixedIncomeAssetType.POST_FIXED
import com.eferraz.entities.FixedIncomeAssetType.PRE_FIXED
import com.eferraz.entities.FixedIncomeSubType
import com.eferraz.entities.FixedIncomeSubType.CDB
import com.eferraz.entities.FixedIncomeSubType.LCA
import com.eferraz.entities.FixedIncomeSubType.LCI
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.InvestmentFundAssetType
import com.eferraz.entities.InvestmentFundAssetType.PENSION
import com.eferraz.entities.InvestmentFundAssetType.STOCK_FUND
import com.eferraz.entities.Issuer
import com.eferraz.entities.Liquidity
import com.eferraz.entities.Owner
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.entities.VariableIncomeAssetType
import com.eferraz.entities.VariableIncomeAssetType.ETF
import com.eferraz.entities.VariableIncomeAssetType.NATIONAL_STOCK
import com.eferraz.entities.VariableIncomeAssetType.REAL_ESTATE_FUND
import kotlinx.datetime.LocalDate

/**
 * Fonte de dados em memória que retorna todos os Assets cadastrados no sistema.
 * Os dados são extraídos do FixedIncomeRepository, retornando apenas os Assets,
 * sem informações de holdings (posições).
 *
 * Cada asset é único, mesmo que compartilhe o mesmo nome com outro asset,
 * pois podem ter características diferentes (ex: datas de vencimento diferentes).
 */

internal object AssetInMemoryDataSourceImpl {

    private val issuers = mutableMapOf<String, Issuer>()
    private var issuerIdCounter = 1L

    private fun getIssuer(name: String): Issuer {
        return issuers.getOrPut(name) { Issuer(id = issuerIdCounter++, name = name) }
    }

    // @formatter:off

    /**
     * Retorna todos os Assets cadastrados no sistema.
     * Cada asset é incluído na lista, mesmo que compartilhe o mesmo nome com outro asset,
     * pois podem ter características diferentes (ex: datas de vencimento diferentes).
     */
    fun getAllAssets() = listOf(

        FI("SUPER POUP BMG", "BMG", POST_FIXED, CDB, LocalDate(2026, 5, 11), 110.0, Liquidity.DAILY).toAssetHolding(bmg),
        FI("CDB ESCALONADO", "BMG", POST_FIXED, CDB, LocalDate(2027, 8, 29), 110.0, Liquidity.DAILY).toAssetHolding(bmg),
        FI("CDB ESCALONADO", "BMG", POST_FIXED, CDB, LocalDate(2030, 4, 1), 112.0, Liquidity.DAILY).toAssetHolding(bmg),
        FI("CDB ESCALONADO", "BMG", POST_FIXED, CDB, LocalDate(2030, 4, 2), 112.0, Liquidity.DAILY).toAssetHolding(bmg),
        FI("CDB ESCALONADO", "BMG", POST_FIXED, CDB, LocalDate(2030, 9, 15), 106.0, Liquidity.DAILY).toAssetHolding(bmg),
        FI("CDB PRE", "BMG", PRE_FIXED, CDB, LocalDate(2025, 10, 18), 12.96, Liquidity.AT_MATURITY).toAssetHolding(bmg),
        FI("Caixinha Turbo Ultravioleta", "Nubank", POST_FIXED, CDB, LocalDate(2030, 10, 18), 120.0, Liquidity.DAILY).toAssetHolding(nubank),
        FI("Saldo Separado", "Nubank", POST_FIXED, CDB, LocalDate(2050, 1, 1), 100.0, Liquidity.DAILY).toAssetHolding(nubank),
        FI("LCA BANCO ABC BRASIL", "ABC Brasil", POST_FIXED, LCA, LocalDate(2026, 7, 1), 90.5, Liquidity.AT_MATURITY).toAssetHolding(nubank),
        FI("CDB BANCO DIGIMAIS", "Banco Digimais", PRE_FIXED, CDB, LocalDate(2026, 7, 9), 11.29, Liquidity.AT_MATURITY).toAssetHolding(nubank),
        FI("CDB DM FINANCEIRA", "DM Financeira", POST_FIXED, CDB, LocalDate(2027, 5, 3), 113.0, Liquidity.AT_MATURITY).toAssetHolding(nubank),
        FI("CDB BANCO SEMEAR SA", "Banco Semear", POST_FIXED, CDB, LocalDate(2027, 6, 3), 122.0, Liquidity.AT_MATURITY).toAssetHolding(nubank),
        FI("CDB BANCO MASTER", "Banco Master", PRE_FIXED, CDB, LocalDate(2028, 12, 26), 15.5, Liquidity.AT_MATURITY).toAssetHolding(nubank),
        FI("CDB BANCO MASTER", "Banco Master", POST_FIXED, CDB, LocalDate(2029, 1, 2), 121.0, Liquidity.AT_MATURITY).toAssetHolding(nubank),
        FI("CDB BANCO MASTER", "Banco Master", POST_FIXED, CDB, LocalDate(2029, 4, 9), 125.0, Liquidity.AT_MATURITY).toAssetHolding(nubank),
        FI("LCA ORIGINAL", "Original", POST_FIXED, LCA, LocalDate(2026, 5, 4), 93.0, Liquidity.AT_MATURITY).toAssetHolding(inter),
        FI("LCI ORIGINAL", "Original", POST_FIXED, LCI, LocalDate(2026, 4, 27), 93.0, Liquidity.AT_MATURITY).toAssetHolding(inter),
        FI("LCI BRB", "BRB", POST_FIXED, LCI, LocalDate(2026, 10, 16), 91.5, Liquidity.AT_MATURITY).toAssetHolding(inter),
        FI("LCI IPCA 252 TB BANCO BARI", "Banco Bari", INFLATION_LINKED, LCI, LocalDate(2025, 12, 22), 5.1, Liquidity.AT_MATURITY).toAssetHolding(inter),
        FI("LCI IPCA FINAL 3 ANOS", "Inter", INFLATION_LINKED, LCI, LocalDate(2026, 5, 29), 5.28, Liquidity.AT_MATURITY).toAssetHolding(inter),
        FI("LCI ABC MENSAL", "ABC Brasil", POST_FIXED, LCI, LocalDate(2026, 6, 12), 96.0, Liquidity.AT_MATURITY).toAssetHolding(inter),
        FI("CDB ARBI", "ARBI", POST_FIXED, CDB, LocalDate(2029, 1, 12), 115.0, Liquidity.AT_MATURITY).toAssetHolding(inter),
        FI("CDB IPCA 252 TB BANCO MASTER", "Banco Master", INFLATION_LINKED, CDB, LocalDate(2026, 11, 17), 6.5, Liquidity.AT_MATURITY).toAssetHolding(inter),
        FI("CDB IPCA 252 TB BANCO MASTER", "Banco Master", INFLATION_LINKED, CDB, LocalDate(2027, 4, 12), 7.28, Liquidity.AT_MATURITY).toAssetHolding(inter),
        FI("CDB IPCA 252 TB BANCO MASTER", "Banco Master", PRE_FIXED, CDB, LocalDate(2027, 6, 28), 15.9, Liquidity.AT_MATURITY).toAssetHolding(inter),
        FI("CDB DI FLUT TB BANCO MASTER", "Banco Master", POST_FIXED, CDB, LocalDate(2027, 3, 10), 115.0, Liquidity.AT_MATURITY).toAssetHolding(inter),
        FI("LIG LIQUIDEZ", "Inter", POST_FIXED, CDB, LocalDate(2027, 9, 3), 87.0, Liquidity.AT_MATURITY).toAssetHolding(inter),
        FI("FGTS", "FGTS", PRE_FIXED, CDB, LocalDate(2026, 12, 31), 0.0, Liquidity.AT_MATURITY).toAssetHolding(caixa),
        FI("CDB LIQUIDEZ DIARIA", "Sofisa", PRE_FIXED, CDB, LocalDate(2026, 9, 8), 110.0, Liquidity.DAILY).toAssetHolding(sofisa),

        // Investment Fund Assets
        IF("ARCA GRÃO", "ICATU", PENSION).toAssetHolding(inter),
        IF("BTG Eletrobrás", "BTG", STOCK_FUND).toAssetHolding(btg),

        // Variable Income Assets
        VI("AZZA3", "AZZA3", NATIONAL_STOCK, "AZZA3", "Carteira: Oportunidades de uma vida").toAssetHolding(nubank),
        VI("B3SA3", "B3SA3", NATIONAL_STOCK, "B3SA3", "Carteira: Oportunidades de uma vida").toAssetHolding(nubank),
        VI("BPAC11", "BPAC11", NATIONAL_STOCK, "BPAC11", "Carteira: Oportunidades de uma vida").toAssetHolding(nubank),
        VI("BRAV3", "BRAV3", NATIONAL_STOCK, "BRAV3", "Carteira: Oportunidades de uma vida").toAssetHolding(nubank),
        VI("BRBI11", "BRBI11", NATIONAL_STOCK, "BRBI11", "Carteira: Oportunidades de uma vida").toAssetHolding(nubank),
        VI("CASH3", "CASH3", NATIONAL_STOCK, "CASH3", "Carteira: Oportunidades de uma vida").toAssetHolding(nubank),
        VI("COCE5", "COCE5", NATIONAL_STOCK, "COCE5", "Carteira: Oportunidades de uma vida").toAssetHolding(nubank),
        VI("CSAN3", "CSAN3", NATIONAL_STOCK, "CSAN3", "Carteira: Oportunidades de uma vida").toAssetHolding(nubank),
        VI("DIRR3", "DIRR3", NATIONAL_STOCK, "DIRR3", "Carteira: Oportunidades de uma vida").toAssetHolding(nubank),
        VI("ELET6", "ELET6", NATIONAL_STOCK, "ELET6", "Carteira: Oportunidades de uma vida").toAssetHolding(nubank),
        VI("ENEV3", "ENEV3", NATIONAL_STOCK, "ENEV3", "Carteira: Oportunidades de uma vida").toAssetHolding(nubank),
        VI("EQTL3", "EQTL3", NATIONAL_STOCK, "EQTL3", "Carteira: Oportunidades de uma vida").toAssetHolding(nubank),
        VI("GOAU4", "GOAU4", NATIONAL_STOCK, "GOAU4", "Carteira: Oportunidades de uma vida").toAssetHolding(nubank),
        VI("IGTI11", "IGTI11", NATIONAL_STOCK, "IGTI11", "Carteira: Oportunidades de uma vida").toAssetHolding(nubank),
        VI("INTB3", "INTB3", NATIONAL_STOCK, "INTB3", "Carteira: Oportunidades de uma vida").toAssetHolding(nubank),
        VI("RENT3", "RENT3", NATIONAL_STOCK, "RENT3", "Carteira: Oportunidades de uma vida").toAssetHolding(nubank),
        VI("SBFG3", "SBFG3", NATIONAL_STOCK, "SBFG3", "Carteira: Oportunidades de uma vida").toAssetHolding(nubank),

        VI("HASH11", "HASH11", ETF, "HASH11", "Carteira: Cripto").toAssetHolding(nubank),
        VI("IVVB11", "IVVB11", ETF, "IVVB11", "Carteira: Ações Internacionais").toAssetHolding(nubank),

        VI("TRXF11", "TRXF11", REAL_ESTATE_FUND, "TRXF11").toAssetHolding(inter),
        VI("KNSC11", "KNSC11", REAL_ESTATE_FUND, "KNSC11").toAssetHolding(inter),
        VI("RBRR11", "RBRR11", REAL_ESTATE_FUND, "RBRR11").toAssetHolding(inter),
        VI("XPML11", "XPML11", REAL_ESTATE_FUND, "XPML11").toAssetHolding(inter),
        VI("MCCI11", "MCCI11", REAL_ESTATE_FUND, "MCCI11").toAssetHolding(inter),
        VI("KNCR11", "KNCR11", REAL_ESTATE_FUND, "KNCR11").toAssetHolding(inter),
        VI("RCRB11", "RCRB11", REAL_ESTATE_FUND, "RCRB11").toAssetHolding(inter),
        VI("BRCO11", "BRCO11", REAL_ESTATE_FUND, "BRCO11").toAssetHolding(inter),
        VI("TEPP11", "TEPP11", REAL_ESTATE_FUND, "TEPP11").toAssetHolding(inter),
        VI("HGCR11", "HGCR11", REAL_ESTATE_FUND, "HGCR11").toAssetHolding(inter),
        VI("TRBL11", "TRBL11", REAL_ESTATE_FUND, "TRBL11").toAssetHolding(inter),
        VI("MCRE11", "MCRE11", REAL_ESTATE_FUND, "MCRE11").toAssetHolding(inter),
        VI("BTHF11", "BTHF11", REAL_ESTATE_FUND, "BTHF11").toAssetHolding(inter),
        VI("HSML11", "HSML11", REAL_ESTATE_FUND, "HSML11").toAssetHolding(inter),
        VI("HFOF11", "HFOF11", REAL_ESTATE_FUND, "HFOF11").toAssetHolding(inter),
        VI("AJFI11", "AJFI11", REAL_ESTATE_FUND, "AJFI11").toAssetHolding(inter),
        VI("HGLG11", "HGLG11", REAL_ESTATE_FUND, "HGLG11").toAssetHolding(inter),
        VI("VISC11", "VISC11", REAL_ESTATE_FUND, "VISC11").toAssetHolding(inter),
        VI("ALZR11", "ALZR11", REAL_ESTATE_FUND, "ALZR11").toAssetHolding(inter),
        VI("CLIN11", "CLIN11", REAL_ESTATE_FUND, "CLIN11").toAssetHolding(inter),
        VI("RBRF11", "RBRF11", REAL_ESTATE_FUND, "RBRF11").toAssetHolding(inter),
        VI("PSEC11", "PSEC11", REAL_ESTATE_FUND, "PSEC11").toAssetHolding(inter),
        VI("PVBI11", "PVBI11", REAL_ESTATE_FUND, "PVBI11").toAssetHolding(inter),
        VI("BTLG11", "BTLG11", REAL_ESTATE_FUND, "BTLG11").toAssetHolding(inter),
        VI("HGPO11", "HGPO11", REAL_ESTATE_FUND, "HGPO11").toAssetHolding(inter),
        VI("WHGR11", "WHGR11", REAL_ESTATE_FUND, "WHGR11").toAssetHolding(inter),
    )
    // @formatter:on


    private fun FI(
        observations: String,
        issuerName: String,
        type: FixedIncomeAssetType,
        subType: FixedIncomeSubType,
        expirationDate: LocalDate,
        yield: Double,
        liquidity: Liquidity,
    ) = FixedIncomeAsset(
        id = 0,
        observations = observations,
        issuer = getIssuer(issuerName),
        type = type,
        subType = subType,
        expirationDate = expirationDate,
        contractedYield = yield,
        liquidity = liquidity
    )

    private fun IF(
        name: String,
        issuerName: String,
        type: InvestmentFundAssetType,
    ) = InvestmentFundAsset(
        id = 0,
        name = name,
        issuer = getIssuer(issuerName),
        type = type,
        liquidity = Liquidity.D_PLUS_DAYS,
        liquidityDays = 60,
        expirationDate = null
    )

    private fun VI(
        name: String,
        issuerName: String,
        type: VariableIncomeAssetType,
        ticker: String,
        observations: String? = null,
    ) = VariableIncomeAsset(
        id = 0,
        name = name,
        issuer = getIssuer(issuerName),
        type = type,
        ticker = ticker,
        observations = observations
    )

    fun getAllHoldings(): List<AssetHoldingEntity> = buildList {
        AssetHolding(0, FI("SUPER POUP BMG", "BMG", POST_FIXED, CDB, LocalDate(2026, 5, 11), 110.0, Liquidity.DAILY), enir, Brokerage(0, "Banco do Brasil"))
    }

    private val enir = Owner(0, "Enir")

    private val nubank = Brokerage(0, "Nubank")
    private val bmg = Brokerage(0, "Banco do Brasil")
    private val inter = Brokerage(0, "Banco Inter")
    private val caixa = Brokerage(0, "Caixa Econômica Federal")
    private val sofisa = Brokerage(0, "Sofisa")
    private val btg = Brokerage(0, "BTG Pactual")


    private fun Asset.toAssetHolding(brokerage: Brokerage) = AssetHolding(0, this, enir, brokerage)
}
