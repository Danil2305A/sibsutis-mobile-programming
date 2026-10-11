package com.example.bugs.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.bugs.data.db.entity.ScoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Insert
    suspend fun insert(score: ScoreEntity): Long

    @Query("""
        SELECT s.id AS id,
               s.score AS score,
               s.hits AS hits,
               s.misses AS misses,
               s.difficulty AS difficulty,
               s.settingsSpeed AS settingsSpeed,
               s.playedAt AS playedAt,
               p.id as playerId,
               p.fullName AS fullName,
               p.zodiac AS zodiac,
               p.zodiacResId AS zodiacResId,
               p.course AS course
        FROM scores s
        JOIN players p ON p.id = s.playerId
        ORDER BY s.score DESC, s.playedAt DESC
    """)
    fun observeRecords(): Flow<List<RecordRow>>

    @Query("SELECT * FROM scores WHERE playerId = :playerId ORDER BY playedAt DESC")
    fun observeByPlayer(playerId: Long): Flow<List<ScoreEntity>>

    @Query("DELETE FROM scores")
    suspend fun clearAll()

    @Query( "SELECT * " +
                    "FROM scores " +
                    "WHERE playerId = :playerId " +
                    "ORDER BY score DESC " +
                    "LIMIT 1")
    suspend fun findByPlayer(playerId: Long): ScoreEntity?

    @Update
    suspend fun update(score: ScoreEntity)
}

data class RecordRow(
    val id: Long,
    val score: Int,
    val hits: Int,
    val misses: Int,
    val difficulty: Int,
    val settingsSpeed: Int,
    val playedAt: Long,
    val playerId: Long,
    val fullName: String,
    val zodiac: String,
    val zodiacResId: Int,
    val course: Int
)