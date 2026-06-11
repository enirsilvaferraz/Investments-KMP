package com.eferraz.entities.brokeragenotes

import kotlinx.datetime.LocalDate

internal object CanonicalNoteFixtures {

    internal fun simplifiedThreeAssetNote(
        netValue: Double = 1004.54,
        apportionableFees: ApportionableFees = ApportionableFees(
            settlement = 3.54,
            emoluments = 1.00,
            transfer = 0.00,
            brokerage = 0.00,
            iss = 0.00,
            others = 0.00,
        ),
        assets: List<NoteAsset> = listOf(
            NoteAsset("AJFI11", "AJFI11 CI", TradeType.BUY, 100.0, 10.00, 1000.00),
            NoteAsset("BRCO11", "BRCO11 CI", TradeType.SELL, 10.0, 100.00, 1000.00),
            NoteAsset("VILG11", "VILG11 CI", TradeType.BUY, 1000.0, 1.00, 1000.00),
        ),
    ): BrokerageNote = BrokerageNote(
        metadata = BrokerageNoteMetadata(
            noteNumber = "12345",
            tradingDate = LocalDate(2026, 1, 1),
            settlementDate = LocalDate(2026, 1, 3),
            brokerage = "Corretora Teste",
            brokerageDocument = "00.000.000/0001-00",
            netValue = netValue,
        ),
        financialSummary = FinancialSummary(
            totalVolumeTraded = assets.sumOf { it.grossValue },
            totalBuys = assets.filter { it.tradeType == TradeType.BUY }.sumOf { it.grossValue },
            totalSells = assets.filter { it.tradeType == TradeType.SELL }.sumOf { it.grossValue },
            apportionableFees = apportionableFees,
            withheldTaxes = WithheldTaxes(irrfOperations = 0.00, irrfDayTrade = 0.00),
        ),
        assets = assets,
    )

    internal fun fullCanonicalNote(): BrokerageNote = BrokerageNote(
        metadata = BrokerageNoteMetadata(
            noteNumber = "8779222",
            tradingDate = LocalDate(2026, 6, 9),
            settlementDate = LocalDate(2026, 6, 11),
            brokerage = "Nu Investimentos S.A.",
            brokerageDocument = "62.169.875/0001-79",
            // SINACOR raw `valor_liquido_nota` is −33705.98 (negative = client debit in source JSON).
            // Domain convention: positive = client debit → +33705.98.
            netValue = 33705.98,
        ),
        financialSummary = FinancialSummary(
            totalVolumeTraded = 48912.22,
            totalBuys = 41301.77,
            totalSells = 7610.45,
            apportionableFees = ApportionableFees(
                settlement = 10.95,
                emoluments = 2.44,
                transfer = 1.27,
                brokerage = 0.00,
                iss = 0.00,
                others = 0.00,
            ),
            withheldTaxes = WithheldTaxes(irrfOperations = 0.38, irrfDayTrade = 0.00),
        ),
        assets = fullCanonicalAssets(),
    )

    private fun fullCanonicalAssets(): List<NoteAsset> = listOf(
        NoteAsset("AXIA3", "AXIASE ON NM", TradeType.BUY, 53.0, 51.06, 2706.18),
        NoteAsset("BRBI11", "BRB111F UNT N2", TradeType.BUY, 42.0, 15.13, 635.46),
        NoteAsset("BRBI11", "BRBI11F UNT N2", TradeType.BUY, 1.0, 15.14, 15.14),
        NoteAsset("BRBI11", "BRB111F UNT N2", TradeType.BUY, 3.0, 15.15, 45.45),
        NoteAsset("DIRR3", "DIRR3F ON NM", TradeType.BUY, 50.0, 12.84, 642.0),
        NoteAsset("EUCA4", "EUCA4F PN N1", TradeType.BUY, 56.0, 25.15, 1408.4),
        NoteAsset("SBFG3", "SBFG3F ON NM", TradeType.BUY, 97.0, 10.59, 1027.23),
        NoteAsset("SBFG3", "SBFG3F ON NM", TradeType.BUY, 1.0, 10.6, 10.6),
        NoteAsset("RENT3", "RENT3F ON NM", TradeType.BUY, 1.0, 40.82, 40.82),
        NoteAsset("RENT3", "RENT3F ON NM", TradeType.BUY, 50.0, 40.85, 2042.5),
        NoteAsset("MDNE3", "MDNE3F ON NM", TradeType.BUY, 40.0, 26.97, 1078.8),
        NoteAsset("PETR4", "PETR4F PN EJ N2", TradeType.BUY, 21.0, 41.12, 863.52),
        NoteAsset("PRNR3", "PRNR3F ON NM", TradeType.BUY, 8.0, 18.15, 145.2),
        NoteAsset("PRNR3", "PRNR3F ON NM", TradeType.BUY, 18.0, 18.16, 326.88),
        NoteAsset("BPAC11", "BPAC11F UNT N2", TradeType.SELL, 2.0, 50.97, 101.94),
        NoteAsset("ENEV3", "ENEV3F ON NM", TradeType.SELL, 59.0, 24.22, 1428.98),
        NoteAsset("EQTL3", "EQTL3F ON NM", TradeType.SELL, 9.0, 38.73, 348.57),
        NoteAsset("EQTL3", "EQTL3F ON NM", TradeType.SELL, 1.0, 38.74, 38.74),
        NoteAsset("PSSA3", "PSSA3F ON NM", TradeType.SELL, 8.0, 48.48, 387.84),
        NoteAsset("AXIA3", "AXIA3 ON NM", TradeType.BUY, 100.0, 51.04, 5104.0),
        NoteAsset("BRBI11", "BRB111 UNT N2", TradeType.BUY, 200.0, 15.12, 3024.0),
        NoteAsset("CSAN3", "CSAN3 ON NM", TradeType.BUY, 2000.0, 3.39, 6780.0),
        NoteAsset("BRCO11", "BRCO11 CI ER", TradeType.BUY, 29.0, 113.53, 3292.37),
        NoteAsset("BTLG11", "BTLG11 CI", TradeType.BUY, 24.0, 102.45, 2458.8),
        NoteAsset("HSML11", "HSML11 CI ER", TradeType.BUY, 16.0, 88.53, 1416.48),
        NoteAsset("HSML11", "HSML11 CI ER", TradeType.BUY, 11.0, 88.54, 973.94),
        NoteAsset("PRNR3", "PRNR3 ON NM", TradeType.BUY, 400.0, 18.16, 7264.0),
        NoteAsset("BTHF11", "BTHF11 CI ER", TradeType.SELL, 217.0, 8.96, 1944.32),
        NoteAsset("HGBS11", "HGBS11 CI ER", TradeType.SELL, 75.0, 19.74, 1480.5),
        NoteAsset("KNSC11", "KNSC11 CIER", TradeType.SELL, 207.0, 9.08, 1879.56),
    )
}
