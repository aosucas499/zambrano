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

import com.example.data.dao.EvaluacionDao
import com.example.data.model.ActividadEvaluacion
import com.example.data.model.CriterioEvaluacion
import com.example.data.model.NotaEvaluacion

class DocenteRepository(
    private val horarioDao: HorarioDao,
    private val diarioDao: DiarioDao,
    private val alumnoDao: AlumnoDao,
    private val conductaDao: ConductaDao,
    private val companeroDao: CompaneroDao,
    private val configuracionDao: ConfiguracionDao,
    private val evaluacionDao: EvaluacionDao
) {
    val todosLosSlots: Flow<List<HorarioSlot>> = horarioDao.getAllSlots()
    val ultimasClases: Flow<List<DiarioClase>> = diarioDao.getUltimasClases()
    val totalCompletadas: Flow<Int> = diarioDao.getConteoCompletadas()
    val totalClases: Flow<Int> = diarioDao.getConteoTotal()

    val todosLosCriterios: Flow<List<CriterioEvaluacion>> = evaluacionDao.getAllCriterios()
    val todasLasActividades: Flow<List<ActividadEvaluacion>> = evaluacionDao.getAllActividades()

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

    // ==========================================
    // SECCIÓN EVALUACIÓN LOMLOE
    // ==========================================

    suspend fun asegurarCriteriosPrecargados(context: Context) = withContext(Dispatchers.IO) {
        val total = evaluacionDao.countCriterios()
        if (total == 0) {
            try {
                context.assets.open("ce_primaria.csv").use { stream ->
                    val criterios = CsvParser.parseCriteriosEvaluacion(stream)
                    if (criterios.isNotEmpty()) {
                        evaluacionDao.insertCriterios(criterios)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getCriteriosPorCurso(curso: String): Flow<List<CriterioEvaluacion>> =
        evaluacionDao.getCriteriosPorCurso(curso)

    val todasLasNotasEvaluacion: Flow<List<NotaEvaluacion>> = evaluacionDao.getAllNotas()

    fun getActividades(area: String, curso: String, grupo: String, unidadSda: String): Flow<List<ActividadEvaluacion>> =
        evaluacionDao.getActividades(area, curso, grupo, unidadSda)

    fun getNotasPorClase(area: String, curso: String, grupo: String): Flow<List<NotaEvaluacion>> =
        evaluacionDao.getNotasPorClase(area, curso, grupo)

    fun getNotasPorSda(area: String, curso: String, grupo: String, unidadSda: String): Flow<List<NotaEvaluacion>> =
        evaluacionDao.getNotasPorSda(area, curso, grupo, unidadSda)

    suspend fun crearActividad(actividad: ActividadEvaluacion, criteriosCodigos: List<String>, alumnos: List<Alumno>): Long = withContext(Dispatchers.IO) {
        val id = evaluacionDao.insertActividad(actividad)
        // Inicializar entradas de notas para todos los alumnos en blanco
        val notasIniciales = mutableListOf<NotaEvaluacion>()
        alumnos.forEach { al ->
            criteriosCodigos.forEach { codCrit ->
                notasIniciales.add(
                    NotaEvaluacion(
                        alumnoNombre = al.nombreCompleto,
                        actividadId = id,
                        area = actividad.area,
                        curso = actividad.curso,
                        grupo = actividad.grupo,
                        unidadSda = actividad.unidadSda,
                        criterioCodigo = codCrit,
                        valor = null
                    )
                )
            }
        }
        if (notasIniciales.isNotEmpty()) {
            evaluacionDao.insertOrUpdateNotas(notasIniciales)
        }
        id
    }

    suspend fun guardarCalificacionesAlumno(
        alumnoNombre: String,
        actividad: ActividadEvaluacion,
        nuevasNotas: Map<String, Double?>
    ) = withContext(Dispatchers.IO) {
        val notasAGuardar = nuevasNotas.map { (criterio, valor) ->
            NotaEvaluacion(
                alumnoNombre = alumnoNombre,
                actividadId = actividad.id,
                area = actividad.area,
                curso = actividad.curso,
                grupo = actividad.grupo,
                unidadSda = actividad.unidadSda,
                criterioCodigo = criterio,
                valor = valor
            )
        }
        evaluacionDao.insertOrUpdateNotas(notasAGuardar)
    }

    suspend fun eliminarActividad(actividadId: Long) = withContext(Dispatchers.IO) {
        evaluacionDao.deleteNotasPorActividad(actividadId)
        evaluacionDao.deleteActividad(actividadId)
    }

    suspend fun getAsignacionesUnidadesAreas(): Map<String, Set<String>> = withContext(Dispatchers.IO) {
        val raw = configuracionDao.getValor("unidades_areas_asignadas") ?: return@withContext emptyMap()
        try {
            val json = org.json.JSONObject(raw)
            val result = mutableMapOf<String, Set<String>>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val arr = json.getJSONArray(k)
                val set = mutableSetOf<String>()
                for (i in 0 until arr.length()) {
                    set.add(arr.getString(i))
                }
                result[k] = set
            }
            result
        } catch (_: Exception) {
            emptyMap()
        }
    }

    suspend fun guardarAsignacionesUnidadesAreas(asignaciones: Map<String, Set<String>>) = withContext(Dispatchers.IO) {
        try {
            val json = org.json.JSONObject()
            asignaciones.forEach { (unidad, areas) ->
                val arr = org.json.JSONArray()
                areas.forEach { arr.put(it) }
                json.put(unidad, arr)
            }
            configuracionDao.setValor(ConfiguracionApp(clave = "unidades_areas_asignadas", valor = json.toString()))
        } catch (_: Exception) {}
    }

    // ==========================================
    // MÉTODOS PARA COPIA DE SEGURIDAD TOTAL
    // ==========================================

    suspend fun obtenerTodosLosAlumnosDirecto(): List<Alumno> = withContext(Dispatchers.IO) {
        alumnoDao.getAllDirecto()
    }

    suspend fun obtenerTodosLosCompanerosDirecto(): List<CompaneroCen> = withContext(Dispatchers.IO) {
        companeroDao.getAllDirecto()
    }

    suspend fun obtenerTodosLosSlotsDirecto(): List<HorarioSlot> = withContext(Dispatchers.IO) {
        horarioDao.getAllDirecto()
    }

    suspend fun obtenerTodosLosDiariosDirecto(): List<DiarioClase> = withContext(Dispatchers.IO) {
        diarioDao.getAllDirecto()
    }

    suspend fun obtenerTodasLasConductasDirecto(): List<RegistroConducta> = withContext(Dispatchers.IO) {
        conductaDao.getAllDirecto()
    }

    suspend fun obtenerTodasLasActividadesDirecto(): List<ActividadEvaluacion> = withContext(Dispatchers.IO) {
        evaluacionDao.getAllActividadesDirecto()
    }

    suspend fun obtenerTodasLasNotasDirecto(): List<NotaEvaluacion> = withContext(Dispatchers.IO) {
        evaluacionDao.getAllNotasDirecto()
    }

    suspend fun obtenerTodasLasConfigsDirecto(): List<ConfiguracionApp> = withContext(Dispatchers.IO) {
        configuracionDao.getAllConfig()
    }

    suspend fun restaurarBaseDeDatosCompleta(
        alumnos: List<Alumno>,
        companeros: List<CompaneroCen>,
        slots: List<HorarioSlot>,
        diarios: List<DiarioClase>,
        conductas: List<RegistroConducta>,
        actividades: List<ActividadEvaluacion>,
        notas: List<NotaEvaluacion>,
        configs: List<ConfiguracionApp>
    ) = withContext(Dispatchers.IO) {
        // Limpiar datos existentes
        horarioDao.deleteAll()
        diarioDao.deleteAll()
        alumnoDao.deleteAll()
        companeroDao.deleteAll()
        conductaDao.deleteAll()
        evaluacionDao.deleteAllActividades()
        evaluacionDao.deleteAllNotas()
        configuracionDao.deleteAll()

        // Insertar datos restaurados
        if (configs.isNotEmpty()) configuracionDao.insertAll(configs)
        if (alumnos.isNotEmpty()) alumnoDao.insertAll(alumnos)
        if (companeros.isNotEmpty()) companeroDao.insertAll(companeros)
        if (slots.isNotEmpty()) horarioDao.insertAll(slots)
        if (diarios.isNotEmpty()) diarioDao.insertAll(diarios)
        if (conductas.isNotEmpty()) conductaDao.insertAll(conductas)
        if (actividades.isNotEmpty()) evaluacionDao.insertActividades(actividades)
        if (notas.isNotEmpty()) evaluacionDao.insertOrUpdateNotas(notas)
    }
}

