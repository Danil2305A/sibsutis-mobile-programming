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
        return if (player.id == 0L) {
            playerDao.insert(player)
        } else {
            val existing = playerDao.getById(player.id)
            if (existing != null) {
                playerDao.update(player.copy(createdAt = existing.createdAt))
                player.id
            } else {
                playerDao.insert(player)
            }
        }
    }

    suspend fun saveScore(score: ScoreEntity): Long {
        val existing = scoreDao.findByPlayer(score.playerId)
        return if (existing == null) {
            scoreDao.insert(score)
        } else {
            if (score.score > existing.score) {
                scoreDao.update(
                    score.copy(
                        id = existing.id,
                        playedAt = score.playedAt
                    )
                )
            }
            existing.id
        }
    }

    suspend fun clearScores() = scoreDao.clearAll()

    suspend fun deletePlayer(id: Long) = playerDao.deleteById(id)
}