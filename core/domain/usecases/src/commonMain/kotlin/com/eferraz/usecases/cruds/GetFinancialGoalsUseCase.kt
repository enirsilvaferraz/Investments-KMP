package com.eferraz.usecases.cruds

import com.eferraz.entities.goals.FinancialGoal
import com.eferraz.usecases.AppUseCase
import com.eferraz.usecases.repositories.FinancialGoalRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
public class GetFinancialGoalsUseCase(
    private val financialGoalRepository: FinancialGoalRepository,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<GetFinancialGoalsUseCase.Param, List<FinancialGoal>>(context) {

    public sealed interface Param
    public data object All : Param
    public data class ByOwnerId(val ownerId: Long) : Param

    override suspend fun execute(param: Param): List<FinancialGoal> = when (param) {
        is All -> financialGoalRepository.getAll()
        is ByOwnerId -> financialGoalRepository.getByOwnerId(param.ownerId)
    }
}