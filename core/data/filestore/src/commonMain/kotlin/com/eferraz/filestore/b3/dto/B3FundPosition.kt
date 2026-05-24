package com.eferraz.filestore.b3.dto

import io.mamon.filemapper.annotation.ExcelColumn
import kotlinx.serialization.Serializable

@Serializable
internal data class B3FundPosition(
    @ExcelColumn(name = "Produto")                      override val produto: String,
    @ExcelColumn(name = "Instituição")                  val institution: String,
    @ExcelColumn(name = "Conta")                        val account: String,
    @ExcelColumn(name = "Código de Negociação")         val ticker: String,
    @ExcelColumn(name = "CNPJ do Fundo")                val fundCnpj: String,
    @ExcelColumn(name = "Código ISIN / Distribuição")   val isinCode: String,
    @ExcelColumn(name = "Tipo")                         val type: String,
    @ExcelColumn(name = "Administrador")                val administrator: String,
    @ExcelColumn(name = "Quantidade")                   val quantity: String,
    @ExcelColumn(name = "Quantidade Disponível")        val availableQuantity: String,
    @ExcelColumn(name = "Quantidade Indisponível")      val unavailableQuantity: String,
    @ExcelColumn(name = "Motivo")                       val reason: String,
    @ExcelColumn(name = "Preço de Fechamento")          val closingPrice: String,
    @ExcelColumn(name = "Valor Atualizado")             val updatedValue: String,
) : B3Position
