package com.example.bugs.data.repository

import com.example.bugs.data.db.dao.PlayerDao
import com.example.bugs.data.db.dao.RecordRow
import com.example.bugs.data.db.dao.ScoreDao
import com.example.bugs.data.db.entity.PlayerEntity
import com.example.bugs.data.db.entity.ScoreEntity
import kotlinx.coroutines.flow.Flow

class PlayerRepository(
    private val playerDao: PlayerDao,
    private val scoreDao: ScoreDao
) {
    fun observePlayers(): Flow<List<PlayerEntity>> = playerDao.observeAll()
    fun observeLastPlayer(): Flow<PlayerEntity?> = playerDao.observeLast()
    fun observeRecords(): Flow<List<RecordRow>> = scoreDao.observeRecords()
    fun observeScoresFor(playerId: Long): Flow<List<ScoreEntity>> =
        scoreDao.observeByPlayer(playerId)

    suspend fun getById(id: Long): PlayerEntity? = playerDao.getById(id)
    suspend fun findByName(name: String): PlayerEntity? = playerDao.findByName(name)

    suspend fun upsertPlayer(player: PlayerEntity): Long {
        val existing = playerDao.findByName(player.fullName)
        return if (existing == null) {
            playerDao.insert(player)
        } else {
            val updated = player.copy(id = existing.id, createdAt = existing.createdAt)
            playerDao.insert(updated)
            existing.id
        }
    }

    suspend fun saveScore(score: ScoreEntity): Long = scoreDao.insert(score)
}