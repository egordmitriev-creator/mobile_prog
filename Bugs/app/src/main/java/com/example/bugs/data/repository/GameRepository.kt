package com.example.bugs.data.repository

import com.example.bugs.data.AppDatabase
import com.example.bugs.data.dao.RecordWithUser
import com.example.bugs.data.entities.Record
import com.example.bugs.data.entities.User
import kotlinx.coroutines.flow.Flow

class GameRepository(private val database: AppDatabase) {

    // User operations
    suspend fun insertUser(user: User): Long {
        return database.userDao().insertUser(user)
    }

    fun getAllUsers(): Flow<List<User>> {
        return database.userDao().getAllUsers()
    }

    suspend fun getUserById(userId: Long): User? {
        return database.userDao().getUserById(userId)
    }

    // Record operations
    suspend fun insertRecord(record: Record): Long {
        return database.recordDao().insertRecord(record)
    }

    fun getTopRecords(): Flow<List<RecordWithUser>> {
        return database.recordDao().getRecordsWithUserInfo()
    }

    suspend fun getUserBestScore(userId: Long): Int? {
        return database.recordDao().getUserBestScore(userId)
    }
}