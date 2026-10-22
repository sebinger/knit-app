package com.stitchtrack.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "counter_changes",
    foreignKeys = [
        ForeignKey(
            entity = CounterEntity::class,
            parentColumns = ["id"],
            childColumns = ["counterId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("counterId")],
)
data class CounterChangeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val counterId: Long,
    val delta: Int,
    val createdAt: Long,
)
