package com.eferraz.pokedex.ui

import com.eferraz.pokedex.entities.FixedIncome
import org.koin.core.annotation.Factory

@Factory
internal class FilterFixedIncomeUseCase() {

    operator fun invoke(items: List<FixedIncome>, filter: FixedIncomeViewModel.FilterData) =
        items.filter(filter).sort(filter)

    private fun List<FixedIncome>.sort(filter: FixedIncomeViewModel.FilterData) =
        if (filter.ascending) sortedWith(compareBy { it.sortBy(filter) })
        else sortedWith(compareByDescending { it.sortBy(filter) })

    private fun List<FixedIncome>.filter(filter: FixedIncomeViewModel.FilterData) =
        filter { item ->
            with(filter) {

                val afterStartDate = filter.maturityStart?.toDate()?.let { it <= item.maturityDate } ?: true
                val beforeEndDate = filter.maturityEnd?.toDate()?.let { it >= item.maturityDate } ?: true
                val matchAnyWhere = filter.searchAnyWhere?.let { item.brokerageFirm.name.contains(it, true) || item.issuerBank.name.contains(it, true) } ?: true

                afterStartDate && beforeEndDate && matchAnyWhere
            }
        }

    private fun FixedIncome.sortBy(filter: FixedIncomeViewModel.FilterData): Comparable<*>? =
        when (filter.columnIndex) {
            0 -> brokerageFirm.name
            1 -> investedAmount
            2 -> currentAmount
            3 -> purchaseDate
            4 -> maturityDate
            5 -> profitability
            6 -> profitabilityIndex.ordinal
            7 -> type.name
            8 -> liquidity.name
            9 -> issuerBank.name
            else -> ""
        }
}