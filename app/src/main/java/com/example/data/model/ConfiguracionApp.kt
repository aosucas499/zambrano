package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configuracion_app")
data class ConfiguracionApp(
    @PrimaryKey
    val clave: String,
    val valor: String
)
