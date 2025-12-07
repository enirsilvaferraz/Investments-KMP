package com.eferraz.usecases

import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.usecases.repositories.AssetRepository
import org.koin.core.annotation.Factory

@Factory
public class SaveFixedIncomeAssetUseCase(
    private val assetRepository: AssetRepository,
) {

    /**
     * Salva ou atualiza um asset de renda fixa.
     * 
     * @param asset Asset de renda fixa a ser salvo
     * @param isEditing Se true, atualiza o asset existente; se false, cria um novo
     * @return ID do asset salvo
     */
    public suspend operator fun invoke(asset: FixedIncomeAsset, isEditing: Boolean): Long {
        return if (isEditing) {
            assetRepository.update(asset)
            asset.id
        } else {
            assetRepository.save(asset)
        }
    }
}

