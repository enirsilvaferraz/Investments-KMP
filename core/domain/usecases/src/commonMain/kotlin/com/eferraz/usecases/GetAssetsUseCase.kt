package com.eferraz.usecases

import com.eferraz.entities.Asset
import com.eferraz.usecases.repositories.AssetRepository
import org.koin.core.annotation.Factory

@Factory
internal class GetAssetsUseCase(
    private val assetRepository: AssetRepository,
) {

    operator fun invoke(): List<Asset> =
        assetRepository.getAll()
}