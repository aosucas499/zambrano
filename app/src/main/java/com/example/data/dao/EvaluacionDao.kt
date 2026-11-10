package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ActividadEvaluacion
import com.example.data.model.CriterioEvaluacion
import com.example.data.model.NotaEvaluacion
import kotlinx.coroutines.flow.Flow

@Dao
interface EvaluacionDao {

    // --- Criterios ---
    @Query("SELECT * FROM criterios_evaluacion")
    fun getAllCriterios(): Flow<List<CriterioEvaluacion>>

    @Query("SELECT * FROM criterios_evaluacion WHERE curso = :curso ORDER BY codigo ASC")
    fun getCriteriosPorCurso(curso: String): Flow<List<CriterioEvaluacion>>

    @Query("SELECT COUNT(*) FROM criterios_evaluacion")
    suspend fun countCriterios(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCriterios(criterios: List<CriterioEvaluacion>)

    // --- Actividades ---
    @Query("SELECT * FROM actividades_evaluacion ORDER BY fechaCreacion ASC")
    fun getAllActividades(): Flow<List<ActividadEvaluacion>>

    @Query("SELECT * FROM actividades_evaluacion WHERE area = :area AND curso = :curso AND grupo = :grupo AND unidadSda = :unidadSda ORDER BY fechaCreacion ASC")
    fun getActividades(area: String, curso: String, grupo: String, unidadSda: String): Flow<List<ActividadEvaluacion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActividad(actividad: ActividadEvaluacion): Long

    @Update
    suspend fun updateActividad(actividad: ActividadEvaluacion)

    @Query("DELETE FROM actividades_evaluacion WHERE id = :actividadId")
    suspend fun deleteActividad(actividadId: Long)

    // --- Notas ---
    @Query("SELECT * FROM notas_evaluacion")
    fun getAllNotas(): Flow<List<NotaEvaluacion>>

    @Query("SELECT * FROM notas_evaluacion WHERE area = :area AND curso = :curso AND grupo = :grupo")
    fun getNotasPorClase(area: String, curso: String, grupo: String): Flow<List<NotaEvaluacion>>

    @Query("SELECT * FROM notas_evaluacion WHERE area = :area AND curso = :curso AND grupo = :grupo AND unidadSda = :unidadSda")
    fun getNotasPorSda(area: String, curso: String, grupo: String, unidadSda: String): Flow<List<NotaEvaluacion>>

    @Query("SELECT * FROM notas_evaluacion WHERE actividadId = :actividadId")
    fun getNotasPorActividad(actividadId: Long): Flow<List<NotaEvaluacion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateNotas(notas: List<NotaEvaluacion>)

    @Query("DELETE FROM notas_evaluacion WHERE actividadId = :actividadId")
    suspend fun deleteNotasPorActividad(actividadId: Long)

    @Query("SELECT * FROM actividades_evaluacion")
    suspend fun getAllActividadesDirecto(): List<ActividadEvaluacion>

    @Query("SELECT * FROM notas_evaluacion")
    suspend fun getAllNotasDirecto(): List<NotaEvaluacion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActividades(actividades: List<ActividadEvaluacion>)

    @Query("DELETE FROM actividades_evaluacion")
    suspend fun deleteAllActividades()

    @Query("DELETE FROM notas_evaluacion")
    suspend fun deleteAllNotas()

    @Query("DELETE FROM notas_evaluacion WHERE alumnoNombre = :alumnoNombre AND actividadId = :actividadId AND criterioCodigo = :criterioCodigo")
    suspend fun deleteNota(alumnoNombre: String, actividadId: Long, criterioCodigo: String)
}
