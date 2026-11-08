package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Alumno
import kotlinx.coroutines.flow.Flow

@Dao
interface AlumnoDao {
    @Query("SELECT * FROM alumnos ORDER BY unidad ASC, primerApellido ASC, segundoApellido ASC, nombre ASC")
    fun getAllAlumnos(): Flow<List<Alumno>>

    @Query("SELECT * FROM alumnos WHERE unidad = :unidad ORDER BY primerApellido ASC, segundoApellido ASC, nombre ASC")
    fun getAlumnosPorUnidad(unidad: String): Flow<List<Alumno>>

    @Query("SELECT * FROM alumnos WHERE unidadNormalizada = :norm OR unidad = :norm ORDER BY primerApellido ASC, segundoApellido ASC, nombre ASC")
    fun getAlumnosPorUnidadNormalizada(norm: String): Flow<List<Alumno>>

    @Query("SELECT DISTINCT unidad FROM alumnos WHERE unidad != '' ORDER BY unidad ASC")
    fun getUnidades(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM alumnos")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(alumnos: List<Alumno>)

    @Query("DELETE FROM alumnos")
    suspend fun deleteAll()
}
