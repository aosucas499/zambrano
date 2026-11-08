package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.HorarioSlot
import kotlinx.coroutines.flow.Flow

@Dao
interface HorarioDao {
    @Query("SELECT * FROM horario_semanal ORDER BY diaSemana ASC, horaInicio ASC")
    fun getAllSlots(): Flow<List<HorarioSlot>>

    @Query("SELECT * FROM horario_semanal WHERE (docente = :docente OR :docente = '') ORDER BY diaSemana ASC, horaInicio ASC")
    fun getSlotsPorDocente(docente: String): Flow<List<HorarioSlot>>

    @Query("SELECT * FROM horario_semanal WHERE diaSemana = :dia AND (docente = :docente OR :docente = '') ORDER BY horaInicio ASC")
    fun getSlotsPorDocenteYDia(docente: String, dia: Int): Flow<List<HorarioSlot>>

    @Query("SELECT * FROM horario_semanal WHERE diaSemana = :dia AND (docente = :docente OR :docente = '') ORDER BY horaInicio ASC")
    suspend fun getSlotsPorDocenteYDiaDirecto(docente: String, dia: Int): List<HorarioSlot>

    @Query("SELECT DISTINCT docente FROM horario_semanal WHERE docente != '' ORDER BY docente ASC")
    fun getAllDocentes(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM horario_semanal")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: HorarioSlot): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(slots: List<HorarioSlot>)

    @Update
    suspend fun updateSlot(slot: HorarioSlot)

    @Delete
    suspend fun deleteSlot(slot: HorarioSlot)

    @Query("DELETE FROM horario_semanal WHERE id = :id")
    suspend fun deleteSlotById(id: Long)

    @Query("DELETE FROM horario_semanal")
    suspend fun deleteAll()
}
