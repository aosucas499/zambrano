package com.example.data

import android.content.Context
import com.example.data.dao.AlumnoDao
import com.example.data.dao.CompaneroDao
import com.example.data.dao.ConductaDao
import com.example.data.dao.ConfiguracionDao
import com.example.data.dao.DiarioDao
import com.example.data.dao.HorarioDao
import com.example.data.model.Alumno
import com.example.data.model.CompaneroCen
import com.example.data.model.ConfiguracionApp
import com.example.data.model.DiarioClase
import com.example.data.model.HorarioSlot
import com.example.data.model.RegistroConducta
import com.example.util.CsvParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class DocenteRepository(
    private val horarioDao: HorarioDao,
    private val diarioDao: DiarioDao,
    private val alumnoDao: AlumnoDao,
    private val conductaDao: ConductaDao,
    private val companeroDao: CompaneroDao,
    private val configuracionDao: ConfiguracionDao
) {
    val todosLosSlots: Flow<List<HorarioSlot>> = horarioDao.getAllSlots()
    val ultimasClases: Flow<List<DiarioClase>> = diarioDao.getUltimasClases()
    val totalCompletadas: Flow<Int> = diarioDao.getConteoCompletadas()
    val totalClases: Flow<Int> = diarioDao.getConteoTotal()

    val todosLosAlumnos: Flow<List<Alumno>> = alumnoDao.getAllAlumnos()
    val todasLasUnidades: Flow<List<String>> = alumnoDao.getUnidades()
    val todosLosCompaneros: Flow<List<CompaneroCen>> = companeroDao.getAllCompaneros()
    val todosLosDocentes: Flow<List<String>> = horarioDao.getAllDocentes()

    val totalPositivosGlobal: Flow<Int> = conductaDao.getTotalPositivosGlobal()
    val totalNegativosGlobal: Flow<Int> = conductaDao.getTotalNegativosGlobal()

    fun getSlotsPorDocente(docente: String): Flow<List<HorarioSlot>> =
        horarioDao.getSlotsPorDocente(docente)

    fun getSlotsPorDocenteYDia(docente: String, dia: Int): Flow<List<HorarioSlot>> =
        horarioDao.getSlotsPorDocenteYDia(docente, dia)

    fun getAlumnosPorUnidad(unidad: String): Flow<List<Alumno>> =
        alumnoDao.getAlumnosPorUnidad(unidad)

    fun getRegistrosPorAlumnoYFecha(alumnoId: Long, fecha: String): Flow<List<RegistroConducta>> =
        conductaDao.getRegistrosPorAlumnoYFecha(alumnoId, fecha)

    fun getRegistrosPorAlumno(alumnoId: Long): Flow<List<RegistroConducta>> =
        conductaDao.getRegistrosPorAlumno(alumnoId)

    fun getPositivosTotal(alumnoId: Long): Flow<Int> =
        conductaDao.getPositivosTotal(alumnoId)

    fun getNegativosTotal(alumnoId: Long): Flow<Int> =
        conductaDao.getNegativosTotal(alumnoId)

    suspend fun eliminarRegistroConducta(registro: RegistroConducta) = withContext(Dispatchers.IO) {
        conductaDao.deleteRegistro(registro)
    }

    fun getPositivosHoy(alumnoId: Long, fecha: String): Flow<Int> =
        conductaDao.getPositivosHoy(alumnoId, fecha)

    fun getNegativosHoy(alumnoId: Long, fecha: String): Flow<Int> =
        conductaDao.getNegativosHoy(alumnoId, fecha)

    fun getClasesPorFecha(fechaIso: String): Flow<List<DiarioClase>> =
        diarioDao.getClasesPorFecha(fechaIso)

    suspend fun getClasesPorFechaDirecto(fechaIso: String): List<DiarioClase> =
        withContext(Dispatchers.IO) {
            diarioDao.getClasesPorFechaDirecto(fechaIso)
        }

    suspend fun getDocenteActivoConfig(): String = withContext(Dispatchers.IO) {
        configuracionDao.getValor("docente_activo") ?: FictionalDataSeeder.DOCENTE_DEFECTO
    }

    suspend fun setDocenteActivoConfig(docente: String) = withContext(Dispatchers.IO) {
        configuracionDao.setValor(ConfiguracionApp(clave = "docente_activo", valor = docente))
    }

    /**
     * Sincroniza el día lectivo usando los tramos del docente activo.
     */
    suspend fun sincronizarDiaLectivo(localDate: LocalDate, docente: String): Boolean = withContext(Dispatchers.IO) {
        val diaSemana = localDate.dayOfWeek
        if (diaSemana == DayOfWeek.SATURDAY || diaSemana == DayOfWeek.SUNDAY) {
            return@withContext false
        }

        val diaNumero = diaSemana.value
        val fechaIso = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val fechaVisible = localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()))
        val idFecha = localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd", Locale.getDefault()))

        val slotsDelDia = horarioDao.getSlotsPorDocenteYDiaDirecto(docente, diaNumero)
        val llavesExistentes = diarioDao.getLlavesPorFecha(fechaIso).toSet()

        val nuevasClases = mutableListOf<DiarioClase>()
        slotsDelDia.forEachIndexed { index, slot ->
            val horaLimpia = slot.horaInicio.replace(":", "")
            val llave = "${idFecha}_$horaLimpia"
            if (!llavesExistentes.contains(llave)) {
                nuevasClases.add(
                    DiarioClase(
                        fecha = fechaIso,
                        fechaVisible = fechaVisible,
                        hora = slot.tramoHorario,
                        grupo = slot.etiquetaCompleta,
                        actividad = "",
                        observaciones = if (slot.aula.isNotBlank()) "Aula: ${slot.aula}" else "",
                        completada = false,
                        llaveUnica = llave,
                        orden = index
                    )
                )
            }
        }

        if (nuevasClases.isNotEmpty()) {
            diarioDao.insertAll(nuevasClases)
        }
        return@withContext true
    }

    suspend fun actualizarActividadYObservaciones(id: Long, actividad: String, observaciones: String) =
        withContext(Dispatchers.IO) {
            diarioDao.updateActividadYObservaciones(id, actividad.trim(), observaciones.trim())
        }

    suspend fun toggleCompletada(id: Long, completada: Boolean) = withContext(Dispatchers.IO) {
        diarioDao.toggleCompletada(id, completada)
    }

    suspend fun guardarSlot(slot: HorarioSlot) = withContext(Dispatchers.IO) {
        if (slot.id == 0L) {
            horarioDao.insertSlot(slot)
        } else {
            horarioDao.updateSlot(slot)
        }
    }

    suspend fun eliminarSlot(slot: HorarioSlot) = withContext(Dispatchers.IO) {
        horarioDao.deleteSlot(slot)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // GESTIÓN DE CONDUCTA / CARITAS
    // ─────────────────────────────────────────────────────────────────────────────

    suspend fun agregarRegistroConducta(
        alumno: Alumno,
        fecha: String,
        hora: String,
        tipo: String, // "POSITIVO" | "NEGATIVO" | "OBSERVACION"
        motivo: String = ""
    ) = withContext(Dispatchers.IO) {
        val registro = RegistroConducta(
            alumnoId = alumno.id,
            fecha = fecha,
            hora = hora,
            tipo = tipo,
            observacion = motivo.trim()
        )
        conductaDao.insertRegistro(registro)
    }

    suspend fun quitarUltimoRegistro(alumnoId: Long, fecha: String, tipo: String) = withContext(Dispatchers.IO) {
        conductaDao.restarUltimoRegistro(alumnoId, fecha, tipo)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // SEMILLADO DIDÁCTICO Y GESTIÓN DE ARCHIVOS
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Si la base de datos está vacía, se inicializa con la muestra didáctica ficticia:
     * - Docentes célebres de la historia
     * - Alumnos de literatura infantil
     * - Claustro de intelectuales
     */
    suspend fun inicializarDatosSiVacio(context: Context) = withContext(Dispatchers.IO) {
        // Comprobar si ya se configuró un docente o si está virgen
        val yaInicializado = configuracionDao.getValor("bd_inicializada") == "true"
        if (yaInicializado) return@withContext

        // 1. Horario de ejemplo con intelectuales
        if (horarioDao.getCount() == 0) {
            val todosSlots = mutableListOf<HorarioSlot>()
            for (doc in FictionalDataSeeder.getDocentesEjemplo()) {
                todosSlots.addAll(FictionalDataSeeder.getHorarioEjemplo(doc))
            }
            horarioDao.insertAll(todosSlots)
            setDocenteActivoConfig(FictionalDataSeeder.DOCENTE_DEFECTO)
        }

        // 2. Alumnos de ejemplo (Literatura Infantil)
        if (alumnoDao.getCount() == 0) {
            alumnoDao.insertAll(FictionalDataSeeder.getAlumnosEjemplo())
        }

        // 3. Claustro de ejemplo (Filósofos y Científicos)
        if (companeroDao.getCount() == 0) {
            companeroDao.insertAll(FictionalDataSeeder.getClaustroEjemplo())
        }

        configuracionDao.setValor(ConfiguracionApp(clave = "bd_inicializada", valor = "true"))
    }

    /**
     * Permite reiniciar o limpiar todos los datos para dejar la app completamente en blanco
     * para un centro educativo nuevo.
     */
    suspend fun limpiarTodosLosDatos() = withContext(Dispatchers.IO) {
        horarioDao.deleteAll()
        diarioDao.deleteAll()
        alumnoDao.deleteAll()
        companeroDao.deleteAll()
        conductaDao.deleteAll()
        configuracionDao.setValor(ConfiguracionApp(clave = "docente_activo", valor = ""))
        configuracionDao.setValor(ConfiguracionApp(clave = "bd_inicializada", valor = "true"))
    }

    /**
     * Carga de nuevo la muestra de literatura e intelectuales
     */
    suspend fun cargarMuestraLiteratura() = withContext(Dispatchers.IO) {
        horarioDao.deleteAll()
        diarioDao.deleteAll()
        alumnoDao.deleteAll()
        companeroDao.deleteAll()
        conductaDao.deleteAll()

        val todosSlots = mutableListOf<HorarioSlot>()
        for (doc in FictionalDataSeeder.getDocentesEjemplo()) {
            todosSlots.addAll(FictionalDataSeeder.getHorarioEjemplo(doc))
        }
        horarioDao.insertAll(todosSlots)
        setDocenteActivoConfig(FictionalDataSeeder.DOCENTE_DEFECTO)
        alumnoDao.insertAll(FictionalDataSeeder.getAlumnosEjemplo())
        companeroDao.insertAll(FictionalDataSeeder.getClaustroEjemplo())
        configuracionDao.setValor(ConfiguracionApp(clave = "bd_inicializada", valor = "true"))
    }

    suspend fun importarHorarioDesdeBytes(bytes: ByteArray, docente: String): Int = withContext(Dispatchers.IO) {
        val slots = CsvParser.parseFetHorario(bytes, docente)
        if (slots.isNotEmpty()) {
            horarioDao.deleteAll()
            diarioDao.deleteAll()
            horarioDao.insertAll(slots)
            setDocenteActivoConfig(docente)
            configuracionDao.setValor(ConfiguracionApp(clave = "bd_inicializada", valor = "true"))
        }
        slots.size
    }

    suspend fun importarAlumnosDesdeBytes(bytes: ByteArray, unidadesFiltro: Set<String> = emptySet()): Int = withContext(Dispatchers.IO) {
        val alumnos = CsvParser.parseSenecaAlumnos(bytes, unidadesFiltro)
        if (alumnos.isNotEmpty()) {
            alumnoDao.deleteAll()
            alumnoDao.insertAll(alumnos)
            configuracionDao.setValor(ConfiguracionApp(clave = "bd_inicializada", valor = "true"))
        }
        alumnos.size
    }

    suspend fun importarCompanerosDesdeBytes(bytes: ByteArray): Int = withContext(Dispatchers.IO) {
        val lista = CsvParser.parseRelPerCen(bytes)
        if (lista.isNotEmpty()) {
            companeroDao.deleteAll()
            companeroDao.insertAll(lista)
            configuracionDao.setValor(ConfiguracionApp(clave = "bd_inicializada", valor = "true"))
        }
        lista.size
    }
}
