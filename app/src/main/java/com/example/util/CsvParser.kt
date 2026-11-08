package com.example.util

import com.example.data.model.Alumno
import com.example.data.model.CompaneroCen
import com.example.data.model.HorarioSlot
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object CsvParser {

    /**
     * Charset preferido para los CSVs de Séneca y FET: ISO-8859-15 (Europa Occidental Euro con tildes)
     */
    val CHARSET_SENECA: Charset = Charset.forName("ISO-8859-15")

    /**
     * Divide una línea CSV respetando comillas y caracteres especiales.
     */
    fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val cur = StringBuilder()
        var inQuotes = false
        var i = 0

        // Detección automática del delimitador: coma (,) o punto y coma (;) común en Excel/Séneca en español
        val delimiter = if (!line.contains(",") && line.contains(";")) ';' else ','

        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    cur.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == delimiter && !inQuotes) {
                result.add(cur.toString().trim())
                cur.clear()
            } else {
                cur.append(c)
            }
            i++
        }
        result.add(cur.toString().trim())
        return result.map { it.removeSurrounding("\"").trim() }
    }

    /**
     * Abre un BufferedReader con decodificación inteligente:
     * Comienza con ISO-8859-15 (con soporte de Euro, tildes y ñ).
     * Si no, permite Windows-1252 o UTF-8 sin corromper tildes.
     */
    fun crearReader(inputStream: InputStream): BufferedReader {
        val bytes = inputStream.readBytes()
        val charsetElegido = if (pareceUtf8Valido(bytes)) {
            StandardCharsets.UTF_8
        } else {
            CHARSET_SENECA
        }
        return BufferedReader(InputStreamReader(ByteArrayInputStream(bytes), charsetElegido))
    }

    private fun pareceUtf8Valido(bytes: ByteArray): Boolean {
        if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
            return true
        }
        var i = 0
        var sawHighBytes = false
        while (i < bytes.size && i < 2048) {
            val b = bytes[i].toInt() and 0xFF
            if (b >= 0x80) {
                sawHighBytes = true
                if (b in 0xC2..0xDF) {
                    if (i + 1 >= bytes.size || (bytes[i + 1].toInt() and 0xC0) != 0x80) return false
                    i += 2
                    continue
                } else if (b in 0xE0..0xEF) {
                    if (i + 2 >= bytes.size || (bytes[i + 1].toInt() and 0xC0) != 0x80 || (bytes[i + 2].toInt() and 0xC0) != 0x80) return false
                    i += 3
                    continue
                } else {
                    return false
                }
            }
            i++
        }
        return sawHighBytes
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // HORARIO FET (horario.csv)
    // ─────────────────────────────────────────────────────────────────────────────

    data class FetEntry(
        val dayCode: String, // "D1" .. "D5"
        val hourRange: String, // "09:00-09:30"
        val hourStart: String,
        val hourEnd: String,
        val group: String,
        val subject: String,
        val teachers: List<String>,
        val room: String
    )

    fun extractAllTeachers(bytes: ByteArray): List<String> {
        val teachersSet = mutableSetOf<String>()
        val reader = crearReader(ByteArrayInputStream(bytes))

        reader.useLines { lines ->
            lines.drop(1).forEach { line ->
                val tokens = parseCsvLine(line)
                if (tokens.size >= 6) {
                    val rawTeachers = tokens[5]
                    if (rawTeachers.isNotBlank()) {
                        rawTeachers.split("+", ",").forEach { t ->
                            val clean = t.trim()
                            if (clean.isNotBlank()) {
                                teachersSet.add(clean)
                            }
                        }
                    }
                }
            }
        }
        return teachersSet.sorted()
    }

    /**
     * Parsea el CSV de FET, filtra por el docente seleccionado y fusiona automáticamente
     * los tramos seguidos (de 30 min) de la misma clase en un único tramo consolidado.
     */
    fun parseFetHorario(bytes: ByteArray, docenteSeleccionado: String): List<HorarioSlot> {
        val entries = mutableListOf<FetEntry>()
        val reader = crearReader(ByteArrayInputStream(bytes))

        reader.useLines { lines ->
            lines.drop(1).forEach { line ->
                val tokens = parseCsvLine(line)
                if (tokens.size >= 6) {
                    val dayCode = tokens[1].trim()
                    val hourRange = tokens[2].trim()
                    val group = tokens[3].trim()
                    val subject = tokens[4].trim()
                    val rawTeachers = tokens[5].trim()
                    val room = if (tokens.size > 6) tokens[6].trim() else ""

                    if (dayCode.startsWith("D") && hourRange.contains("-")) {
                        val hours = hourRange.split("-")
                        val start = hours.getOrNull(0)?.trim() ?: ""
                        val end = hours.getOrNull(1)?.trim() ?: ""

                        val teachersList = rawTeachers.split("+", ",").map { it.trim() }.filter { it.isNotBlank() }

                        val coincide = docenteSeleccionado.isBlank() ||
                                teachersList.any { it.equals(docenteSeleccionado, ignoreCase = true) }

                        if (coincide && start.isNotBlank() && end.isNotBlank()) {
                            entries.add(
                                FetEntry(
                                    dayCode = dayCode,
                                    hourRange = hourRange,
                                    hourStart = start,
                                    hourEnd = end,
                                    group = group,
                                    subject = subject,
                                    teachers = teachersList,
                                    room = room
                                )
                            )
                        }
                    }
                }
            }
        }

        val diaOrder = mapOf("D1" to 1, "D2" to 2, "D3" to 3, "D4" to 4, "D5" to 5)
        val sortedEntries = entries.sortedWith(
            compareBy(
                { diaOrder[it.dayCode] ?: 99 },
                { it.hourStart }
            )
        )

        val mergedSlots = mutableListOf<HorarioSlot>()

        for (entry in sortedEntries) {
            val diaSemana = diaOrder[entry.dayCode] ?: continue
            val last = mergedSlots.lastOrNull()

            if (last != null &&
                last.diaSemana == diaSemana &&
                last.grupo.equals(entry.group, ignoreCase = true) &&
                last.asignatura.equals(entry.subject, ignoreCase = true) &&
                last.horaFin == entry.hourStart
            ) {
                mergedSlots[mergedSlots.lastIndex] = last.copy(
                    horaFin = entry.hourEnd,
                    aula = if (last.aula.isBlank()) entry.room else last.aula
                )
            } else {
                mergedSlots.add(
                    HorarioSlot(
                        docente = docenteSeleccionado,
                        diaSemana = diaSemana,
                        horaInicio = entry.hourStart,
                        horaFin = entry.hourEnd,
                        grupo = entry.group,
                        asignatura = entry.subject,
                        aula = entry.room
                    )
                )
            }
        }

        return mergedSlots
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // ALUMNADO SÉNECA (RegAlum.csv - Todo el centro Y RegMisAlu.csv - Mis Alumnos)
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Extrae rápidamente la lista de unidades únicas presentes en un CSV de Séneca.
     */
    fun extraerUnidadesDisponibles(bytes: ByteArray): List<String> {
        val unidades = mutableSetOf<String>()
        val reader = crearReader(ByteArrayInputStream(bytes))
        var idxUnidad = -1
        var cabeceraEncontrada = false

        reader.useLines { lines ->
            lines.forEach { line ->
                val tokens = parseCsvLine(line)
                if (!cabeceraEncontrada) {
                    tokens.forEachIndexed { index, rawHeader ->
                        val h = normalizarCabecera(rawHeader)
                        if (h == "unidad" || h.contains("unidad")) {
                            idxUnidad = index
                            cabeceraEncontrada = true
                        }
                    }
                } else {
                    if (tokens.isNotEmpty() && idxUnidad != -1 && idxUnidad < tokens.size) {
                        val u = tokens[idxUnidad].trim()
                        if (u.isNotBlank() && !u.equals("Unidad", ignoreCase = true)) {
                            unidades.add(u)
                        }
                    }
                }
            }
        }
        return unidades.sorted()
    }

    /**
     * Parsea Séneca admitiendo ambos formatos:
     * 1. RegAlum.csv (todo el centro): columna "Alumno/a" y "Unidad". Permite filtrar por lista de unidades elegidas.
     * 2. RegMisAlu.csv (mis alumnos): columna "Alumno" y "Cuenta Google/Microsoft".
     */
    fun parseSenecaAlumnos(bytes: ByteArray, unidadesFiltro: Set<String> = emptySet()): List<Alumno> {
        val alumnos = mutableListOf<Alumno>()
        val reader = crearReader(ByteArrayInputStream(bytes))

        var idxAlumno = -1
        var idxUnidad = -1
        var idxCurso = -1
        var idxNie = -1
        var idxCuentaGoogle = -1

        var cabeceraEncontrada = false

        reader.useLines { lines ->
            lines.forEach { line ->
                val tokens = parseCsvLine(line)
                if (!cabeceraEncontrada) {
                    tokens.forEachIndexed { index, rawHeader ->
                        val h = normalizarCabecera(rawHeader)
                        // Detectar columna Alumno/a o Alumno
                        if (idxAlumno == -1 && (h == "alumno/a" || h == "alumno" || h.startsWith("alumno/a") || h.startsWith("alumno"))) {
                            idxAlumno = index
                            cabeceraEncontrada = true
                        }
                        // Detectar columna Unidad
                        if (h == "unidad") {
                            idxUnidad = index
                        }
                        if (h == "curso") {
                            idxCurso = index
                        }
                        if (h.contains("id. escolar") || h.contains("nie") || h == "n id. escolar") {
                            idxNie = index
                        }
                        // Detectar Cuenta Google/Microsoft (en RegMisAlu.csv)
                        if (h.contains("cuenta google") || h.contains("cuenta google/microsoft")) {
                            idxCuentaGoogle = index
                        }
                    }
                } else {
                    if (tokens.isNotEmpty() && idxAlumno != -1 && idxAlumno < tokens.size) {
                        val nombreCompleto = tokens[idxAlumno].trim()
                        if (nombreCompleto.isNotBlank() &&
                            !nombreCompleto.equals("Alumno/a", ignoreCase = true) &&
                            !nombreCompleto.equals("Alumno", ignoreCase = true)
                        ) {
                            val unidad = if (idxUnidad != -1 && idxUnidad < tokens.size && tokens[idxUnidad].isNotBlank()) {
                                tokens[idxUnidad].trim()
                            } else "General"

                            // Si se especificó un filtro de unidades, ignorar las demás
                            if (unidadesFiltro.isNotEmpty() && !unidadesFiltro.contains(unidad)) {
                                return@forEach
                            }

                            val curso = if (idxCurso != -1 && idxCurso < tokens.size) tokens[idxCurso].trim() else ""
                            val nie = if (idxNie != -1 && idxNie < tokens.size) tokens[idxNie].trim() else ""
                            val cuentaGoogle = if (idxCuentaGoogle != -1 && idxCuentaGoogle < tokens.size) tokens[idxCuentaGoogle].trim() else ""

                            // Separar Apellidos, Nombre si viene en formato "Apellido1 Apellido2, Nombre"
                            val partes = nombreCompleto.split(",")
                            val apellidos = partes.getOrNull(0)?.trim() ?: ""
                            val nombrePila = partes.getOrNull(1)?.trim() ?: ""

                            val apellidosTokens = apellidos.split(" ").filter { it.isNotBlank() }
                            val primerApellido = apellidosTokens.getOrNull(0) ?: apellidos
                            val segundoApellido = apellidosTokens.drop(1).joinToString(" ")

                            alumnos.add(
                                Alumno(
                                    nie = nie,
                                    nombreCompleto = nombreCompleto,
                                    primerApellido = primerApellido,
                                    segundoApellido = segundoApellido,
                                    nombre = if (nombrePila.isNotBlank()) nombrePila else nombreCompleto,
                                    curso = curso,
                                    unidad = unidad,
                                    unidadNormalizada = normalizarUnidad(unidad),
                                    telefonoUrgencia = "",
                                    email = cuentaGoogle // Solo cuenta personal si existe en RegMisAlu.csv
                                )
                            )
                        }
                    }
                }
            }
        }
        return alumnos
    }

    private fun normalizarCabecera(h: String): String {
        return h.lowercase()
            .replace("º", "")
            .replace("ª", "")
            .trim()
    }

    fun normalizarUnidad(u: String): String {
        val raw = u.trim().uppercase()
        return when {
            raw.contains("3 AÑOS B") || raw.contains("INF3B") -> "Inf3B"
            raw.contains("3 AÑOS A") || raw.contains("INF3A") -> "Inf3A"
            raw.contains("4 AÑOS B") || raw.contains("INF4B") -> "Inf4B"
            raw.contains("4 AÑOS A") || raw.contains("INF4A") -> "Inf4A"
            raw.contains("5 AÑOS B") || raw.contains("INF5B") -> "Inf5B"
            raw.contains("5 AÑOS A") || raw.contains("INF5A") -> "Inf5A"
            raw.contains("6ºA") || raw.contains("6A") -> "6A"
            raw.contains("6ºB") || raw.contains("6B") -> "6B"
            raw.contains("5ºA") || raw.contains("5A") -> "5A"
            raw.contains("5ºB") || raw.contains("5B") -> "5B"
            raw.contains("4ºA") || raw.contains("4A") -> "4A"
            raw.contains("4ºB") || raw.contains("4B") -> "4B"
            raw.contains("3ºA") || raw.contains("3A") -> "3A"
            raw.contains("3ºB") || raw.contains("3B") -> "3B"
            raw.contains("2ºA") || raw.contains("2A") -> "2A"
            raw.contains("2ºB") || raw.contains("2B") -> "2B"
            raw.contains("1ºA") || raw.contains("1A") -> "1A"
            raw.contains("1ºB") || raw.contains("1B") -> "1B"
            else -> raw.replace("º", "").replace(" ", "").trim()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // RELACIÓN PERSONAL CENTRO (RelPerCen.csv)
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Parsea RelPerCen.csv buscando las columnas:
     * - "Empleado/a" (o Nombre) -> "Apellidos, Nombre"
     * - "Cuenta Google/Microsoft" (o Correo)
     * Ordena la lista alfabéticamente por Nombre de pila (lo que va tras la coma).
     */
    fun parseRelPerCen(bytes: ByteArray): List<CompaneroCen> {
        val lista = mutableListOf<CompaneroCen>()
        val reader = crearReader(ByteArrayInputStream(bytes))

        var idxEmpleado = -1
        var idxCuenta = -1
        var cabeceraEncontrada = false

        reader.useLines { lines ->
            lines.forEach { line ->
                val tokens = parseCsvLine(line)
                if (!cabeceraEncontrada) {
                    tokens.forEachIndexed { index, rawH ->
                        val h = normalizarCabecera(rawH)
                        if (h.contains("empleado") || h.contains("empleada") || h.contains("profesor") || h.contains("nombre")) {
                            idxEmpleado = index
                            cabeceraEncontrada = true
                        }
                        if (h.contains("cuenta google") || h.contains("cuenta") || h.contains("correo") || h.contains("email")) {
                            idxCuenta = index
                        }
                    }
                } else {
                    if (tokens.isNotEmpty()) {
                        val rawNombre = if (idxEmpleado != -1 && idxEmpleado < tokens.size) tokens[idxEmpleado].trim()
                        else tokens.getOrNull(1)?.trim() ?: ""

                        val rawEmail = if (idxCuenta != -1 && idxCuenta < tokens.size) tokens[idxCuenta].trim()
                        else tokens.getOrNull(0)?.trim() ?: ""

                        if (rawNombre.isNotBlank() && rawEmail.isNotBlank()) {
                            lista.add(
                                CompaneroCen(
                                    nombre = rawNombre,
                                    email = rawEmail
                                )
                            )
                        }
                    }
                }
            }
        }

        // Ordenar alfabéticamente por Nombre (lo que va tras la coma en "Apellidos, Nombre")
        return lista.sortedWith { c1, c2 ->
            val n1 = extraerNombreDePila(c1.nombre)
            val n2 = extraerNombreDePila(c2.nombre)
            n1.compareTo(n2, ignoreCase = true)
        }
    }

    fun extraerNombreDePila(nombreCompleto: String): String {
        val partes = nombreCompleto.split(",")
        return if (partes.size > 1) {
            partes[1].trim()
        } else {
            nombreCompleto.trim()
        }
    }
}
