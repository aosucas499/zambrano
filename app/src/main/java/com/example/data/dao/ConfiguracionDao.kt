package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ConfiguracionApp

@Dao
interface ConfiguracionDao {
    @Query("SELECT valor FROM configuracion_app WHERE clave = :clave")
    suspend fun getValor(clave: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setValor(config: ConfiguracionApp)

    @Query("SELECT * FROM configuracion_app")
    suspend fun getAllConfig(): List<ConfiguracionApp>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(configs: List<ConfiguracionApp>)

    @Query("DELETE FROM configuracion_app")
    suspend fun deleteAll()
}
