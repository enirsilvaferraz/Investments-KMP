package com.eferraz.repositories

import com.eferraz.entities.Asset
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.FixedIncomeAssetType
import com.eferraz.entities.FixedIncomeAssetType.INFLATION_LINKED
import com.eferraz.entities.FixedIncomeAssetType.POST_FIXED
import com.eferraz.entities.FixedIncomeAssetType.PRE_FIXED
import com.eferraz.entities.FixedIncomeSubType
import com.eferraz.entities.FixedIncomeSubType.*
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.InvestmentFundAssetType
import com.eferraz.entities.InvestmentFundAssetType.PENSION
import com.eferraz.entities.Issuer
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.entities.VariableIncomeAssetType
import com.eferraz.entities.VariableIncomeAssetType.ETF
import com.eferraz.entities.VariableIncomeAssetType.NATIONAL_STOCK
import com.eferraz.entities.VariableIncomeAssetType.REAL_ESTATE_FUND
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

        // Fixed Income Assets (extraídos do FixedIncomeRepository)
        FI(observations = "SUPER POUP BMG", issuerName = "BMG", type = POST_FIXED, subType = CDB, expirationDate = LocalDate(2026, 5, 11), yield = 110.0, liquidity = Daily)
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
        IF("ARCA GRÃO", "Icatu", PENSION, null)
        IF("BTG Eletrobrás", "BTG", PENSION, null)

        // Variable Income Assets
        VI("AZZA3", "AZZA3", NATIONAL_STOCK, "AZZA3")
        VI("B3SA3", "B3SA3", NATIONAL_STOCK, "B3SA3")
        VI("BPAC11", "BPAC11", NATIONAL_STOCK, "BPAC11")
        VI("BRAV3", "BRAV3", NATIONAL_STOCK, "BRAV3")
        VI("BRBI11", "BRBI11", NATIONAL_STOCK, "BRBI11")
        VI("CASH3", "CASH3", NATIONAL_STOCK, "CASH3")
        VI("COCE5", "COCE5", NATIONAL_STOCK, "COCE5")
        VI("CSAN3", "CSAN3", NATIONAL_STOCK, "CSAN3")
        VI("DIRR3", "DIRR3", NATIONAL_STOCK, "DIRR3")
        VI("ELET6", "ELET6", NATIONAL_STOCK, "ELET6")
        VI("ENEV3", "ENEV3", NATIONAL_STOCK, "ENEV3")
        VI("EQTL3", "EQTL3", NATIONAL_STOCK, "EQTL3")
        VI("GOAU4", "GOAU4", NATIONAL_STOCK, "GOAU4")
        VI("HASH11", "HASH11", ETF, "HASH11")
        VI("IGTI11", "IGTI11", NATIONAL_STOCK, "IGTI11")
        VI("INTB3", "INTB3", NATIONAL_STOCK, "INTB3")
        VI("IVVB11", "IVVB11", ETF, "IVVB11")
        VI("RENT3", "RENT3", NATIONAL_STOCK, "RENT3")
        VI("SBFG3", "SBFG3", NATIONAL_STOCK, "SBFG3")
        VI("FII", "FII", REAL_ESTATE_FUND, "FII")
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

