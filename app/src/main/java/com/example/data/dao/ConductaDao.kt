package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.RegistroConducta
import kotlinx.coroutines.flow.Flow

@Dao
interface ConductaDao {
    @Query("SELECT * FROM registros_conducta WHERE fecha = :fecha ORDER BY timestamp DESC")
    fun getRegistrosPorFecha(fecha: String): Flow<List<RegistroConducta>>

    @Query("SELECT * FROM registros_conducta WHERE alumnoId = :alumnoId ORDER BY timestamp DESC")
    fun getRegistrosPorAlumno(alumnoId: Long): Flow<List<RegistroConducta>>

    @Query("SELECT * FROM registros_conducta WHERE alumnoId = :alumnoId AND fecha = :fecha ORDER BY timestamp DESC")
    fun getRegistrosPorAlumnoYFecha(alumnoId: Long, fecha: String): Flow<List<RegistroConducta>>

    @Query("SELECT COUNT(*) FROM registros_conducta WHERE alumnoId = :alumnoId AND fecha = :fecha AND tipo = 'POSITIVO'")
    fun getPositivosHoy(alumnoId: Long, fecha: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM registros_conducta WHERE alumnoId = :alumnoId AND fecha = :fecha AND tipo = 'NEGATIVO'")
    fun getNegativosHoy(alumnoId: Long, fecha: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM registros_conducta WHERE alumnoId = :alumnoId AND tipo = 'POSITIVO'")
    fun getPositivosTotal(alumnoId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM registros_conducta WHERE alumnoId = :alumnoId AND tipo = 'NEGATIVO'")
    fun getNegativosTotal(alumnoId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM registros_conducta WHERE tipo = 'POSITIVO'")
    fun getTotalPositivosGlobal(): Flow<Int>

    @Query("SELECT COUNT(*) FROM registros_conducta WHERE tipo = 'NEGATIVO'")
    fun getTotalNegativosGlobal(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistro(registro: RegistroConducta): Long

    @Delete
    suspend fun deleteRegistro(registro: RegistroConducta)

    @Query("DELETE FROM registros_conducta WHERE id = (SELECT id FROM registros_conducta WHERE alumnoId = :alumnoId AND fecha = :fecha AND tipo = :tipo ORDER BY timestamp DESC LIMIT 1)")
    suspend fun restarUltimoRegistro(alumnoId: Long, fecha: String, tipo: String)

    @Query("SELECT * FROM registros_conducta")
    suspend fun getAllDirecto(): List<RegistroConducta>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(registros: List<RegistroConducta>)

    @Query("DELETE FROM registros_conducta")
    suspend fun deleteAll()
}
