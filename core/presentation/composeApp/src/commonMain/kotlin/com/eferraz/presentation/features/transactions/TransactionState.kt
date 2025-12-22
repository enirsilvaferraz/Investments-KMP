package com.eferraz.presentation.features.transactions

import com.eferraz.entities.AssetHolding
import com.eferraz.entities.AssetTransaction
import com.eferraz.entities.TransactionType

internal data class TransactionState(
    val selectedHolding: AssetHolding? = null,
    val transactions: List<AssetTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val formData: TransactionFormData = TransactionFormData(),
    val validationErrors: Map<String, String> = emptyMap(),
    val successMessage: String? = null,
)

internal data class TransactionFormData(
    val type: TransactionType? = null,
    val date: String = "", // DD/MM/YYYY format
    val quantity: String = "",
    val unitPrice: String = "",
    val totalValue: String = "",
)

