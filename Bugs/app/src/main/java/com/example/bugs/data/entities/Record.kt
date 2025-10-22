package com.example.bugs.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "records",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Record(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val score: Int,
    val difficultyLevel: Int,
    val gameDuration: Int,
    val date: Long = System.currentTimeMillis()
)