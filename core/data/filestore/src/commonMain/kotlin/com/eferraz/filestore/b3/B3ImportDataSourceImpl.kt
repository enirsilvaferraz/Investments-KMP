package com.eferraz.filestore.b3

import com.eferraz.filestore.b3.dto.B3EtfPosition
import com.eferraz.filestore.b3.dto.B3FixedIncomePosition
import com.eferraz.filestore.b3.dto.B3FundPosition
import com.eferraz.filestore.b3.dto.B3Position
import com.eferraz.filestore.b3.dto.B3StockPosition
import com.eferraz.filestore.b3.dto.B3TreasuryPosition
import com.eferraz.usecases.entities.B3Record
import com.eferraz.usecases.repositories.B3ImportDataSource
import io.mamon.filemapper.FileMapper
import io.mamon.filemapper.FileMapperException
import io.mamon.filemapper.FileMapperType
import io.mamon.filemapper.provider.FileMapperPicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory

@Factory(binds = [B3ImportDataSource::class])
internal class B3ImportDataSourceImpl : B3ImportDataSource {

    private val fileMapper = FileMapper()

    override suspend fun import(): Result<List<B3Record>> = withContext(Dispatchers.Default) {

        val bytes = FileMapperPicker.pickFile(FileMapperType.XLSX)?.readBytes()
            ?: return@withContext Result.success(emptyList())

        val knownSheets = B3WorkbookSheets.extractKnownSheets(
            sheetsNames = B3Sheet.entries.map { it.workbookName },
            xlsxBytes = bytes,
        )

        val records = buildList {
            addAll(recordsFromSheet(B3Sheet.Acoes, knownSheets) { importRows<B3StockPosition>(it) })
            addAll(recordsFromSheet(B3Sheet.Etf, knownSheets) { importRows<B3EtfPosition>(it) })
            addAll(recordsFromSheet(B3Sheet.FundoDeInvestimento, knownSheets) { importRows<B3FundPosition>(it) })
            addAll(recordsFromSheet(B3Sheet.RendaFixa, knownSheets) { importRows<B3FixedIncomePosition>(it) })
            addAll(recordsFromSheet(B3Sheet.TesouroDireto, knownSheets) { importRows<B3TreasuryPosition>(it) })
        }

        Result.success(records)
    }

    private suspend inline fun <reified T : B3Position> recordsFromSheet(
        sheet: B3Sheet,
        knownSheets: Map<String, ByteArray>,
        crossinline parse: suspend (ByteArray) -> List<T>,
    ): List<B3Record> {
        val sheetBytes = knownSheets[sheet.workbookName] ?: return emptyList()
        return parse(sheetBytes).dropAfterBlankRow().mapNotNull { positionToRecord(it) }
    }

    private fun positionToRecord(position: B3Position): B3Record? =
        try {
            B3Record(position.b3Identifier(), position.b3Value())
        } catch (e: NumberFormatException) {
            println("WARN: valor inválido para '${position.b3Identifier()}' — linha ignorada")
            null
        }

    private suspend inline fun <reified T> importRows(bytes: ByteArray): List<T> {

        var rows: List<T>? = null
        var failure: FileMapperException? = null

        fileMapper.importData(
            bytes = bytes,
            fileType = FileMapperType.XLSX,
            onSuccess = { imported -> rows = imported },
            onFailed = { error -> failure = error },
        )

        failure?.let { throw it }
        return rows.orEmpty()
    }
}
