package com.eferraz.filestore.b3

import com.eferraz.filestore.b3.dto.B3EtfPosition
import com.eferraz.filestore.b3.dto.B3FixedIncomePosition
import com.eferraz.filestore.b3.dto.B3FundPosition
import com.eferraz.filestore.b3.dto.B3Position
import com.eferraz.filestore.b3.dto.B3StockPosition
import com.eferraz.filestore.b3.dto.B3TreasuryPosition
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

    override suspend fun importAndLog(): Result<Unit> = withContext(Dispatchers.Default) {

        val bytes = FileMapperPicker.pickFile(FileMapperType.XLSX)?.readBytes()!!

        val knownSheets = B3WorkbookSheets.extractKnownSheets(B3Sheet.entries.map { it.workbookName }, bytes,)

        logSheet(B3Sheet.Acoes, knownSheets) { importRows<B3StockPosition>(it) }
        logSheet(B3Sheet.Etf, knownSheets) { importRows<B3EtfPosition>(it) }
        logSheet(B3Sheet.FundoDeInvestimento, knownSheets) { importRows<B3FundPosition>(it) }
        logSheet(B3Sheet.RendaFixa, knownSheets) { importRows<B3FixedIncomePosition>(it) }
        logSheet(B3Sheet.TesouroDireto, knownSheets) { importRows<B3TreasuryPosition>(it) }

        Result.success(Unit)
    }

    private suspend inline fun <reified T : B3Position> logSheet(
        sheet: B3Sheet,
        knownSheets: Map<String, ByteArray>,
        crossinline parse: suspend (ByteArray) -> List<T>,
    ) {
        val sheetBytes = knownSheets[sheet.workbookName] ?: return
        parse(sheetBytes).dropAfterBlankRow().forEach { println(it) }
        println()
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
