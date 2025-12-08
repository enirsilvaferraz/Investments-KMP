package com.eferraz.usecases

import com.eferraz.entities.Brokerage
import com.eferraz.usecases.repositories.BrokerageRepository
import org.koin.core.annotation.Factory

@Factory
public class GetBrokeragesUseCase(
    private val brokerageRepository: BrokerageRepository,
) {

    public suspend operator fun invoke(): List<Brokerage> =
        brokerageRepository.getAll()
}

