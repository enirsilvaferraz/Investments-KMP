package com.eferraz.repositories.datasources

import com.eferraz.entities.Asset
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
import com.eferraz.entities.Issuer
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.entities.VariableIncomeAssetType
import com.eferraz.entities.liquidity.AtMaturity
import com.eferraz.entities.liquidity.Daily
import com.eferraz.entities.liquidity.FixedLiquidity
import com.eferraz.entities.liquidity.OnDaysAfterSale
import kotlinx.datetime.LocalDate

/**
 * Fonte de dados em memória que retorna todos os Assets cadastrados no sistema.
 * Os dados são extraídos do FixedIncomeRepository, retornando apenas os Assets,
 * sem informações de holdings (posições).
 *
 * Cada asset é único, mesmo que compartilhe o mesmo nome com outro asset,
 * pois podem ter características diferentes (ex: datas de vencimento diferentes).
 */
internal object AssetInMemoryDataSource {

    private val issuers = mutableMapOf<String, Issuer>()
    private var issuerIdCounter = 1L
    private var assetIdCounter = 1L

    private fun getIssuer(name: String): Issuer {
        return issuers.getOrPut(name) { Issuer(id = issuerIdCounter++, name = name) }
    }

    /**
     * Retorna todos os Assets cadastrados no sistema.
     * Cada asset é incluído na lista, mesmo que compartilhe o mesmo nome com outro asset,
     * pois podem ter características diferentes (ex: datas de vencimento diferentes).
     */
    val assets: List<Asset> = buildList {

        FI("SUPER POUP BMG", "BMG", POST_FIXED, CDB, LocalDate(2026, 5, 11), 110.0, Daily)
        FI("CDB ESCALONADO", "BMG", POST_FIXED, CDB, LocalDate(2027, 8, 29), 110.0, Daily)
        FI("CDB ESCALONADO", "BMG", POST_FIXED, CDB, LocalDate(2030, 4, 1), 112.0, Daily)
        FI("CDB ESCALONADO", "BMG", POST_FIXED, CDB, LocalDate(2030, 4, 2), 112.0, Daily)
        FI("CDB ESCALONADO", "BMG", POST_FIXED, CDB, LocalDate(2030, 9, 15), 106.0, Daily)
        FI("CDB PRE", "BMG", PRE_FIXED, CDB, LocalDate(2025, 10, 18), 12.96, AtMaturity)
        FI("Caixinha Turbo Ultravioleta", "Nubank", POST_FIXED, CDB, LocalDate(2030, 10, 18), 120.0, Daily)
        FI("Saldo Separado", "Nubank", POST_FIXED, CDB, LocalDate(2050, 1, 1), 100.0, Daily)
        FI("LCA BANCO ABC BRASIL", "ABC Brasil", POST_FIXED, LCA, LocalDate(2026, 7, 1), 90.5, AtMaturity)
        FI("CDB BANCO DIGIMAIS", "Banco Digimais", PRE_FIXED, CDB, LocalDate(2026, 7, 9), 11.29, AtMaturity)
        FI("CDB DM FINANCEIRA", "DM Financeira", POST_FIXED, CDB, LocalDate(2027, 5, 3), 113.0, AtMaturity)
        FI("CDB BANCO SEMEAR SA", "Banco Semear", POST_FIXED, CDB, LocalDate(2027, 6, 3), 122.0, AtMaturity)
        FI("CDB BANCO MASTER", "Banco Master", PRE_FIXED, CDB, LocalDate(2028, 12, 26), 15.5, AtMaturity)
        FI("CDB BANCO MASTER", "Banco Master", POST_FIXED, CDB, LocalDate(2029, 1, 2), 121.0, AtMaturity)
        FI("CDB BANCO MASTER", "Banco Master", POST_FIXED, CDB, LocalDate(2029, 4, 9), 125.0, AtMaturity)
        FI("LCA ORIGINAL", "Original", POST_FIXED, LCA, LocalDate(2026, 5, 4), 93.0, AtMaturity)
        FI("LCI ORIGINAL", "Original", POST_FIXED, LCI, LocalDate(2026, 4, 27), 93.0, AtMaturity)
        FI("LCI BRB", "BRB", POST_FIXED, LCI, LocalDate(2026, 10, 16), 91.5, AtMaturity)
        FI("LCI IPCA 252 TB BANCO BARI", "Banco Bari", INFLATION_LINKED, LCI, LocalDate(2025, 12, 22), 5.1, AtMaturity)
        FI("LCI IPCA FINAL 3 ANOS", "Inter", INFLATION_LINKED, LCI, LocalDate(2026, 5, 29), 5.28, AtMaturity)
        FI("CDB ARBI", "ARBI", POST_FIXED, CDB, LocalDate(2029, 1, 12), 115.0, AtMaturity)
        FI("CDB IPCA 252 TB BANCO MASTER", "Banco Master", INFLATION_LINKED, CDB, LocalDate(2026, 11, 17), 6.5, AtMaturity)
        FI("CDB IPCA 252 TB BANCO MASTER", "Banco Master", INFLATION_LINKED, CDB, LocalDate(2027, 4, 12), 7.28, AtMaturity)
        FI("CDB IPCA 252 TB BANCO MASTER", "Banco Master", PRE_FIXED, CDB, LocalDate(2027, 6, 28), 15.9, AtMaturity)
        FI("CDB DI FLUT TB BANCO MASTER", "Banco Master", POST_FIXED, CDB, LocalDate(2027, 3, 10), 115.0, AtMaturity)
        FI("LIG LIQUIDEZ", "Inter", POST_FIXED, CDB, LocalDate(2027, 9, 3), 87.0, AtMaturity)
        FI("FGTS", "FGTS", PRE_FIXED, CDB, LocalDate(2026, 12, 31), 0.0, AtMaturity)
        FI("CDB LIQUIDEZ DIARIA", "Sofisa", PRE_FIXED, CDB, LocalDate(2026, 9, 8), 110.0, Daily)

        // Investment Fund Assets
        IF("ARCA GRÃO", "Icatu", InvestmentFundAssetType.PENSION, null)
        IF("BTG Eletrobrás", "BTG", InvestmentFundAssetType.PENSION, null)

        // Variable Income Assets
        VI("AZZA3", "AZZA3", VariableIncomeAssetType.NATIONAL_STOCK, "AZZA3")
        VI("B3SA3", "B3SA3", VariableIncomeAssetType.NATIONAL_STOCK, "B3SA3")
        VI("BPAC11", "BPAC11", VariableIncomeAssetType.NATIONAL_STOCK, "BPAC11")
        VI("BRAV3", "BRAV3", VariableIncomeAssetType.NATIONAL_STOCK, "BRAV3")
        VI("BRBI11", "BRBI11", VariableIncomeAssetType.NATIONAL_STOCK, "BRBI11")
        VI("CASH3", "CASH3", VariableIncomeAssetType.NATIONAL_STOCK, "CASH3")
        VI("COCE5", "COCE5", VariableIncomeAssetType.NATIONAL_STOCK, "COCE5")
        VI("CSAN3", "CSAN3", VariableIncomeAssetType.NATIONAL_STOCK, "CSAN3")
        VI("DIRR3", "DIRR3", VariableIncomeAssetType.NATIONAL_STOCK, "DIRR3")
        VI("ELET6", "ELET6", VariableIncomeAssetType.NATIONAL_STOCK, "ELET6")
        VI("ENEV3", "ENEV3", VariableIncomeAssetType.NATIONAL_STOCK, "ENEV3")
        VI("EQTL3", "EQTL3", VariableIncomeAssetType.NATIONAL_STOCK, "EQTL3")
        VI("GOAU4", "GOAU4", VariableIncomeAssetType.NATIONAL_STOCK, "GOAU4")
        VI("IGTI11", "IGTI11", VariableIncomeAssetType.NATIONAL_STOCK, "IGTI11")
        VI("INTB3", "INTB3", VariableIncomeAssetType.NATIONAL_STOCK, "INTB3")
        VI("RENT3", "RENT3", VariableIncomeAssetType.NATIONAL_STOCK, "RENT3")
        VI("SBFG3", "SBFG3", VariableIncomeAssetType.NATIONAL_STOCK, "SBFG3")

        VI("HASH11", "HASH11", VariableIncomeAssetType.ETF, "HASH11")
        VI("IVVB11", "IVVB11", VariableIncomeAssetType.ETF, "IVVB11")

        VI("TRXF11", "TRXF11", VariableIncomeAssetType.REAL_ESTATE_FUND, "TRXF11")
        VI("KNSC11", "KNSC11", VariableIncomeAssetType.REAL_ESTATE_FUND, "KNSC11")
        VI("RBRR11", "RBRR11", VariableIncomeAssetType.REAL_ESTATE_FUND, "RBRR11")
        VI("XPML11", "XPML11", VariableIncomeAssetType.REAL_ESTATE_FUND, "XPML11")
        VI("MCCI11", "MCCI11", VariableIncomeAssetType.REAL_ESTATE_FUND, "MCCI11")
        VI("KNCR11", "KNCR11", VariableIncomeAssetType.REAL_ESTATE_FUND, "KNCR11")
        VI("RCRB11", "RCRB11", VariableIncomeAssetType.REAL_ESTATE_FUND, "RCRB11")
        VI("BRCO11", "BRCO11", VariableIncomeAssetType.REAL_ESTATE_FUND, "BRCO11")
        VI("TEPP11", "TEPP11", VariableIncomeAssetType.REAL_ESTATE_FUND, "TEPP11")
        VI("HGCR11", "HGCR11", VariableIncomeAssetType.REAL_ESTATE_FUND, "HGCR11")
        VI("TRBL11", "TRBL11", VariableIncomeAssetType.REAL_ESTATE_FUND, "TRBL11")
        VI("MCRE11", "MCRE11", VariableIncomeAssetType.REAL_ESTATE_FUND, "MCRE11")
        VI("BTHF11", "BTHF11", VariableIncomeAssetType.REAL_ESTATE_FUND, "BTHF11")
        VI("HSML11", "HSML11", VariableIncomeAssetType.REAL_ESTATE_FUND, "HSML11")
        VI("HFOF11", "HFOF11", VariableIncomeAssetType.REAL_ESTATE_FUND, "HFOF11")
        VI("AJFI11", "AJFI11", VariableIncomeAssetType.REAL_ESTATE_FUND, "AJFI11")
        VI("HGLG11", "HGLG11", VariableIncomeAssetType.REAL_ESTATE_FUND, "HGLG11")
        VI("VISC11", "VISC11", VariableIncomeAssetType.REAL_ESTATE_FUND, "VISC11")
        VI("ALZR11", "ALZR11", VariableIncomeAssetType.REAL_ESTATE_FUND, "ALZR11")
        VI("CLIN11", "CLIN11", VariableIncomeAssetType.REAL_ESTATE_FUND, "CLIN11")
        VI("RBRF11", "RBRF11", VariableIncomeAssetType.REAL_ESTATE_FUND, "RBRF11")
        VI("PSEC11", "PSEC11", VariableIncomeAssetType.REAL_ESTATE_FUND, "PSEC11")
        VI("PVBI11", "PVBI11", VariableIncomeAssetType.REAL_ESTATE_FUND, "PVBI11")
        VI("BTLG11", "BTLG11", VariableIncomeAssetType.REAL_ESTATE_FUND, "BTLG11")
        VI("HGPO11", "HGPO11", VariableIncomeAssetType.REAL_ESTATE_FUND, "HGPO11")
        VI("WHGR11", "WHGR11", VariableIncomeAssetType.REAL_ESTATE_FUND, "WHGR11")
    }

    private fun MutableList<Asset>.FI(
        observations: String,
        issuerName: String,
        type: FixedIncomeAssetType,
        subType: FixedIncomeSubType,
        expirationDate: LocalDate,
        yield: Double,
        liquidity: FixedLiquidity,
    ) {
        add(
            FixedIncomeAsset(
                id = assetIdCounter++,
                observations = observations,
                issuer = getIssuer(issuerName),
                type = type,
                subType = subType,
                expirationDate = expirationDate,
                contractedYield = yield,
                cdiRelativeYield = null,
                liquidity = liquidity
            )
        )
    }

    private fun MutableList<Asset>.IF(
        name: String,
        issuerName: String,
        type: InvestmentFundAssetType,
        expirationDate: LocalDate?,
    ) {
        add(
            InvestmentFundAsset(
                id = assetIdCounter++,
                name = name,
                issuer = getIssuer(issuerName),
                type = type,
                liquidity = OnDaysAfterSale(60),
                expirationDate = expirationDate
            )
        )
    }

    private fun MutableList<Asset>.VI(
        name: String,
        issuerName: String,
        type: VariableIncomeAssetType,
        ticker: String,
    ) {
        add(
            VariableIncomeAsset(
                id = assetIdCounter++,
                name = name,
                issuer = getIssuer(issuerName),
                type = type,
                ticker = ticker,
                liquidity = OnDaysAfterSale(2)
            )
        )
    }
}