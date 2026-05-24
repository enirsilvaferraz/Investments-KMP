package com.eferraz.filestore.b3.dto

import io.mamon.filemapper.annotation.ExcelColumn
import kotlinx.serialization.Serializable

@Serializable
internal data class B3FixedIncomePosition(
    @ExcelColumn(name = "Produto")                          val produto: String,
    @ExcelColumn(name = "Instituição")                      val institution: String,
    @ExcelColumn(name = "Emissor")                          val issuer: String,
    @ExcelColumn(name = "Código")                           val code: String,
    @ExcelColumn(name = "Indexador")                        val indexer: String,
    @ExcelColumn(name = "Tipo de regime")                   val regimeType: String,
    @ExcelColumn(name = "Data de Emissão")                  val issueDate: String,
    @ExcelColumn(name = "Vencimento")                       val maturityDate: String,
    @ExcelColumn(name = "Quantidade")                       val quantity: String,
    @ExcelColumn(name = "Quantidade Disponível")            val availableQuantity: String,
    @ExcelColumn(name = "Quantidade Indisponível")          val unavailableQuantity: String,
    @ExcelColumn(name = "Motivo")                           val reason: String,
    @ExcelColumn(name = "Contraparte")                      val counterparty: String,
    @ExcelColumn(name = "Preço Atualizado MTM")             val mtmPrice: String,
    @ExcelColumn(name = "Valor Atualizado MTM")             val mtmValue: String,
    @ExcelColumn(name = "Preço Atualizado CURVA")           val curvePrice: String,
    @ExcelColumn(name = "Valor Atualizado CURVA")           val curveValue: String,
    @ExcelColumn(name = "Preço Atualizado FECHAMENTO")      val closingPrice: String,
    @ExcelColumn(name = "Valor Atualizado FECHAMENTO")      val closingValue: String,
) : B3Position {

    override fun isBlankRow(): Boolean = com.eferraz.filestore.b3.isBlankRow(
        produto, institution, issuer, code, indexer, regimeType, issueDate,
        maturityDate, quantity, availableQuantity, unavailableQuantity, reason,
        counterparty, mtmPrice, mtmValue, curvePrice, curveValue, closingPrice,
        closingValue,
    )
}
