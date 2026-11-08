package com.example.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object DateUtils {
    private val localeEs = Locale("es", "ES")
    val isoFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    val displayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", localeEs)

    fun formatoFechaLarga(date: LocalDate): String {
        val diaSemana = date.dayOfWeek.getDisplayName(TextStyle.FULL, localeEs)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeEs) else it.toString() }
        val dia = date.dayOfMonth
        val mes = date.month.getDisplayName(TextStyle.FULL, localeEs)
        val anio = date.year
        return "$diaSemana, $dia de $mes de $anio"
    }

    fun nombreDiaSemana(diaNumero: Int): String {
        return when (diaNumero) {
            1 -> "Lunes"
            2 -> "Martes"
            3 -> "Miércoles"
            4 -> "Jueves"
            5 -> "Viernes"
            6 -> "Sábado"
            7 -> "Domingo"
            else -> "Día $diaNumero"
        }
    }

    fun esFinDeSemana(date: LocalDate): Boolean {
        return date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
    }

    fun siguienteDiaLectivo(date: LocalDate): LocalDate {
        var prox = date.plusDays(1)
        while (esFinDeSemana(prox)) {
            prox = prox.plusDays(1)
        }
        return prox
    }

    fun anteriorDiaLectivo(date: LocalDate): LocalDate {
        var ant = date.minusDays(1)
        while (esFinDeSemana(ant)) {
            ant = ant.minusDays(1)
        }
        return ant
    }
}
