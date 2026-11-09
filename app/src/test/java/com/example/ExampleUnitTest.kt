package com.example

import com.example.data.FictionalDataSeeder
import com.example.data.model.DiarioClase
import com.example.util.CsvParser
import com.example.util.DateUtils
import com.example.util.ShareUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.Charset
import java.time.LocalDate

class ExampleUnitTest {

    @Test
    fun testDateUtils() {
        val lunes = LocalDate.of(2026, 11, 2)
        val sabado = LocalDate.of(2026, 11, 7)

        assertEquals(false, DateUtils.esFinDeSemana(lunes))
        assertEquals(true, DateUtils.esFinDeSemana(sabado))
        assertEquals("Lunes", DateUtils.nombreDiaSemana(1))
        assertEquals("Viernes", DateUtils.nombreDiaSemana(5))
    }

    @Test
    fun testWhatsappFormatting() {
        val clases = listOf(
            DiarioClase(
                id = 1,
                fecha = "2026-11-06",
                fechaVisible = "06/11/2026",
                hora = "08:15 - 09:15",
                grupo = "3º Literatura Infantil - Cuentos",
                actividad = "Tema 3: El Principito y el Zorro",
                observaciones = "Revisar cuaderno",
                completada = true,
                llaveUnica = "20261106_0815"
            )
        )
        val texto = ShareUtils.formatearTextoWhatsApp("06/11/2026", clases)
        assertTrue(texto.contains("PROGRAMACIÓN DOCENTE - 06/11/2026"))
        assertTrue(texto.contains("3º Literatura Infantil"))
        assertTrue(texto.contains("El Principito y el Zorro"))
        assertTrue(texto.contains("Cuaderno de Zambrano"))
    }

    @Test
    fun testFictionalDataSeeder() {
        val alumnos = FictionalDataSeeder.getAlumnosEjemplo()
        assertTrue(alumnos.isNotEmpty())
        assertTrue(alumnos.any { it.nombreCompleto.contains("Principito") })
        assertTrue(alumnos.any { it.nombreCompleto.contains("Matilda") })

        val claustro = FictionalDataSeeder.getClaustroEjemplo()
        assertTrue(claustro.isNotEmpty())
        assertTrue(claustro.any { it.nombre.contains("Cervantes") })
        assertTrue(claustro.any { it.nombre.contains("Curie") })
    }

    @Test
    fun testCsvParserSenecaIso885915() {
        val csvContenido = """
            "Nº Id. escolar","Alumno/a","Curso","Unidad"
            "12345","García Peláez, Inés","3º Ed. Primaria","3ºA"
            "67890","Núñez del Río, Álvaro","3º Ed. Primaria","3ºA"
            "11111","Romero Soto, Lucía","4º Ed. Primaria","4ºB"
        """.trimIndent()

        val charsetIso = Charset.forName("ISO-8859-15")
        val bytes = csvContenido.toByteArray(charsetIso)

        // Prueba 1: Extraer unidades disponibles
        val unidades = CsvParser.extraerUnidadesDisponibles(bytes)
        assertEquals(2, unidades.size)
        assertTrue(unidades.contains("3ºA"))
        assertTrue(unidades.contains("4ºB"))

        // Prueba 2: Filtrar sólo por unidad "3ºA"
        val alumnosFiltrados = CsvParser.parseSenecaAlumnos(bytes, setOf("3ºA"))
        assertEquals(2, alumnosFiltrados.size)
        assertEquals("García Peláez, Inés", alumnosFiltrados[0].nombreCompleto)
        assertEquals("3ºA", alumnosFiltrados[0].unidad)
        assertEquals("", alumnosFiltrados[0].email) // Nunca emails de tutores

        // Prueba 3: Sin filtro (todas las unidades)
        val todosAlumnos = CsvParser.parseSenecaAlumnos(bytes, emptySet())
        assertEquals(3, todosAlumnos.size)
    }

    @Test
    fun testCsvParserMisAlumnosSeneca() {
        // Formato RegMisAlu.csv (columna Alumno y Cuenta Google/Microsoft)
        val csvContenido = """
            "Alumno","Cuenta Google/Microsoft"
            "Panza, Sancho","spanza@ejemplo.edu"
            "Quijano, Alonso","aquijano@ejemplo.edu"
        """.trimIndent()

        val bytes = csvContenido.toByteArray(Charset.forName("ISO-8859-15"))
        val alumnos = CsvParser.parseSenecaAlumnos(bytes)

        assertEquals(2, alumnos.size)
        assertEquals("Panza, Sancho", alumnos[0].nombreCompleto)
        assertEquals("spanza@ejemplo.edu", alumnos[0].email)
    }

    @Test
    fun testCsvParserClaustroOrdenadoPorNombre() {
        val csvContenido = """
            "Cuenta Google/Microsoft","Empleado/a"
            "cervantes@ejemplo.edu","Cervantes, Miguel de"
            "curie@ejemplo.edu","Curie, Marie"
            "aristoteles@ejemplo.edu","Estagira, Aristóteles de"
        """.trimIndent()

        val bytes = csvContenido.toByteArray(Charset.forName("ISO-8859-15"))
        val claustro = CsvParser.parseRelPerCen(bytes)

        assertEquals(3, claustro.size)
        // Aristóteles (A) antes que Marie (M), y este antes que Miguel (M)
        assertEquals("Estagira, Aristóteles de", claustro[0].nombre)
        assertEquals("Curie, Marie", claustro[1].nombre)
        assertEquals("Cervantes, Miguel de", claustro[2].nombre)
    }
}
