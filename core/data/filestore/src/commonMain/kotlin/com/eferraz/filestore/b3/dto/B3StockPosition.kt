package com.eferraz.filestore.b3.dto

import io.mamon.filemapper.annotation.ExcelColumn
import kotlinx.serialization.Serializable

@Serializable
internal data class B3StockPosition(
    @ExcelColumn(name = "Produto")                      val produto: String,
    @ExcelColumn(name = "Instituição")                  val institution: String,
    @ExcelColumn(name = "Conta")                        val account: String,
    @ExcelColumn(name = "Código de Negociação")         val ticker: String,
    @ExcelColumn(name = "CNPJ da Empresa")              val companyCnpj: String,
    @ExcelColumn(name = "Código ISIN / Distribuição")   val isinCode: String,
    @ExcelColumn(name = "Tipo")                         val type: String,
    @ExcelColumn(name = "Escriturador")                 val registrar: String,
    @ExcelColumn(name = "Quantidade")                   val quantity: String,
    @ExcelColumn(name = "Quantidade Disponível")        val availableQuantity: String,
    @ExcelColumn(name = "Quantidade Indisponível")      val unavailableQuantity: String,
    @ExcelColumn(name = "Motivo")                       val reason: String,
    @ExcelColumn(name = "Preço de Fechamento")          val closingPrice: String,
    @ExcelColumn(name = "Valor Atualizado")             val updatedValue: String,
) : B3Position {

    override fun isBlankRow(): Boolean = com.eferraz.filestore.b3.isBlankRow(
        produto, institution, account, ticker, companyCnpj, isinCode, type,
        registrar, quantity, availableQuantity, unavailableQuantity, reason,
        closingPrice, updatedValue,
    )
}
