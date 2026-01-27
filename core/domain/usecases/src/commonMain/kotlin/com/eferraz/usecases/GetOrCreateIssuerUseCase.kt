package com.eferraz.usecases

import com.eferraz.entities.assets.Issuer
import com.eferraz.usecases.repositories.IssuerRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
public class GetOrCreateIssuerUseCase(
    private val issuerRepository: IssuerRepository,
    context: CoroutineDispatcher = Dispatchers.Default
) : AppUseCase<GetOrCreateIssuerUseCase.Param, Issuer?>(context) {

    public data class Param(val name: String)

    /**
     * Obtém um emissor existente pelo nome ou cria um novo se não existir.
     *
     * @param param Parametro contendo o Nome do emissor (será trimado)
     * @return Issuer existente ou recém-criado, ou null se o nome for inválido
     */
    override suspend fun execute(param: Param): Issuer? {
        val trimmedName = param.name.trim()

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

