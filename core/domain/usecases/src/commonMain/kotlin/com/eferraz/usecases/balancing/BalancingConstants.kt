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

    val FII_FOF_TICKERS:Map<String, Double> = mapOf(
        "BTHF11" to 25.0,
        "BCIA11" to 25.0,
        "KNHF11" to 25.0,
        "MCRE11" to 25.0,
    )
}
