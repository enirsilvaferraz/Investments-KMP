package com.eferraz.usecases

import com.eferraz.entities.Issuer
import com.eferraz.usecases.repositories.IssuerRepository
import org.koin.core.annotation.Factory

@Factory
public class GetIssuersUseCase(
    private val issuerRepository: IssuerRepository,
) {

    public suspend operator fun invoke(): List<Issuer> =
        issuerRepository.getAll()
}

