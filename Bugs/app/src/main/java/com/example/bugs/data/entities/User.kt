package com.example.bugs.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val gender: String,
    val course: String,
    val difficultyLevel: Int,
    val birthDate: Long,
    val zodiacSign: String,
    val createdAt: Long = System.currentTimeMillis()
)