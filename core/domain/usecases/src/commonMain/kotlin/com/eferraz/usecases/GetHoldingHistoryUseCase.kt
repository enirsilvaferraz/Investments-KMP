package com.eferraz.usecases

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Factory
public class GetHoldingHistoryUseCase(
    private val holdingHistoryRepository: HoldingHistoryRepository,
    private val getQuotesUseCase: GetQuotesUseCase,
) {

    @OptIn(ExperimentalTime::class)
    public suspend operator fun invoke(referenceDate: YearMonth): List<Triple<AssetHolding, HoldingHistoryEntry?, HoldingHistoryEntry?>> {

        val previousMonth = referenceDate.minusMonth()

        val holdings = holdingHistoryRepository.getAllHoldings() // TODO Mover para outro repository
        val current = holdingHistoryRepository.getByReferenceDate(referenceDate)
        val previous = holdingHistoryRepository.getByReferenceDate(previousMonth)

        val triples: HashMap<Long, Triple<AssetHolding, HoldingHistoryEntry?, HoldingHistoryEntry?>> =
            HashMap(holdings.map { Triple(it, null, null) }.associateBy { it.first.id })

        current.forEach { entry ->
            val holding = triples[entry.holding.id]?.first
            if (holding != null) {
                triples[entry.holding.id] = triples[entry.holding.id]!!.copy(second = entry)
            }
        }

        previous.forEach { entry ->
            val holding = triples[entry.holding.id]?.first
            if (holding != null) {
                triples[entry.holding.id] = triples[entry.holding.id]!!.copy(third = entry)
            }
        }

        triples.values.filter { it.second == null && it.first.asset !is VariableIncomeAsset }.forEach { (holding, _, previous) ->
println("Asset -> ${holding.asset.name}")
            if (previous != null) {
                HoldingHistoryEntry(
                    holding = holding,
                    referenceDate = referenceDate,
                    endOfMonthValue = previous.endOfMonthValue,
                    endOfMonthQuantity = previous.endOfMonthQuantity,
                    endOfMonthAverageCost = previous.endOfMonthAverageCost,
                    totalInvested = previous.totalInvested
                )
            } else {
                HoldingHistoryEntry(
                    holding = holding,
                    referenceDate = referenceDate,
                    endOfMonthValue = 0.0,
                    endOfMonthQuantity = 1.0,
                    endOfMonthAverageCost = 0.0,
                    totalInvested = 0.0
                )
            }.also {
                val id = holdingHistoryRepository.insert(it)
                triples[holding.id] = triples[holding.id]!!.copy(second = it.copy(id = id))
            }
        }

        if (referenceDate == Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).let { now -> YearMonth(now.year, now.month) }) {

            val filter = triples.values.filter { it.first.asset is VariableIncomeAsset && it.second == null }

            filter.forEach { (holding, current, previous) ->
                val quoteHistory = getQuotesUseCase((holding.asset as VariableIncomeAsset).ticker)
                val holdingHistoryEntry = HoldingHistoryEntry(
                    holding = holding,
                    referenceDate = referenceDate,
                    endOfMonthValue = quoteHistory.close ?: quoteHistory.adjustedClose ?: 0.0,
                    endOfMonthQuantity = current?.endOfMonthQuantity ?: previous?.endOfMonthQuantity ?: 0.0,
                    endOfMonthAverageCost = 0.0,
                    totalInvested = 0.0
                )
                val id = holdingHistoryRepository.insert(holdingHistoryEntry)
                triples[holding.id] = triples[holding.id]!!.copy(second = holdingHistoryEntry.copy(id = id))
            }
        }

        return triples.values.toList().also {

            it.sumOf { triple ->
                (triple.second?.endOfMonthValue ?: 0.0) * (triple.second?.endOfMonthQuantity ?: 0.0)
            }.also {
                println("Total: " + it)
            }
        }
    }
}