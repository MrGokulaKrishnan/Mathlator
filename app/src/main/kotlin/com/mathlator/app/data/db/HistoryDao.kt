package com.mathlator.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history ORDER BY createdAt DESC LIMIT 500")
    fun observeAll(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HistoryEntity)

    @Delete
    suspend fun delete(entry: HistoryEntity)

    @Query("DELETE FROM history")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM history")
    suspend fun count(): Int

    /** Prune oldest entries, keeping only the latest [limit]. */
    @Query(
        "DELETE FROM history WHERE id NOT IN " +
        "(SELECT id FROM history ORDER BY createdAt DESC LIMIT :limit)"
    )
    suspend fun pruneOldest(limit: Int = 500)
}
