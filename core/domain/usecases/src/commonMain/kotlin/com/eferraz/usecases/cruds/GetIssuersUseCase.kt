package com.eferraz.usecases.cruds

import com.eferraz.entities.assets.Issuer
import com.eferraz.usecases.AppUseCase
import com.eferraz.usecases.repositories.IssuerRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
public class GetIssuersUseCase(
    private val issuerRepository: IssuerRepository,
    context: CoroutineDispatcher = Dispatchers.Default
) : AppUseCase<GetIssuersUseCase.Param, List<Issuer>>(context) {

    public object Param

    override suspend fun execute(param: Param): List<Issuer> =
        issuerRepository.getAll()
}