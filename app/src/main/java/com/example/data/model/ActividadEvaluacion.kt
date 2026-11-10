package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "actividades_evaluacion")
data class ActividadEvaluacion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val area: String,
    val curso: String,
    val grupo: String,
    val unidadSda: String, // "SA1", "SA2", ..., "SA12"
    val criteriosCodigosJson: String, // JSON lista con los códigos de los criterios ej: ["1.1.a", "1.2.a"]
    val fechaCreacion: Long = System.currentTimeMillis()
)
