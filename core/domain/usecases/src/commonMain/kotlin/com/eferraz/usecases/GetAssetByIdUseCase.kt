package com.eferraz.usecases

import com.eferraz.entities.Asset
import com.eferraz.usecases.repositories.AssetRepository
import org.koin.core.annotation.Factory

@Factory
public class GetAssetByIdUseCase(
    private val assetRepository: AssetRepository,
) {

    public suspend operator fun invoke(id: Long): Asset? =
        assetRepository.getById(id)
}

