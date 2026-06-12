package com.eferraz.usecases

import com.eferraz.entities.brokeragenotes.NoteFeeAllocation
import com.eferraz.usecases.repositories.BrokerageNoteRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
public class LoadBrokerageNoteUseCase (
    private val repository: BrokerageNoteRepository,
    private val dispatcher: CoroutineDispatcher
): AppUseCase<Unit, Unit>(dispatcher){

    override suspend fun execute(param: Unit) {
        val note = repository.loadNote().getOrNull()
        NoteFeeAllocation.calculate(note!!)
        println("OK!")
    }
}