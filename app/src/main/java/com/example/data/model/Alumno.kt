package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alumnos")
data class Alumno(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nie: String = "",
    val nombreCompleto: String,
    val primerApellido: String = "",
    val segundoApellido: String = "",
    val nombre: String = "",
    val curso: String = "",
    val unidad: String = "", // "6ºA", "4 años B", etc.
    val unidadNormalizada: String = "", // "6A", "Inf4B", etc.
    val telefonoUrgencia: String = "",
    val email: String = ""
)
