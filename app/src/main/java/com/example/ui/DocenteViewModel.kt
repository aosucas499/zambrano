package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DocenteRepository
import com.example.data.model.Alumno
import com.example.data.model.CompaneroCen
import com.example.data.model.DiarioClase
import com.example.data.model.HorarioSlot
import com.example.util.CsvParser
import com.example.util.DateUtils
import com.example.util.PdfExporter
import com.example.util.ShareUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.data.model.RegistroConducta
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class AlumnoConConducta(
    val alumno: Alumno,
    val positivosHoy: Int = 0,
    val negativosHoy: Int = 0
)

data class DocenteUiState(
    val fechaSeleccionada: LocalDate = LocalDate.now(),
    val esFinDeSemana: Boolean = DateUtils.esFinDeSemana(LocalDate.now()),
    val diaHorarioTab: Int = 1, // 1=Lunes .. 5=Viernes
    val docenteActivo: String = "Miguel de Cervantes",
    val listaDocentes: List<String> = emptyList(),
    val unidadSeleccionada: String = "",
    val busquedaAlumnos: String = "",
    val busquedaCompaneros: String = "",
    val claseEnEdicion: DiarioClase? = null,
    val slotEnEdicion: HorarioSlot? = null,
    val esNuevoSlot: Boolean = false,
    val mostrarAjustesHorarioDialog: Boolean = false,
    val alumnoParaObservacion: Alumno? = null,
    val alumnoDetalle: Alumno? = null,
    val unidadesParaSeleccion: List<String> = emptyList(),
    val unidadesMarcadas: Set<String> = emptySet(),
    val bytesPendientesAlumnos: ByteArray? = null,
    val docentesParaSeleccionFet: List<String> = emptyList(),
    val bytesPendientesFet: ByteArray? = null,
    val snackbarMensaje: String? = null,
    val estaCargando: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class DocenteViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository: DocenteRepository = DocenteRepository(
        db.horarioDao(),
        db.diarioDao(),
        db.alumnoDao(),
        db.conductaDao(),
        db.companeroDao(),
        db.configuracionDao()
    )

    private val _uiState = MutableStateFlow(DocenteUiState())
    val uiState: StateFlow<DocenteUiState> = _uiState.asStateFlow()

    private val _fechaFlow = MutableStateFlow(LocalDate.now())
    private val _docenteFlow = MutableStateFlow("Miguel de Cervantes")
    private val _unidadFlow = MutableStateFlow("")

    val clasesDelDia: StateFlow<List<DiarioClase>> = _fechaFlow
        .flatMapLatest { fecha ->
            val iso = fecha.format(DateUtils.isoFormatter)
            repository.getClasesPorFecha(iso)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todosLosSlots: StateFlow<List<HorarioSlot>> = _docenteFlow
        .flatMapLatest { doc ->
            repository.getSlotsPorDocente(doc)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val listaDocentes: StateFlow<List<String>> = repository.todosLosDocentes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todasLasUnidades: StateFlow<List<String>> = repository.todasLasUnidades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCompletadas: StateFlow<Int> = repository.totalCompletadas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalClases: StateFlow<Int> = repository.totalClases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalPositivosGlobal: StateFlow<Int> = repository.totalPositivosGlobal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalNegativosGlobal: StateFlow<Int> = repository.totalNegativosGlobal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todosLosCompaneros: StateFlow<List<CompaneroCen>> = repository.todosLosCompaneros
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Alumnos filtrados según unidad actual y búsqueda
    val alumnosDeUnidad: StateFlow<List<Alumno>> = combine(_unidadFlow, _uiState) { unidad, state ->
        Pair(unidad, state.busquedaAlumnos)
    }.flatMapLatest { (unidad, query) ->
        if (unidad.isBlank()) {
            repository.todosLosAlumnos
        } else {
            repository.getAlumnosPorUnidad(unidad)
        }
    }.combine(_uiState) { lista, state ->
        val query = state.busquedaAlumnos.trim().lowercase()
        if (query.isBlank()) {
            lista
        } else {
            lista.filter {
                it.nombreCompleto.lowercase().contains(query) ||
                        it.unidad.lowercase().contains(query)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val detalleRegistrosAlumno: StateFlow<List<RegistroConducta>> = _uiState
        .map { it.alumnoDetalle }
        .distinctUntilChanged()
        .flatMapLatest { alumno ->
            if (alumno == null) flowOf(emptyList())
            else repository.getRegistrosPorAlumno(alumno.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val detallePositivosTotal: StateFlow<Int> = _uiState
        .map { it.alumnoDetalle }
        .distinctUntilChanged()
        .flatMapLatest { alumno ->
            if (alumno == null) flowOf(0)
            else repository.getPositivosTotal(alumno.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val detalleNegativosTotal: StateFlow<Int> = _uiState
        .map { it.alumnoDetalle }
        .distinctUntilChanged()
        .flatMapLatest { alumno ->
            if (alumno == null) flowOf(0)
            else repository.getNegativosTotal(alumno.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        viewModelScope.launch {
            // Inicializar base de datos con assets si está vacía
            repository.inicializarDatosSiVacio(getApplication())
            val doc = repository.getDocenteActivoConfig()
            _docenteFlow.value = doc
            _uiState.value = _uiState.value.copy(docenteActivo = doc)

            // Cargar lista de docentes disponibles
            listaDocentes.collect { docs ->
                if (docs.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(listaDocentes = docs)
                }
            }
        }

        // Mantener "Todas" por defecto para ver la totalidad del alumnado cargado (300+ alumnos)
        val hoy = LocalDate.now()
        seleccionarFecha(hoy)
    }

    private fun determinarUnidadPorHoraActual(unidadesDisponibles: List<String>) {
        val ahora = LocalTime.now()
        val diaSemana = LocalDate.now().dayOfWeek.value
        val slots = todosLosSlots.value

        val slotActual = slots.firstOrNull { slot ->
            slot.diaSemana == diaSemana && estaEnRango(ahora, slot.horaInicio, slot.horaFin)
        }

        if (slotActual != null) {
            val normGrupo = CsvParser.normalizarUnidad(slotActual.grupo)
            val encontrada = unidadesDisponibles.firstOrNull {
                CsvParser.normalizarUnidad(it).equals(normGrupo, ignoreCase = true) ||
                        it.contains(slotActual.grupo, ignoreCase = true)
            }
            if (encontrada != null) {
                seleccionarUnidad(encontrada)
                return
            }
        }

        if (unidadesDisponibles.isNotEmpty()) {
            seleccionarUnidad(unidadesDisponibles.first())
        }
    }

    private fun estaEnRango(hora: LocalTime, inicioStr: String, finStr: String): Boolean {
        return try {
            val inicio = LocalTime.parse(inicioStr)
            val fin = LocalTime.parse(finStr)
            !hora.isBefore(inicio) && hora.isBefore(fin)
        } catch (_: Exception) {
            false
        }
    }

    fun seleccionarDocente(docente: String) {
        viewModelScope.launch {
            repository.setDocenteActivoConfig(docente)
            _docenteFlow.value = docente
            _uiState.value = _uiState.value.copy(docenteActivo = docente)
            // Sincronizar fecha activa con el nuevo horario del docente
            repository.sincronizarDiaLectivo(_uiState.value.fechaSeleccionada, docente)
        }
    }

    fun seleccionarUnidad(unidad: String) {
        _unidadFlow.value = unidad
        _uiState.value = _uiState.value.copy(unidadSeleccionada = unidad)
    }

    fun setBusquedaAlumnos(query: String) {
        _uiState.value = _uiState.value.copy(busquedaAlumnos = query)
    }

    fun setBusquedaCompaneros(query: String) {
        _uiState.value = _uiState.value.copy(busquedaCompaneros = query)
    }

    fun navegarAUnidadDeClase(grupo: String) {
        val norm = CsvParser.normalizarUnidad(grupo)
        val unidades = todasLasUnidades.value
        val encontrada = unidades.firstOrNull {
            CsvParser.normalizarUnidad(it).equals(norm, ignoreCase = true) ||
                    it.contains(grupo, ignoreCase = true)
        } ?: unidades.firstOrNull() ?: grupo

        seleccionarUnidad(encontrada)
    }

    fun sumarCaritaPositiva(alumno: Alumno) {
        viewModelScope.launch {
            val fechaIso = _uiState.value.fechaSeleccionada.format(DateUtils.isoFormatter)
            val horaStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            repository.agregarRegistroConducta(
                alumno = alumno,
                fecha = fechaIso,
                hora = horaStr,
                tipo = "POSITIVO",
                motivo = "Participación y actitud positiva"
            )
            _uiState.value = _uiState.value.copy(
                snackbarMensaje = "😊 +1 Positivo a ${alumno.nombreCompleto}"
            )
        }
    }

    fun restarCaritaPositiva(alumno: Alumno) {
        viewModelScope.launch {
            val fechaIso = _uiState.value.fechaSeleccionada.format(DateUtils.isoFormatter)
            repository.quitarUltimoRegistro(alumno.id, fechaIso, "POSITIVO")
        }
    }

    fun sumarCaritaNegativa(alumno: Alumno) {
        viewModelScope.launch {
            val fechaIso = _uiState.value.fechaSeleccionada.format(DateUtils.isoFormatter)
            val horaStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            repository.agregarRegistroConducta(
                alumno = alumno,
                fecha = fechaIso,
                hora = horaStr,
                tipo = "NEGATIVO",
                motivo = "Amonestación / Llamada de atención"
            )
            _uiState.value = _uiState.value.copy(
                snackbarMensaje = "😟 -1 Negativo a ${alumno.nombreCompleto}"
            )
        }
    }

    fun restarCaritaNegativa(alumno: Alumno) {
        viewModelScope.launch {
            val fechaIso = _uiState.value.fechaSeleccionada.format(DateUtils.isoFormatter)
            repository.quitarUltimoRegistro(alumno.id, fechaIso, "NEGATIVO")
        }
    }

    fun abrirDialogoObservacion(alumno: Alumno) {
        _uiState.value = _uiState.value.copy(alumnoParaObservacion = alumno)
    }

    fun cerrarDialogoObservacion() {
        _uiState.value = _uiState.value.copy(alumnoParaObservacion = null)
    }

    fun guardarObservacionAlumno(alumno: Alumno, texto: String) {
        viewModelScope.launch {
            val fechaIso = _uiState.value.fechaSeleccionada.format(DateUtils.isoFormatter)
            val horaStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            repository.agregarRegistroConducta(
                alumno = alumno,
                fecha = fechaIso,
                hora = horaStr,
                tipo = "OBSERVACION",
                motivo = texto
            )
            _uiState.value = _uiState.value.copy(
                alumnoParaObservacion = null,
                snackbarMensaje = "Anotación guardada para ${alumno.nombreCompleto}"
            )
        }
    }

    fun abrirDetalleAlumno(alumno: Alumno) {
        _uiState.value = _uiState.value.copy(alumnoDetalle = alumno)
    }

    fun cerrarDetalleAlumno() {
        _uiState.value = _uiState.value.copy(alumnoDetalle = null)
    }

    fun eliminarRegistroConducta(registro: RegistroConducta) {
        viewModelScope.launch {
            repository.eliminarRegistroConducta(registro)
        }
    }

    fun seleccionarFecha(fecha: LocalDate) {
        val esFinde = DateUtils.esFinDeSemana(fecha)
        _uiState.value = _uiState.value.copy(
            fechaSeleccionada = fecha,
            esFinDeSemana = esFinde
        )
        _fechaFlow.value = fecha

        if (!esFinde) {
            viewModelScope.launch {
                repository.sincronizarDiaLectivo(fecha, _uiState.value.docenteActivo)
            }
        }
    }

    fun irAHoy() {
        seleccionarFecha(LocalDate.now())
    }

    fun diaSiguiente() {
        seleccionarFecha(_uiState.value.fechaSeleccionada.plusDays(1))
    }

    fun diaAnterior() {
        seleccionarFecha(_uiState.value.fechaSeleccionada.minusDays(1))
    }

    fun irAlProximoLunes() {
        seleccionarFecha(DateUtils.siguienteDiaLectivo(_uiState.value.fechaSeleccionada))
    }

    fun cambiarDiaHorarioTab(diaNumero: Int) {
        _uiState.value = _uiState.value.copy(diaHorarioTab = diaNumero)
    }

    fun abrirAjustesHorarioDialog() {
        _uiState.value = _uiState.value.copy(mostrarAjustesHorarioDialog = true)
    }

    fun cerrarAjustesHorarioDialog() {
        _uiState.value = _uiState.value.copy(mostrarAjustesHorarioDialog = false)
    }

    fun abrirEditorClase(clase: DiarioClase) {
        _uiState.value = _uiState.value.copy(claseEnEdicion = clase)
    }

    fun cerrarEditorClase() {
        _uiState.value = _uiState.value.copy(claseEnEdicion = null)
    }

    fun guardarEdicionClase(id: Long, actividad: String, observaciones: String) {
        viewModelScope.launch {
            repository.actualizarActividadYObservaciones(id, actividad, observaciones)
            _uiState.value = _uiState.value.copy(
                claseEnEdicion = null,
                snackbarMensaje = "Planificación actualizada correctamente"
            )
        }
    }

    fun toggleCompletada(clase: DiarioClase) {
        viewModelScope.launch {
            repository.toggleCompletada(clase.id, !clase.completada)
        }
    }

    fun abrirNuevoSlot(diaSemana: Int) {
        val nuevo = HorarioSlot(
            docente = _uiState.value.docenteActivo,
            diaSemana = diaSemana,
            horaInicio = "08:15",
            horaFin = "09:15",
            grupo = "",
            asignatura = "",
            aula = ""
        )
        _uiState.value = _uiState.value.copy(
            slotEnEdicion = nuevo,
            esNuevoSlot = true
        )
    }

    fun abrirEditarSlot(slot: HorarioSlot) {
        _uiState.value = _uiState.value.copy(
            slotEnEdicion = slot,
            esNuevoSlot = false
        )
    }

    fun cerrarEditorSlot() {
        _uiState.value = _uiState.value.copy(slotEnEdicion = null)
    }

    fun guardarSlot(slot: HorarioSlot) {
        viewModelScope.launch {
            val slotConDocente = slot.copy(
                docente = if (slot.docente.isBlank()) _uiState.value.docenteActivo else slot.docente
            )
            repository.guardarSlot(slotConDocente)
            val fechaActual = _uiState.value.fechaSeleccionada
            if (!DateUtils.esFinDeSemana(fechaActual) && fechaActual.dayOfWeek.value == slot.diaSemana) {
                repository.sincronizarDiaLectivo(fechaActual, _uiState.value.docenteActivo)
            }
            _uiState.value = _uiState.value.copy(
                slotEnEdicion = null,
                snackbarMensaje = "Tramo horario guardado"
            )
        }
    }

    fun eliminarSlot(slot: HorarioSlot) {
        viewModelScope.launch {
            repository.eliminarSlot(slot)
            _uiState.value = _uiState.value.copy(
                slotEnEdicion = null,
                snackbarMensaje = "Tramo horario eliminado"
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // IMPORTACIONES DE ARCHIVOS CSV (Séneca, FET, Claustro)
    // ─────────────────────────────────────────────────────────────────────────────

    fun importarCsvUri(context: Context, uri: Uri, tipo: TipoImportacion) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(estaCargando = true)
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null || bytes.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        estaCargando = false,
                        snackbarMensaje = "El archivo seleccionado está vacío"
                    )
                    return@launch
                }

                when (tipo) {
                    TipoImportacion.HORARIO_FET -> {
                        val profesores = CsvParser.extractAllTeachers(bytes)
                        if (profesores.size > 1) {
                            // Mostrar diálogo interactivo para elegir qué profesor eres
                            _uiState.value = _uiState.value.copy(
                                docentesParaSeleccionFet = profesores,
                                bytesPendientesFet = bytes,
                                estaCargando = false
                            )
                        } else {
                            val docente = profesores.firstOrNull() ?: _uiState.value.docenteActivo
                            confirmarImportacionFet(bytes, docente)
                        }
                    }
                    TipoImportacion.ALUMNOS_SENECA -> {
                        // Detectar si el CSV tiene múltiples unidades (RegAlum.csv) para mostrar selector
                        val unidades = CsvParser.extraerUnidadesDisponibles(bytes)
                        if (unidades.size > 1) {
                            _uiState.value = _uiState.value.copy(
                                unidadesParaSeleccion = unidades,
                                unidadesMarcadas = unidades.toSet(), // Por defecto todas marcadas
                                bytesPendientesAlumnos = bytes,
                                estaCargando = false
                            )
                        } else {
                            // Es RegMisAlu.csv (un solo curso) o una sola unidad: importar directo
                            confirmarImportacionAlumnos(bytes, emptySet())
                        }
                    }
                    TipoImportacion.CLAUSTRO -> {
                        val cant = repository.importarCompanerosDesdeBytes(bytes)
                        _uiState.value = _uiState.value.copy(
                            snackbarMensaje = "Importados $cant profesores en el Claustro",
                            estaCargando = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    snackbarMensaje = "Error al importar archivo: ${e.message}",
                    estaCargando = false
                )
            }
        }
    }

    fun toggleUnidadMarcada(unidad: String) {
        val actuales = _uiState.value.unidadesMarcadas.toMutableSet()
        if (actuales.contains(unidad)) {
            actuales.remove(unidad)
        } else {
            actuales.add(unidad)
        }
        _uiState.value = _uiState.value.copy(unidadesMarcadas = actuales)
    }

    fun marcarTodasUnidades(marcar: Boolean) {
        val nuevas = if (marcar) _uiState.value.unidadesParaSeleccion.toSet() else emptySet()
        _uiState.value = _uiState.value.copy(unidadesMarcadas = nuevas)
    }

    fun confirmarImportacionAlumnosConUnidadesMarcadas() {
        val bytes = _uiState.value.bytesPendientesAlumnos ?: return
        val unidades = _uiState.value.unidadesMarcadas
        confirmarImportacionAlumnos(bytes, unidades)
    }

    fun cancelarSeleccionUnidades() {
        _uiState.value = _uiState.value.copy(
            unidadesParaSeleccion = emptyList(),
            unidadesMarcadas = emptySet(),
            bytesPendientesAlumnos = null
        )
    }

    private fun confirmarImportacionAlumnos(bytes: ByteArray, unidadesFiltro: Set<String>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(estaCargando = true)
            val cant = repository.importarAlumnosDesdeBytes(bytes, unidadesFiltro)
            _unidadFlow.value = ""
            _uiState.value = _uiState.value.copy(
                unidadSeleccionada = "",
                unidadesParaSeleccion = emptyList(),
                unidadesMarcadas = emptySet(),
                bytesPendientesAlumnos = null,
                snackbarMensaje = "Importados $cant alumnos correctamente en 'Todas'",
                estaCargando = false
            )
        }
    }

    fun confirmarImportacionFet(bytes: ByteArray, docente: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(estaCargando = true)
            val cant = repository.importarHorarioDesdeBytes(bytes, docente)
            _docenteFlow.value = docente
            _uiState.value = _uiState.value.copy(
                docenteActivo = docente,
                docentesParaSeleccionFet = emptyList(),
                bytesPendientesFet = null,
                snackbarMensaje = "Importadas $cant clases de horario para $docente",
                estaCargando = false
            )
            repository.sincronizarDiaLectivo(_uiState.value.fechaSeleccionada, docente)
        }
    }

    fun cancelarSeleccionDocenteFet() {
        _uiState.value = _uiState.value.copy(
            docentesParaSeleccionFet = emptyList(),
            bytesPendientesFet = null
        )
    }

    fun recargarDatosEjemplo(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(estaCargando = true)
            repository.cargarMuestraLiteratura()
            val doc = repository.getDocenteActivoConfig()
            _docenteFlow.value = doc
            _uiState.value = _uiState.value.copy(
                docenteActivo = doc,
                estaCargando = false,
                snackbarMensaje = "Muestra didáctica (Literatura Infantil y Filósofos) cargada con éxito"
            )
            repository.sincronizarDiaLectivo(_uiState.value.fechaSeleccionada, doc)
        }
    }

    fun vaciarBaseDeDatos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(estaCargando = true)
            repository.limpiarTodosLosDatos()
            _docenteFlow.value = ""
            _uiState.value = _uiState.value.copy(
                docenteActivo = "",
                unidadSeleccionada = "",
                estaCargando = false,
                snackbarMensaje = "Base de datos vaciada. Lista para importar los datos de tu centro."
            )
        }
    }

    fun compartirPorWhatsApp(context: Context) {
        val fecha = _uiState.value.fechaSeleccionada
        val fechaVisible = fecha.format(DateUtils.displayFormatter)
        val clases = clasesDelDia.value
        val texto = ShareUtils.formatearTextoWhatsApp(fechaVisible, clases)
        ShareUtils.compartirPorWhatsApp(context, texto)
    }

    fun exportarPdf(context: Context) {
        val fecha = _uiState.value.fechaSeleccionada
        val fechaVisible = fecha.format(DateUtils.displayFormatter)
        val fechaLarga = DateUtils.formatoFechaLarga(fecha)
        val clases = clasesDelDia.value

        val uri = PdfExporter.generarPdfProgramacion(
            context = context,
            fechaVisible = fechaVisible,
            fechaLarga = fechaLarga,
            clases = clases
        )

        if (uri != null) {
            PdfExporter.abrirOCompartirPdf(context, uri, "Programación $fechaVisible")
        } else {
            _uiState.value = _uiState.value.copy(snackbarMensaje = "Error al generar el archivo PDF")
        }
    }

    fun limpiarSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMensaje = null)
    }
}

enum class TipoImportacion {
    HORARIO_FET,
    ALUMNOS_SENECA,
    CLAUSTRO
}
