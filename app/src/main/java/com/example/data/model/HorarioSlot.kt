package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa una clase o tramo horario fijo en la semana del docente (FET / Horario).
 * diaSemana: 1 = Lunes, 2 = Martes, 3 = Miércoles, 4 = Jueves, 5 = Viernes
 */
@Entity(tableName = "horario_semanal")
data class HorarioSlot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val docente: String = "", // Nombre del docente en FET (ej. "Andrés", "Antonio", etc.)
    val diaSemana: Int, // 1=Lunes, 2=Martes, 3=Miércoles, 4=Jueves, 5=Viernes
    val horaInicio: String, // ej. "09:00"
    val horaFin: String, // ej. "10:00"
    val grupo: String, // ej. "2A" o "1º ESO A"
    val asignatura: String, // ej. "Mates" o "English"
    val aula: String = "", // ej. "4A" o "Aula 101"
    val colorHex: String = "#1E88E5"
) {
    val tramoHorario: String
        get() = if (horaFin.isNotBlank()) "$horaInicio - $horaFin" else horaInicio

    val etiquetaCompleta: String
        get() = when {
            asignatura.isNotBlank() && grupo.isNotBlank() -> "$grupo - $asignatura"
            grupo.isNotBlank() -> grupo
            asignatura.isNotBlank() -> asignatura
            else -> "Clase"
        }
}
