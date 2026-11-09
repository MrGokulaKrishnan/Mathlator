package com.mathlator.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RateDao {

    @Query("SELECT * FROM rate_snapshot WHERE base = :base")
    fun observeRates(base: String): Flow<List<RateSnapshotEntity>>

    @Query("SELECT * FROM rate_snapshot WHERE base = :base")
    suspend fun getRates(base: String): List<RateSnapshotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rates: List<RateSnapshotEntity>)

    @Query("DELETE FROM rate_snapshot WHERE base = :base")
    suspend fun deleteByBase(base: String)

    @Query("DELETE FROM rate_snapshot")
    suspend fun deleteAll()

    @Query("SELECT MIN(fetchedAt) FROM rate_snapshot WHERE base = :base")
    suspend fun oldestFetchedAt(base: String): Long?
}
