package com.eferraz.usecases

import com.eferraz.entities.Issuer
import com.eferraz.usecases.repositories.IssuerRepository
import org.koin.core.annotation.Factory

@Factory
public class GetOrCreateIssuerUseCase(
    private val issuerRepository: IssuerRepository,
) {

    /**
     * Obtém um emissor existente pelo nome ou cria um novo se não existir.
     * 
     * @param name Nome do emissor (será trimado)
     * @return Issuer existente ou recém-criado, ou null se o nome for inválido
     */
    public suspend operator fun invoke(name: String): Issuer? {
        val trimmedName = name.trim()
        
        if (trimmedName.isBlank()) {
            return null
        }

        // Verifica se já existe
        val existing = issuerRepository.getByName(trimmedName)
        if (existing != null) {
            return existing
        }

        // Cria novo
        return try {
            issuerRepository.create(trimmedName)
        } catch (e: Exception) {
            null
        }
    }
}

