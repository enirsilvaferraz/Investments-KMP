package com.eferraz.usecases

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Factory

@Factory
public class GetHoldingHistoryUseCase(
    private val holdingHistoryRepository: HoldingHistoryRepository
) {

    public operator fun invoke(referenceDate: YearMonth): Flow<List<Triple<AssetHolding, HoldingHistoryEntry?, HoldingHistoryEntry?>>> =
        holdingHistoryRepository.getByReferenceDateAndPrevious(referenceDate)
}