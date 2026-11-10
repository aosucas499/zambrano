package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "criterios_evaluacion")
data class CriterioEvaluacion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val area: String,           // ej. "Cono", "Lengua", "Matemáticas", "Inglés", "Francés", "Ed. Artística", "Ed. Física"
    val ciclo: String = "",     // ej. "1er Ciclo (1º-2º)"
    val curso: String,          // ej. "1º", "2º", "3º", "4º", "5º", "6º"
    val ceNumero: String = "",  // ej. "1", "2"
    val codigo: String,         // ej. "1.1.a", "2.1.b"
    val descripcion: String,
    val saberesBasicos: String = "",
    val competencia: String = "" // ej. "1", "2", "3" (primer dígito del código)
)
