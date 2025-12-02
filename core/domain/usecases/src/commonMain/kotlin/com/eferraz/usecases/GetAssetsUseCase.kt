package com.eferraz.usecases

import com.eferraz.usecases.repositories.AssetRepository
import org.koin.core.annotation.Factory

@Factory
internal class GetAssetsUseCase(
    private val assetRepository: AssetRepository,
) {

    suspend operator fun invoke() =
        assetRepository.getAll()
}