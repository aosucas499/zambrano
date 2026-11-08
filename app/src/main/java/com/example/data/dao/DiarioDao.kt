package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DiarioClase
import kotlinx.coroutines.flow.Flow

@Dao
interface DiarioDao {
    @Query("SELECT * FROM diario_clases WHERE fecha = :fecha ORDER BY hora ASC, id ASC")
    fun getClasesPorFecha(fecha: String): Flow<List<DiarioClase>>

    @Query("SELECT * FROM diario_clases WHERE fecha = :fecha ORDER BY hora ASC, id ASC")
    suspend fun getClasesPorFechaDirecto(fecha: String): List<DiarioClase>

    @Query("SELECT llaveUnica FROM diario_clases WHERE fecha = :fecha")
    suspend fun getLlavesPorFecha(fecha: String): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(clases: List<DiarioClase>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOActualizar(clase: DiarioClase): Long

    @Update
    suspend fun updateClase(clase: DiarioClase)

    @Query("UPDATE diario_clases SET actividad = :actividad, observaciones = :observaciones WHERE id = :id")
    suspend fun updateActividadYObservaciones(id: Long, actividad: String, observaciones: String)

    @Query("UPDATE diario_clases SET completada = :completada WHERE id = :id")
    suspend fun toggleCompletada(id: Long, completada: Boolean)

    @Delete
    suspend fun deleteClase(clase: DiarioClase)

    @Query("SELECT * FROM diario_clases ORDER BY fecha DESC, hora ASC LIMIT 100")
    fun getUltimasClases(): Flow<List<DiarioClase>>

    @Query("SELECT COUNT(*) FROM diario_clases WHERE fecha = :fecha")
    suspend fun getConteoPorFecha(fecha: String): Int

    @Query("SELECT COUNT(*) FROM diario_clases WHERE completada = 1")
    fun getConteoCompletadas(): Flow<Int>

    @Query("SELECT COUNT(*) FROM diario_clases")
    fun getConteoTotal(): Flow<Int>

    @Query("DELETE FROM diario_clases")
    suspend fun deleteAll()
}
