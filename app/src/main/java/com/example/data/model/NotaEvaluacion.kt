package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notas_evaluacion")
data class NotaEvaluacion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val alumnoNombre: String,
    val actividadId: Long,
    val area: String,
    val curso: String,
    val grupo: String,
    val unidadSda: String,
    val criterioCodigo: String,
    val valor: Double? = null // null si aún no se ha calificado
)
