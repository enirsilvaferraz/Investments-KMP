package com.eferraz.usecases

import com.eferraz.entities.holdings.Owner
import com.eferraz.usecases.repositories.OwnerRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
public class GetOwnerUseCase(
    private val ownerRepository: OwnerRepository,
    context: CoroutineDispatcher = Dispatchers.Default
) : AppUseCase<GetOwnerUseCase.Param, Owner?>(context) {

    public object Param

    override suspend fun execute(param: Param): Owner? =
        ownerRepository.getFirst()
}

