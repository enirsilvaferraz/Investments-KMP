package com.eferraz.usecases

import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.entities.brokeragenotes.NoteFeeAllocation
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.transactions.AssetTransaction
import com.eferraz.usecases.repositories.AssetHoldingRepository
import com.eferraz.usecases.repositories.AssetTransactionRepository
import com.eferraz.usecases.repositories.BrokerageNoteRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
public class ImportBrokerageNoteUseCase(
    private val brokerageNoteRepository: BrokerageNoteRepository,
    private val assetHoldingRepository: AssetHoldingRepository,
    private val assetTransactionRepository: AssetTransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<Unit, Unit>(dispatcher) {

    override suspend fun execute(param: Unit) {
        val note = brokerageNoteRepository.loadNote().getOrElse { error ->
            println("Failed to load note: ${error.message}")
            return
        }

        val allocation = NoteFeeAllocation.calculate(note)
        val entries = mutableListOf<Pair<AssetHolding, AssetTransaction>>()

        for (noteAsset in allocation.assets) {
            val holding = assetHoldingRepository.getByTicker(noteAsset.ticker)
            if (holding == null) {
                println("Holding not found for ticker=${noteAsset.ticker}")
                return
            }

            entries.add(holding to noteAsset.transaction)
        }

//        assetTransactionRepository.saveAll(entries)
        entries.forEach {
            println("Saving transaction: ${(it.first.asset as VariableIncomeAsset).ticker} ${it.second}")
        }

        println("Import complete: ${entries.size} transactions saved")
    }
}
