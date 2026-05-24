package com.eferraz.filestore.b3.dto

import io.mamon.filemapper.annotation.ExcelColumn
import kotlinx.serialization.Serializable

@Serializable
internal data class B3TreasuryPosition(
    @ExcelColumn(name = "Produto")                  val produto: String,
    @ExcelColumn(name = "Instituição")              val institution: String,
    @ExcelColumn(name = "Código ISIN")              val isinCode: String,
    @ExcelColumn(name = "Indexador")                val indexer: String,
    @ExcelColumn(name = "Vencimento")               val maturityDate: String,
    @ExcelColumn(name = "Quantidade")               val quantity: String,
    @ExcelColumn(name = "Quantidade Disponível")    val availableQuantity: String,
    @ExcelColumn(name = "Quantidade Indisponível")  val unavailableQuantity: String,
    @ExcelColumn(name = "Motivo")                   val reason: String,
    @ExcelColumn(name = "Valor Aplicado")           val appliedValue: String,
    @ExcelColumn(name = "Valor bruto")              val grossValue: String,
    @ExcelColumn(name = "Valor líquido")            val netValue: String,
    @ExcelColumn(name = "Valor Atualizado")         val updatedValue: String,
) : B3Position {

    override fun isBlankRow(): Boolean = com.eferraz.filestore.b3.isBlankRow(
        produto, institution, isinCode, indexer, maturityDate, quantity,
        availableQuantity, unavailableQuantity, reason, appliedValue, grossValue,
        netValue, updatedValue,
    )
}
