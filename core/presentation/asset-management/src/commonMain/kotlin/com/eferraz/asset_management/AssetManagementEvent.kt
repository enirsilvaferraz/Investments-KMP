package com.eferraz.asset_management

import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.FixedIncomeSubType
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.design_system.input.date.filterDateMaskDigits

/**
 * Ações do ecrã de registo (formulário + diálogos e navegação) — ponto de entrada único [AssetManagementViewModel.dispatch].
 */
internal sealed class AssetManagementEvent {
    // Formulário
    data class CategoryChanged(val category: InvestmentCategory) : AssetManagementEvent()
    data class IssuerChanged(val issuer: Issuer) : AssetManagementEvent()
    data class ObservationsChanged(val value: String) : AssetManagementEvent()
    data class BrokerageChanged(val brokerage: Brokerage) : AssetManagementEvent()

    data class FixedTypeChanged(val type: FixedIncomeAssetType) : AssetManagementEvent()
    data class FixedSubTypeChanged(val subType: FixedIncomeSubType) : AssetManagementEvent()
    data class FixedExpirationChanged(val raw: String) : AssetManagementEvent()
    data class FixedYieldChanged(val value: String) : AssetManagementEvent()
    data class FixedCdiChanged(val value: String) : AssetManagementEvent()
    data class FixedLiquidityChanged(val liquidity: Liquidity) : AssetManagementEvent()

    data class VariableTypeChanged(val type: VariableIncomeAssetType) : AssetManagementEvent()
    data class VariableTickerChanged(val value: String) : AssetManagementEvent()
    data class VariableCnpjChanged(val value: String) : AssetManagementEvent()

    data class FundNameChanged(val value: String) : AssetManagementEvent()
    data class FundTypeChanged(val type: InvestmentFundAssetType) : AssetManagementEvent()
    data class FundLiquidityDaysChanged(val value: String) : AssetManagementEvent()
    data class FundExpirationChanged(val raw: String) : AssetManagementEvent()

    data object Save : AssetManagementEvent()
    data object RequestDismiss : AssetManagementEvent()
    data object ConfirmDiscard : AssetManagementEvent()
    data object CancelDiscard : AssetManagementEvent()
    data object NavigationConsumed : AssetManagementEvent()
}

/**
 * Aplica [AssetManagementEvent] de formulário a um [AssetDraft] e devolve o novo rascunho, ou `null` se o evento não mexe no rascunho.
 */
internal fun AssetDraft.applyFormEvent(event: AssetManagementEvent): AssetDraft? = when (event) {
    is AssetManagementEvent.CategoryChanged ->
        withCategoryPreservingIssuerAndObs(event.category)

    is AssetManagementEvent.IssuerChanged -> copy(issuer = event.issuer, errors = errors.clearIssuerError())
    is AssetManagementEvent.ObservationsChanged -> copy(observations = event.value)
    is AssetManagementEvent.BrokerageChanged ->
        copy(brokerage = event.brokerage, errors = errors.clearBrokerageError())

    is AssetManagementEvent.FixedTypeChanged -> copy(fixedType = event.type, errors = errors.clearFixedTypeError())
    is AssetManagementEvent.FixedSubTypeChanged -> copy(fixedSubType = event.subType, errors = errors.clearFixedSubTypeError())
    is AssetManagementEvent.FixedExpirationChanged -> copy(
        fixedExpiration = filterDateMaskDigits(event.raw),
        errors = errors.clearFixedExpirationError(),
    )

    is AssetManagementEvent.FixedYieldChanged -> copy(fixedYield = event.value, errors = errors.clearFixedYieldError())
    is AssetManagementEvent.FixedCdiChanged -> copy(fixedCdi = event.value, errors = errors.clearFixedCdiError())
    is AssetManagementEvent.FixedLiquidityChanged -> copy(fixedLiquidity = event.liquidity, errors = errors.clearFixedLiquidityError())

    is AssetManagementEvent.VariableTypeChanged -> copy(variableType = event.type, errors = errors.clearVariableTypeError())
    is AssetManagementEvent.VariableTickerChanged -> copy(variableTicker = event.value, errors = errors.clearVariableTickerError())
    is AssetManagementEvent.VariableCnpjChanged -> copy(variableCnpj = event.value, errors = errors.clearCnpjError())

    is AssetManagementEvent.FundNameChanged -> copy(fundName = event.value, errors = errors.clearFundNameError())
    is AssetManagementEvent.FundTypeChanged -> copy(fundType = event.type, errors = errors.clearFundTypeError())
    is AssetManagementEvent.FundLiquidityDaysChanged -> copy(fundLiquidityDays = event.value, errors = errors.clearFundLiquidityDaysError())
    is AssetManagementEvent.FundExpirationChanged -> copy(
        fundExpiration = filterDateMaskDigits(event.raw),
        errors = errors.clearFundExpirationError(),
    )

    is AssetManagementEvent.Save,
    is AssetManagementEvent.RequestDismiss,
    is AssetManagementEvent.ConfirmDiscard,
    is AssetManagementEvent.CancelDiscard,
    is AssetManagementEvent.NavigationConsumed,
    -> null
}

private fun AssetFormErrors.clearIssuerError() = copy(issuer = null)
private fun AssetFormErrors.clearBrokerageError() = copy(brokerage = null)
private fun AssetFormErrors.clearFixedTypeError() = copy(fixedType = null)
private fun AssetFormErrors.clearFixedSubTypeError() = copy(fixedSubType = null)
private fun AssetFormErrors.clearFixedExpirationError() = copy(fixedExpiration = null)
private fun AssetFormErrors.clearFixedYieldError() = copy(fixedYield = null)
private fun AssetFormErrors.clearFixedCdiError() = copy(fixedCdi = null)
private fun AssetFormErrors.clearFixedLiquidityError() = copy(fixedLiquidity = null)
private fun AssetFormErrors.clearVariableTypeError() = copy(variableType = null)
private fun AssetFormErrors.clearVariableTickerError() = copy(variableTicker = null)
private fun AssetFormErrors.clearCnpjError() = copy(cnpj = null)
private fun AssetFormErrors.clearFundNameError() = copy(fundName = null)
private fun AssetFormErrors.clearFundTypeError() = copy(fundType = null)
private fun AssetFormErrors.clearFundLiquidityError() = copy(fundLiquidity = null)
private fun AssetFormErrors.clearFundLiquidityDaysError() = copy(fundLiquidityDays = null)
private fun AssetFormErrors.clearFundExpirationError() = copy(fundExpiration = null)
