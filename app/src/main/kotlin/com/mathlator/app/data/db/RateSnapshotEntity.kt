package com.mathlator.app.data.db

import androidx.room.Entity

@Entity(
    tableName = "rate_snapshot",
    primaryKeys = ["base", "code"],
)
data class RateSnapshotEntity(
    val base: String,
    val code: String,
    val rate: String,          // BigDecimal stored as string
    val fetchedAt: Long,
)
