package com.eferraz.usecases.cruds

import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.usecases.AppUseCase
import com.eferraz.usecases.exceptions.ValidateException
import com.eferraz.usecases.repositories.AssetHoldingRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

/**
 * Grava [Param.assetHolding] via [AssetHoldingRepository.upsert].
 * Valida a corretora do [AssetHolding] (catálogo) de forma alinhada à validação de emissor em [UpsertAssetUseCase].
 */
@Factory
public class UpsertAssetHoldingUseCase(
    private val assetHoldingRepository: AssetHoldingRepository,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<UpsertAssetHoldingUseCase.Param, Long>(context) {

    public data class Param(val assetHolding: AssetHolding)

    override suspend fun execute(param: Param): Long {

        val errors = mutableMapOf<String, String>()

        errors += catalogRefErrors(brokerage = param.assetHolding.brokerage)

        if (errors.isNotEmpty()) {
            throw ValidateException(errors)
        }

        return assetHoldingRepository.upsert(param.assetHolding)
    }

    private fun catalogRefErrors(brokerage: Brokerage): Map<String, String> {

        val m = mutableMapOf<String, String>()

        if (brokerage.id <= 0L) {
            m["brokerage"] = "Selecione uma corretora"
        }

        return m
    }
}
