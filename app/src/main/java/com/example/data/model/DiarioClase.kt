package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa la planificación real y diario de una sesión de clase en una fecha concreta.
 * fecha: formato ISO "yyyy-MM-dd"
 * llaveUnica: "yyyyMMdd_HHmm" para evitar duplicados al cargar el día (como en el script de Apps Script)
 */
@Entity(
    tableName = "diario_clases",
    indices = [
        Index(value = ["llaveUnica"], unique = true),
        Index(value = ["fecha"])
    ]
)
data class DiarioClase(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fecha: String, // "yyyy-MM-dd"
    val fechaVisible: String, // "dd/MM/yyyy"
    val hora: String, // "08:15" o "08:15 - 09:15"
    val grupo: String, // "2º ESO B - Matemáticas"
    val actividad: String = "", // Contenido / Lección / Ejercicios
    val observaciones: String = "", // Tarea / Incidencias / Materiales
    val completada: Boolean = false, // Marca de clase impartida
    val llaveUnica: String, // "20261106_0815"
    val orden: Int = 0
)
