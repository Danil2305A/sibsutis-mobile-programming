package com.example.bugs.data.repository

import com.example.bugs.data.db.dao.PlayerDao
import com.example.bugs.data.db.dao.RecordRow
import com.example.bugs.data.db.dao.ScoreDao
import com.example.bugs.data.db.entity.PlayerEntity
import com.example.bugs.data.db.entity.ScoreEntity
import com.example.bugs.data.model.DifficultyPreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class PlayerRepositoryTest {

    private class FakePlayerDao : PlayerDao {
        val players = linkedMapOf<Long, PlayerEntity>()
        private var nextId = 1L
        override suspend fun insert(player: PlayerEntity): Long {
            val id = if (player.id == 0L) nextId++ else player.id
            players[id] = player.copy(id = id)
            return id
        }
        override fun observeAll(): Flow<List<PlayerEntity>> = flowOf(players.values.toList())
        override suspend fun getById(id: Long): PlayerEntity? = players[id]
        override suspend fun findByName(name: String): PlayerEntity? =
            players.values.firstOrNull { it.fullName == name }
        override fun observeLast(): Flow<PlayerEntity?> = flowOf(players.values.lastOrNull())
        override suspend fun update(player: PlayerEntity) { players[player.id] = player }
        override suspend fun deleteById(id: Long) { players.remove(id) }
    }

    private class FakeScoreDao : ScoreDao {
        val scores = linkedMapOf<Long, ScoreEntity>()
        private var nextId = 1L
        override suspend fun insert(score: ScoreEntity): Long {
            val id = if (score.id == 0L) nextId++ else score.id
            scores[id] = score.copy(id = id)
            return id
        }
        override fun observeRecords(): Flow<List<RecordRow>> = flowOf(emptyList())
        override fun observeByPlayer(playerId: Long): Flow<List<ScoreEntity>> =
            flowOf(scores.values.filter { it.playerId == playerId })
        override suspend fun clearAll() { scores.clear() }
        override suspend fun findByPlayer(playerId: Long): ScoreEntity? =
            scores.values.filter { it.playerId == playerId }.maxByOrNull { it.score }
        override suspend fun findBestForPlayerAndDifficulty(
            playerId: Long,
            preset: DifficultyPreset
        ): ScoreEntity? = scores.values
            .filter { it.playerId == playerId && it.difficulty == preset }
            .maxByOrNull { it.score }
        override fun observeRecordsByDifficulty(preset: DifficultyPreset): Flow<List<RecordRow>> =
            flowOf(emptyList())
        override suspend fun update(score: ScoreEntity) { scores[score.id] = score }
    }

    private fun player(name: String = "Иванов Иван", id: Long = 0L) = PlayerEntity(
        id = id, fullName = name, gender = "Мужской", course = 1,
        difficulty = DifficultyPreset.MEDIUM,
        birthDateMillis = 0L, zodiac = "Овен", zodiacResId = 0
    )

    private fun score(playerId: Long, score: Int, preset: DifficultyPreset = DifficultyPreset.MEDIUM) =
        ScoreEntity(
            id = 0L, playerId = playerId, score = score,
            hits = 10, misses = 0,
            difficulty = preset, settingsSpeed = 5
        )

    @Test
    fun upsertInsertWhenIdZero() = runTest {
        val dao = FakePlayerDao()
        val repo = PlayerRepository(dao, FakeScoreDao())
        val id = repo.upsertPlayer(player(id = 0L))
        assertNotEquals(0L, id)
        assertEquals(1, dao.players.size)
    }

    @Test
    fun upsertRestoresCreatedAt() = runTest {
        val dao = FakePlayerDao()
        val repo = PlayerRepository(dao, FakeScoreDao())
        val id = repo.upsertPlayer(player(name = "A"))
        val originalCreatedAt = dao.players[id]!!.createdAt

        repo.upsertPlayer(player(name = "B", id = id).copy(createdAt = 0L))

        assertEquals("B", dao.players[id]!!.fullName)
        assertEquals(originalCreatedAt, dao.players[id]!!.createdAt)
    }

    @Test
    fun saveScoreInsertsWhenNone() = runTest {
        val pDao = FakePlayerDao(); val sDao = FakeScoreDao()
        val repo = PlayerRepository(pDao, sDao)
        val pid = repo.upsertPlayer(player())

        repo.saveScore(score(pid, 100))
        assertEquals(1, sDao.scores.size)
        assertEquals(100, sDao.scores.values.first().score)
    }

    @Test
    fun saveScoreOnlyOverwritesStrictlyHigher() = runTest {
        val pDao = FakePlayerDao(); val sDao = FakeScoreDao()
        val repo = PlayerRepository(pDao, sDao)
        val pid = repo.upsertPlayer(player())

        repo.saveScore(score(pid, 100))
        repo.saveScore(score(pid, 200))
        repo.saveScore(score(pid, 50))
        repo.saveScore(score(pid, 200))

        assertEquals(1, sDao.scores.size)
        assertEquals(200, sDao.scores.values.first().score)
    }

    @Test
    fun saveScoreDifferentDifficultySeparateRecords() = runTest {
        val pDao = FakePlayerDao(); val sDao = FakeScoreDao()
        val repo = PlayerRepository(pDao, sDao)
        val pid = repo.upsertPlayer(player())

        repo.saveScore(score(pid, 100))
        repo.saveScore(score(pid, 50, DifficultyPreset.HARD))

        assertEquals(2, sDao.scores.size)
    }

    @Test
    fun deletePlayerDelegates() = runTest {
        val pDao = FakePlayerDao()
        val repo = PlayerRepository(pDao, FakeScoreDao())
        val id = repo.upsertPlayer(player())

        repo.deletePlayer(id)
        assertEquals(0, pDao.players.size)
    }
}