package com.eferraz.presentation.features.assetForm

import com.eferraz.usecases.entities.AssetFormData
import com.eferraz.usecases.entities.FixedIncomeFormData

internal data class AssetFormState(
    val issuers: List<String> = emptyList(),
    val brokerages: List<String> = emptyList(),
    val goals: List<String> = emptyList(),
    val formData: AssetFormData = FixedIncomeFormData(), // TODO VERIFICAR
    val validationErrors: Map<String, String> = emptyMap(),
    val message: String? = null,
    val isEditMode: Boolean = false,
    val shouldCloseForm: Boolean = false,
)

