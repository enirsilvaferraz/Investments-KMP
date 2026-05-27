package com.eferraz.filestore.b3.dto

internal sealed interface B3Position {

    fun isBlankRow(): Boolean

    /**
     * Retorna o identificador que será usado para correlacionar esta posição com
     * o ativo correspondente no banco de dados.
     *
     * - [B3StockPosition]       → `ticker`
     * - [B3EtfPosition]         → `ticker`
     * - [B3FundPosition]        → `ticker` (FIIs = VariableIncomeAsset no sistema)
     * - [B3FixedIncomePosition] → `code`
     * - [B3TreasuryPosition]    → `isinCode`
     */
    fun b3Identifier(): String

    /**
     * Retorna o valor financeiro atualizado desta posição, convertido para [Double].
     *
     * Campo de origem por subtipo:
     * - [B3StockPosition]       → `updatedValue`   ("Valor Atualizado")
     * - [B3EtfPosition]         → `updatedValue`   ("Valor Atualizado")
     * - [B3FundPosition]        → `updatedValue`   ("Valor Atualizado")
     * - [B3FixedIncomePosition] → `curveValue`     ("Valor Atualizado CURVA")
     * - [B3TreasuryPosition]    → `updatedValue`   ("Valor Atualizado")
     *
     * @throws NumberFormatException se o campo de valor não puder ser convertido.
     *   O chamador ([B3ImportDataSourceImpl]) deve capturar esta exceção por linha.
     */
    fun b3Value(): Double
}
