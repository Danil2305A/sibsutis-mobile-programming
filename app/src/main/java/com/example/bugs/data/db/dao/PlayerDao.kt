package com.example.bugs.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bugs.data.db.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(player: PlayerEntity): Long

    @Query("SELECT * FROM players ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE id = :id")
    suspend fun getById(id: Long): PlayerEntity?

    @Query("SELECT * FROM players WHERE fullName = :name")
    suspend fun findByName(name: String): PlayerEntity?

    @Query("SELECT * FROM players ORDER BY id DESC")
    fun observeLast(): Flow<PlayerEntity?>
}