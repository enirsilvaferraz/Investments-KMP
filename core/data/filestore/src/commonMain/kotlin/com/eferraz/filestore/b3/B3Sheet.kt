package com.eferraz.filestore.b3

/** Nomes exatos das guias no export B3 (FR-004). */
internal enum class B3Sheet(val workbookName: String) {
    Acoes("Acoes"),
    Etf("ETF"),
    FundoDeInvestimento("Fundo de Investimento"),
    RendaFixa("Renda Fixa"),
    TesouroDireto("Tesouro Direto"),
}