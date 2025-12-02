package com.eferraz.usecases

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import org.koin.core.annotation.Factory

@Factory
public class GetHoldingHistoryUseCase(
    private val holdingHistoryRepository: HoldingHistoryRepository,
) {

    public operator fun invoke(referenceDate: YearMonth): Flow<List<Triple<AssetHolding, HoldingHistoryEntry?, HoldingHistoryEntry?>>> {

        val previousMonth = referenceDate.minusMonth()

        return combine(
            holdingHistoryRepository.getAllHoldings(),
            holdingHistoryRepository.getByReferenceDate(referenceDate),
            holdingHistoryRepository.getByReferenceDate(previousMonth)
        ) { holdings, current, previous ->

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

            triples.values.toList()
        }
    }
}