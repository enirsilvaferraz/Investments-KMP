package com.eferraz.usecases

import com.eferraz.usecases.repositories.AssetRepository
import org.koin.core.annotation.Factory

@Factory
public class DeleteAssetUseCase(
    private val assetRepository: AssetRepository,
) {

    /**
     * Exclui um asset por ID.
     * 
     * @param id ID do asset a ser excluído
     * @throws IllegalArgumentException se o ID for inválido (menor ou igual a zero)
     */
    public suspend operator fun invoke(id: Long) {
        require(id > 0) { "ID do asset deve ser maior que zero" }
        assetRepository.delete(id)
    }
}

