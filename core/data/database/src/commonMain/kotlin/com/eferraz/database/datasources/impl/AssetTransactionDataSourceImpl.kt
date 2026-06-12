package com.eferraz.database.datasources.impl

import com.eferraz.database.daos.AssetTransactionDao
import com.eferraz.database.datasources.AssetTransactionDataSource
import com.eferraz.database.mappers.toDomain
import com.eferraz.database.mappers.toEntity
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.transactions.AssetTransaction
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Factory

@Factory(binds = [AssetTransactionDataSource::class])
internal class AssetTransactionDataSourceImpl(
    private val assetTransactionDao: AssetTransactionDao,
) : AssetTransactionDataSource {

    override suspend fun save(holding: AssetHolding, transaction: AssetTransaction): Long =
        assetTransactionDao.save(transaction.toEntity(holding.id))

    override suspend fun find(id: Long, holdingId: Long): AssetTransaction? {
        val entity = assetTransactionDao.find(id) ?: return null
        if (entity.holdingId != holdingId) return null
        return entity.toDomain()
    }

    override suspend fun getAllByHolding(holding: AssetHolding): List<AssetTransaction> =
        assetTransactionDao.getAllByHoldingId(holding.id).map { it.toDomain() }

    override suspend fun getAllByHoldingAndDateRange(
        holding: AssetHolding,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<AssetTransaction> =
        assetTransactionDao.getAllByHoldingIdAndDateRange(
            holdingId = holding.id,
            startDate = startDate,
            endDate = endDate
        ).map { it.toDomain() }

    override suspend fun getByGoalAndReferenceDate(
        goalId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ) =
        assetTransactionDao.getByGoalAndDateRange(
            goalId = goalId,
            startDate = startDate,
            endDate = endDate
        ).map { it.toDomain() }

    override suspend fun getByReferenceDate(startDate: LocalDate, endDate: LocalDate): List<AssetTransaction> =
        assetTransactionDao.getByDateRange(
            startDate = startDate,
            endDate = endDate
        ).map { it.toDomain() }

    override suspend fun delete(holdingId: Long, id: Long) {
        val existing = assetTransactionDao.find(id)
        if (existing?.holdingId == holdingId) {
            assetTransactionDao.deleteById(id)
        }
    }

    override suspend fun saveAll(entries: List<Pair<AssetHolding, AssetTransaction>>) {
        assetTransactionDao.saveAll(
            entries.map { (holding, transaction) -> transaction.toEntity(holding.id) },
        )
    }
}
