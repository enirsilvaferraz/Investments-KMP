package com.eferraz.usecases

import com.eferraz.entities.FinancialGoal
import com.eferraz.usecases.repositories.AssetHoldingRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
public class SetGoalToHoldingUseCase(
    private val assetHoldingRepository: AssetHoldingRepository,
    context: CoroutineDispatcher = Dispatchers.Default
) : AppUseCase<SetGoalToHoldingUseCase.Param, Unit>(context) {

    public data class Param(
        val assetId: Long,
        val goal: FinancialGoal?
    )

    override suspend fun execute(param: Param) {
        
        val existingHolding = assetHoldingRepository.getByAssetId(param.assetId)

        // Só atualiza se já existir um AssetHolding
        // (porque o holding precisa de corretora, que é obrigatória)
        if (existingHolding != null) {
            val updatedHolding = existingHolding.copy(goal = param.goal)
            assetHoldingRepository.save(updatedHolding)
        }
    }
}
