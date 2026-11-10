package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CompaneroCen
import kotlinx.coroutines.flow.Flow

@Dao
interface CompaneroDao {
    @Query("SELECT * FROM companeros_cen ORDER BY nombre ASC")
    fun getAllCompaneros(): Flow<List<CompaneroCen>>

    @Query("SELECT COUNT(*) FROM companeros_cen")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(companeros: List<CompaneroCen>)

    @Query("SELECT * FROM companeros_cen")
    suspend fun getAllDirecto(): List<CompaneroCen>

    @Query("DELETE FROM companeros_cen")
    suspend fun deleteAll()
}
