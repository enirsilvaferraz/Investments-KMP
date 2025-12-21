package com.eferraz.usecases

import com.eferraz.entities.Owner
import com.eferraz.usecases.repositories.OwnerRepository
import org.koin.core.annotation.Factory

@Factory
public class GetOwnerUseCase(
    private val ownerRepository: OwnerRepository,
) {

    public suspend operator fun invoke(): Owner? =
        ownerRepository.getFirst()
}

