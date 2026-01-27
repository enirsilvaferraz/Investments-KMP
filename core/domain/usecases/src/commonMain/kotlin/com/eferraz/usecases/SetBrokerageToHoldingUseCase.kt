package com.eferraz.usecases

import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.usecases.repositories.AssetHoldingRepository
import com.eferraz.usecases.repositories.AssetRepository
import com.eferraz.usecases.repositories.OwnerRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
public class SetBrokerageToHoldingUseCase(
    private val assetRepository: AssetRepository,
    private val assetHoldingRepository: AssetHoldingRepository,
    private val ownerRepository: OwnerRepository,
    context: CoroutineDispatcher = Dispatchers.Default
) : AppUseCase<SetBrokerageToHoldingUseCase.Param, Unit>(context) {

    public data class Param(
        val assetId: Long,
        val brokerage: Brokerage?
    )

    override suspend fun execute(param: Param) {
        
        val existingHolding = assetHoldingRepository.getByAssetId(param.assetId)

        when {
            
            // Caso 1: Corretora foi informada
            param.brokerage != null -> upsert(param.assetId, existingHolding, param.brokerage)
            
            // Caso 2: Corretora foi removida (null) e existe AssetHolding
            existingHolding != null -> delete(existingHolding)
        }
    }

    private suspend fun upsert(
        assetId: Long,
        existingHolding: AssetHolding?,
        brokerage: Brokerage,
    ) {
        val owner = ownerRepository.getFirst()

        if (owner != null) {

            val asset = assetRepository.getById(assetId) ?: return

            val holding = existingHolding?.copy(brokerage = brokerage, owner = owner)
                ?: AssetHolding(id = 0, asset = asset, owner = owner, brokerage = brokerage)

            assetHoldingRepository.save(holding)
        }
    }

    private suspend fun delete(existingHolding: AssetHolding) {
        assetHoldingRepository.delete(existingHolding.id)
    }
}

