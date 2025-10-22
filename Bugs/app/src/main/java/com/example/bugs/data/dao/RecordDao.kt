package com.example.bugs.data.dao

import androidx.room.*
import com.example.bugs.data.entities.Record
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Query("SELECT * FROM records ORDER BY score DESC LIMIT 50")
    fun getTopRecords(): Flow<List<Record>>

    @Query("SELECT * FROM records WHERE userId = :userId ORDER BY date DESC")
    fun getRecordsByUser(userId: Long): Flow<List<Record>>

    @Insert
    suspend fun insertRecord(record: Record): Long

    @Query("SELECT MAX(score) FROM records WHERE userId = :userId")
    suspend fun getUserBestScore(userId: Long): Int?

    @Query("""
        SELECT r.*, u.fullName, u.zodiacSign 
        FROM records r 
        INNER JOIN users u ON r.userId = u.id 
        ORDER BY r.score DESC 
        LIMIT 20
    """)
    fun getRecordsWithUserInfo(): Flow<List<RecordWithUser>>
}

data class RecordWithUser(
    val id: Long,
    val userId: Long,
    val score: Int,
    val difficultyLevel: Int,
    val gameDuration: Int,
    val date: Long,
    val fullName: String,
    val zodiacSign: String
)