package com.eferraz.usecases.balancing

internal object BalancingConstants {

    const val HASH11: String = "HASH11"
    const val IVVB11: String = "IVVB11"

    val FII_REND_TICKERS: Set<String> = setOf(
        "BRCO11",
        "BTLG11",
        "HGBS11",
        "HSML11",
        "JSRE11",
        "KNCR11",
        "KNSC11",
        "MCCI11",
        "PVBI11",
    )

    val FII_TAT_TICKERS: Set<String> = setOf(
        "ALZR11",
        "CLIN11",
        "HGCR11",
        "KNIP11",
        "KORE11",
        "PMLL11",
        "VILG11",
        "VISC11",
    )

    val FII_FOF_TICKERS: Set<String> = setOf(
        "BTHF11",
        "BCIA11",
        "KNHF11",
        "MCRE11",
    )
}
