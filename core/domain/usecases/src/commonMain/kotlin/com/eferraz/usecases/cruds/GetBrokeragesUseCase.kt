package com.eferraz.usecases.cruds

import com.eferraz.entities.holdings.Brokerage
import com.eferraz.usecases.AppUseCase
import com.eferraz.usecases.repositories.BrokerageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
public class GetBrokeragesUseCase(
    private val brokerageRepository: BrokerageRepository,
    context: CoroutineDispatcher = Dispatchers.Default
) : AppUseCase<GetBrokeragesUseCase.Param, List<Brokerage>>(context) {

    public object Param

    override suspend fun execute(param: Param): List<Brokerage> =
        brokerageRepository.getAll()
}