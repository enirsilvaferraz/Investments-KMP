package com.eferraz.usecases

import com.eferraz.usecases.repositories.AssetHoldingRepository
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.Factory

@Factory
public class UpdateVariableIncomeValuesUseCase(
    private val assetHoldingRepository: AssetHoldingRepository,
    private val createHistoryUseCase: CreateHistoryUseCase,
) : AppUseCase<UpdateVariableIncomeValuesUseCase.Param, Unit>() {

    public data class Param(val referenceDate: YearMonth)

    override suspend fun execute(param: Param) {
        assetHoldingRepository.getAllVariableIncomeAssets().forEach { holding ->
            createHistoryUseCase(CreateHistoryUseCase.Param(param.referenceDate, holding)).getOrThrow()
        }
    }
}