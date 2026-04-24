package com.eferraz.usecases.cruds

import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.usecases.AppUseCase
import com.eferraz.usecases.exceptions.ValidateException
import com.eferraz.usecases.repositories.AssetRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Grava [Param.asset] via [AssetRepository.upsert]. Valida regras de negócio por subtipo de [Asset].
 */
@Factory
public class UpsertAssetUseCase(
    private val assetRepository: AssetRepository,
    context: CoroutineDispatcher = Dispatchers.Default,
) : AppUseCase<UpsertAssetUseCase.Param, Long>(context) {

    public data class Param(val asset: Asset)

    @OptIn(ExperimentalTime::class)
    override suspend fun execute(param: Param): Long {

        val errors = mutableMapOf<String, String>()

        errors += catalogRefErrors(issuer = param.asset.issuer)

        errors += when (val a = param.asset) {
            is FixedIncomeAsset -> validateFixedIncome(a)
            is VariableIncomeAsset -> validateVariableIncome(a)
            is InvestmentFundAsset -> validateInvestmentFund(a)
        }

        if (errors.isNotEmpty()) {
            throw ValidateException(errors)
        }

        return assetRepository.upsert(param.asset)
    }

    private fun catalogRefErrors(issuer: Issuer): Map<String, String> {

        val m = mutableMapOf<String, String>()

        if (issuer.id <= 0L) {
            m["issuer"] = "Selecione um emissor"
        }

        return m
    }

    @OptIn(ExperimentalTime::class)
    private fun validateFixedIncome(asset: FixedIncomeAsset): Map<String, String> {

        val errors = mutableMapOf<String, String>()

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        if (asset.expirationDate <= today) {
            errors["expirationDate"] = "Data de vencimento deve ser futura"
        }

        if (asset.contractedYield <= 0) {
            errors["contractedYield"] = "Rentabilidade deve ser positiva"
        }

        asset.cdiRelativeYield?.let { cdi ->
            if (cdi <= 0) {
                errors["cdiRelativeYield"] = "Rentabilidade relativa ao CDI deve ser positiva"
            }
        }

        return errors
    }

    private fun validateVariableIncome(asset: VariableIncomeAsset): Map<String, String> {

        val errors = mutableMapOf<String, String>()

        if (asset.name.isBlank()) {
            errors["assetName"] = "Campo obrigatório"
        }

        if (asset.ticker.isBlank()) {
            errors["ticker"] = "Campo obrigatório"
        }

        return errors
    }

    @OptIn(ExperimentalTime::class)
    private fun validateInvestmentFund(asset: InvestmentFundAsset): Map<String, String> {

        val errors = mutableMapOf<String, String>()

        if (asset.name.isBlank()) {
            errors["name"] = "Campo obrigatório"
        }

        if (asset.liquidityDays <= 0) {
            errors["liquidityDays"] = "Dias para resgate deve ser um número positivo"
        }

        asset.expirationDate?.let { date ->

            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            if (date <= today) {
                errors["expirationDate"] = "Data de vencimento deve ser futura"
            }
        }

        return errors
    }
}
