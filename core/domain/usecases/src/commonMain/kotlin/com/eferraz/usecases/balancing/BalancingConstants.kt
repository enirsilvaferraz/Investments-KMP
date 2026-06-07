package com.eferraz.usecases.balancing

internal object BalancingConstants {

    const val HASH11: String = "HASH11"
    const val IVVB11: String = "IVVB11"

    val FII_REND_TICKERS: Map<String, Double> = mapOf(
        "BRCO11" to 4.5,
        "BTHF11" to 5.0,
        "BTLG11" to 13.5,
        "HGBS11" to 7.5,
        "HSML11" to 11.0,
        "JSRE11" to 7.5,
        "KNCR11" to 18.5,
        "KNSC11" to 13.5,
        "MCCI11" to 11.5,
        "PVBI11" to 7.5,
    )

    val FII_TAT_TICKERS: Map<String, Double> = mapOf(
        "ALZR11" to 12.5,
        "CLIN11" to 12.5,
        "HGCR11" to 10.0,
        "KNHF11" to 5.0,
        "KNIP11" to 10.0,
        "KORE11" to 5.0,
        "MCRE11" to 15.0,
        "PMLL11" to 7.5,
        "VILG11" to 15.0,
        "VISC11" to 7.5,
    )

    val NATIONAL_STOCK_TICKERS: Map<String, Double> = mapOf(
        "AXIA6" to 10.0,
        "BRBI11" to 4.0,
        "BPAC11" to 8.0,
        "CSAN3" to 12.0,
        "DIRR3" to 8.0,
        "ENEV3" to 4.0,
        "EQTL3" to 8.0,
        "EUCA4" to 4.0,
        "RENT3" to 9.0,
        "MDNE3" to 4.0,
        "PETR4" to 4.0,
        "PSSA3" to 9.0,
        "PRNR3" to 8.0,
        "SBFG3" to 8.0,
    )
}
