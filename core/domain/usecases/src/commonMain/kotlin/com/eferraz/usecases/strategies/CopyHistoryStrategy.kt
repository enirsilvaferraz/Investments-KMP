package com.eferraz.usecases.strategies

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.usecases.GetQuotesUseCase
import com.eferraz.usecases.repositories.HoldingHistoryRepository
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import org.koin.core.annotation.Factory

/**
 *
 */
public interface CopyHistoryStrategy {

    public fun canHandle(holding: AssetHolding): Boolean
    public suspend fun create(referenceDate: YearMonth, holding: AssetHolding): HoldingHistoryEntry?

    /**
     *
     */
    @Factory(binds = [CopyHistoryStrategy::class])
    public class FixedIncomeHistoryStrategy(
        private val holdingHistoryRepository: HoldingHistoryRepository,
    ) : CopyHistoryStrategy {

        override fun canHandle(holding: AssetHolding): Boolean =
            holding.asset is FixedIncomeAsset || holding.asset is InvestmentFundAsset

        override suspend fun create(referenceDate: YearMonth, holding: AssetHolding): HoldingHistoryEntry? =
            holdingHistoryRepository.getByHoldingAndReferenceDate(referenceDate.minusMonth(), holding)
    }

    /**
     *
     */
    @Factory(binds = [CopyHistoryStrategy::class])
    public class VariableIncomeHistoryStrategy(
        private val holdingHistoryRepository: HoldingHistoryRepository,
        private val getQuotesUseCase: GetQuotesUseCase,
    ) : CopyHistoryStrategy {

        override fun canHandle(holding: AssetHolding): Boolean =
            holding.asset is VariableIncomeAsset

        override suspend fun create(referenceDate: YearMonth, holding: AssetHolding): HoldingHistoryEntry? {

            val asset = holding.asset as? VariableIncomeAsset
                ?: throw IllegalStateException("Asset is not a VariableIncomeAsset")

            val quoteHistory = getQuotesUseCase(GetQuotesUseCase.Params(asset.ticker, referenceDate))
                .getOrThrow()

            val endOfMonthValue = (quoteHistory.close ?: quoteHistory.adjustedClose)
                ?: throw IllegalStateException("Quote history is missing close or adjustedClose")

            holdingHistoryRepository.getByHoldingAndReferenceDate(referenceDate, holding)?.let {
                return it.copy(endOfMonthValue = endOfMonthValue)
            }

            return holdingHistoryRepository.getByHoldingAndReferenceDate(referenceDate.minusMonth(), holding)?.let {
                HoldingHistoryEntry(holding = holding, referenceDate = referenceDate, endOfMonthValue = endOfMonthValue, endOfMonthQuantity = it.endOfMonthQuantity)
            }
        }
    }
}