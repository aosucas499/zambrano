package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.DocenteViewModel
import com.example.ui.components.AjustesHorarioDialog
import com.example.ui.components.CalificarAlumnoDialog
import com.example.ui.components.CrearActividadDialog
import com.example.ui.components.EditClaseDialog
import com.example.ui.components.EditHorarioSlotDialog
import com.example.ui.components.GestionActividadesDialog
import com.example.ui.screens.AlumnosScreen
import com.example.ui.screens.DiarioScreen
import com.example.ui.screens.ResumenScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.CsvParser

enum class AppDestino(val titulo: String) {
    DIARIO("Diario de Clases"),
    ALUMNADO("Alumnado y Conducta"),
    RESUMEN("Evaluación")
}

class MainActivity : ComponentActivity() {

    private val viewModel: DocenteViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val clasesDelDia by viewModel.clasesDelDia.collectAsStateWithLifecycle()
                val todosLosSlots by viewModel.todosLosSlots.collectAsStateWithLifecycle()
                val totalClases by viewModel.totalClases.collectAsStateWithLifecycle()
                val totalCompletadas by viewModel.totalCompletadas.collectAsStateWithLifecycle()
                val totalPositivos by viewModel.totalPositivosGlobal.collectAsStateWithLifecycle()
                val totalNegativos by viewModel.totalNegativosGlobal.collectAsStateWithLifecycle()
                val alumnos by viewModel.alumnosDeUnidad.collectAsStateWithLifecycle()
                val unidades by viewModel.todasLasUnidades.collectAsStateWithLifecycle()
                val companeros by viewModel.todosLosCompaneros.collectAsStateWithLifecycle()
                val listaDocentes by viewModel.listaDocentes.collectAsStateWithLifecycle()
                val registrosAlumnoDetalle by viewModel.detalleRegistrosAlumno.collectAsStateWithLifecycle()
                val totalPositivosCurso by viewModel.detallePositivosTotal.collectAsStateWithLifecycle()
                val totalNegativosCurso by viewModel.detalleNegativosTotal.collectAsStateWithLifecycle()

                // Flujos del módulo de Evaluación LOMLOE
                val cursosEvaluacion by viewModel.cursosEvaluacionDisponibles.collectAsStateWithLifecycle()
                val areasEvaluacion by viewModel.areasEvaluacionDisponibles.collectAsStateWithLifecycle()
                val criteriosEvaluacion by viewModel.criteriosEvaluacionFiltrados.collectAsStateWithLifecycle()
                val actividadesEvaluacion by viewModel.actividadesSdaActual.collectAsStateWithLifecycle()
                val todasLasActividades by viewModel.todasLasActividades.collectAsStateWithLifecycle()
                val notasEvaluacion by viewModel.notasClaseActual.collectAsStateWithLifecycle()
                val alumnosEvaluacion by viewModel.alumnosEvaluacion.collectAsStateWithLifecycle()

                var destinoActual by remember { mutableStateOf(AppDestino.DIARIO) }
                val snackbarHostState = remember { SnackbarHostState() }

                // Mostrar mensajes de confirmación
                LaunchedEffect(uiState.snackbarMensaje) {
                    uiState.snackbarMensaje?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.limpiarSnackbar()
                    }
                }

                // Manejo de botón atrás si estamos en otra pestaña secundaria
                if (destinoActual != AppDestino.DIARIO) {
                    BackHandler {
                        destinoActual = AppDestino.DIARIO
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = destinoActual.titulo,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            actions = {
                                // Rueda dentada: en Evaluación abre la gestión de actividades; en Diario abre ajustes de horario
                                IconButton(
                                    onClick = {
                                        if (destinoActual == AppDestino.RESUMEN) {
                                            viewModel.abrirGestionActividadesDialog()
                                        } else {
                                            viewModel.abrirAjustesHorarioDialog()
                                        }
                                    },
                                    modifier = Modifier.testTag(
                                        if (destinoActual == AppDestino.RESUMEN) "btn_ajustes_actividades_gear" else "btn_ajustes_horario_gear"
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = if (destinoActual == AppDestino.RESUMEN)
                                            "Gestión de actividades de evaluación"
                                        else
                                            "Ajustar horario semanal y docente",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("barra_navegacion")
                        ) {
                            NavigationBarItem(
                                selected = destinoActual == AppDestino.DIARIO,
                                onClick = { destinoActual = AppDestino.DIARIO },
                                icon = {
                                    Icon(
                                        imageVector = if (destinoActual == AppDestino.DIARIO)
                                            Icons.Default.EditCalendar else Icons.Outlined.EditCalendar,
                                        contentDescription = "Diario"
                                    )
                                },
                                label = { Text("Diario") },
                                modifier = Modifier.testTag("nav_item_diario")
                            )

                            NavigationBarItem(
                                selected = destinoActual == AppDestino.ALUMNADO,
                                onClick = { destinoActual = AppDestino.ALUMNADO },
                                icon = {
                                    Icon(
                                        imageVector = if (destinoActual == AppDestino.ALUMNADO)
                                            Icons.Default.Groups else Icons.Outlined.Groups,
                                        contentDescription = "Alumnado"
                                    )
                                },
                                label = { Text("Alumnado") },
                                modifier = Modifier.testTag("nav_item_alumnado")
                            )

                            NavigationBarItem(
                                selected = destinoActual == AppDestino.RESUMEN,
                                onClick = { destinoActual = AppDestino.RESUMEN },
                                icon = {
                                    Icon(
                                        imageVector = if (destinoActual == AppDestino.RESUMEN)
                                            Icons.Default.Assessment else Icons.Outlined.Assessment,
                                        contentDescription = "Evaluación"
                                    )
                                },
                                label = { Text("Evaluación") },
                                modifier = Modifier.testTag("nav_item_resumen")
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (destinoActual) {
                            AppDestino.DIARIO -> DiarioScreen(
                                uiState = uiState,
                                clases = clasesDelDia,
                                onFechaSeleccionada = { viewModel.seleccionarFecha(it) },
                                onDiaAnterior = { viewModel.diaAnterior() },
                                onDiaSiguiente = { viewModel.diaSiguiente() },
                                onIrAHoy = { viewModel.irAHoy() },
                                onIrAlLunes = { viewModel.irAlProximoLunes() },
                                onEditarClase = { viewModel.abrirEditorClase(it) },
                                onToggleCompletada = { viewModel.toggleCompletada(it) },
                                onCompartirWhatsApp = { viewModel.compartirPorWhatsApp(context) },
                                onExportarPdf = { viewModel.exportarPdf(context) },
                                onIrAAlumnosUnidad = { grupo ->
                                    viewModel.navegarAUnidadDeClase(grupo)
                                    destinoActual = AppDestino.ALUMNADO
                                },
                                onIrAHorarioTab = { viewModel.abrirAjustesHorarioDialog() }
                            )

                            AppDestino.ALUMNADO -> AlumnosScreen(
                                uiState = uiState,
                                alumnos = alumnos,
                                unidades = unidades,
                                registrosAlumnoDetalle = registrosAlumnoDetalle,
                                totalPositivosCurso = totalPositivosCurso,
                                totalNegativosCurso = totalNegativosCurso,
                                onSeleccionarUnidad = { viewModel.seleccionarUnidad(it) },
                                onBusquedaChanged = { viewModel.setBusquedaAlumnos(it) },
                                onAbrirDetalleAlumno = { viewModel.abrirDetalleAlumno(it) },
                                onCerrarDetalleAlumno = { viewModel.cerrarDetalleAlumno() },
                                onSumarPositivo = { viewModel.sumarCaritaPositiva(it) },
                                onSumarNegativo = { viewModel.sumarCaritaNegativa(it) },
                                onAbrirNota = { viewModel.abrirDialogoObservacion(it) },
                                onGuardarNota = { alumno, texto ->
                                    viewModel.guardarObservacionAlumno(alumno, texto)
                                },
                                onCerrarNota = { viewModel.cerrarDialogoObservacion() },
                                onEliminarRegistro = { viewModel.eliminarRegistroConducta(it) }
                            )

                            AppDestino.RESUMEN -> ResumenScreen(
                                uiState = uiState,
                                companeros = companeros,
                                cursosDisponibles = cursosEvaluacion,
                                areasDisponibles = areasEvaluacion,
                                criterios = criteriosEvaluacion,
                                actividades = actividadesEvaluacion,
                                notas = notasEvaluacion,
                                alumnosEvaluacion = alumnosEvaluacion,
                                onCambiarModoEvaluacion = { viewModel.cambiarModoEvaluacion(it) },
                                onCambiarUnidadSda = { viewModel.cambiarUnidadSda(it) },
                                onCambiarCursoEvaluacion = { viewModel.cambiarCursoEvaluacion(it) },
                                onCambiarAreaEvaluacion = { viewModel.cambiarAreaEvaluacion(it) },
                                onAbrirNuevaActividad = { viewModel.abrirDialogoNuevaActividad() },
                                onEliminarActividad = { id, nom -> viewModel.eliminarActividad(id, nom) },
                                onCalificarAlumno = { alu, act -> viewModel.abrirCalificacion(alu, act) },
                                onExportarSeneca = { viewModel.exportarSdaSeneca(context) },
                                onExportarPdfGlobal = { viewModel.exportarPdfGlobal(context) },
                                onAbrirGestionActividades = { viewModel.abrirGestionActividadesDialog() },
                                onBusquedaCompaneroChanged = { viewModel.setBusquedaCompaneros(it) },
                                onImportarCsv = { uri, tipo ->
                                    viewModel.importarCsvUri(context, uri, tipo)
                                },
                                onToggleUnidadMarcada = { viewModel.toggleUnidadMarcada(it) },
                                onMarcarTodasUnidades = { viewModel.marcarTodasUnidades(it) },
                                onConfirmarImportacionUnidades = { unidades, areas ->
                                    viewModel.confirmarImportacionAlumnosConUnidadesMarcadas(unidades, areas)
                                },
                                onCancelarSeleccionUnidades = { viewModel.cancelarSeleccionUnidades() },
                                onConfirmarDocenteFet = { bytes, doc -> viewModel.confirmarImportacionFet(bytes, doc) },
                                onCancelarSeleccionFet = { viewModel.cancelarSeleccionDocenteFet() },
                                onVaciarBaseDeDatos = { viewModel.vaciarBaseDeDatos() }
                            )
                        }

                        // Diálogo para editar clase/actividad
                        uiState.claseEnEdicion?.let { clase ->
                            EditClaseDialog(
                                clase = clase,
                                onDismiss = { viewModel.cerrarEditorClase() },
                                onSave = { id, act, obs ->
                                    viewModel.guardarEdicionClase(id, act, obs)
                                }
                            )
                        }

                        // Diálogo para editar/añadir slot en horario
                        uiState.slotEnEdicion?.let { slot ->
                            EditHorarioSlotDialog(
                                slot = slot,
                                isNew = uiState.esNuevoSlot,
                                onDismiss = { viewModel.cerrarEditorSlot() },
                                onSave = { viewModel.guardarSlot(it) },
                                onDelete = { viewModel.eliminarSlot(it) }
                            )
                        }

                        // Diálogo Ajustes de Horario y Docente (Rueda dentada ⚙️ en Diario)
                        if (uiState.mostrarAjustesHorarioDialog) {
                            AjustesHorarioDialog(
                                uiState = uiState,
                                todosLosSlots = todosLosSlots,
                                listaDocentes = listaDocentes,
                                onSeleccionarDocente = { viewModel.seleccionarDocente(it) },
                                onNuevoSlot = { dia -> viewModel.abrirNuevoSlot(dia) },
                                onEditarSlot = { slot -> viewModel.abrirEditarSlot(slot) },
                                onEliminarSlot = { slot -> viewModel.eliminarSlot(slot) },
                                onDismiss = { viewModel.cerrarAjustesHorarioDialog() }
                            )
                        }

                        // Diálogo para crear nueva actividad de evaluación
                        if (uiState.mostrarDialogoNuevaActividad) {
                            CrearActividadDialog(
                                unidadSda = uiState.evaluacionUnidadSda,
                                area = uiState.evaluacionAreaSeleccionada,
                                curso = CsvParser.extraerCursoNumero(uiState.evaluacionCursoSeleccionado),
                                grupo = uiState.evaluacionCursoSeleccionado,
                                criteriosDisponibles = criteriosEvaluacion,
                                onDismiss = { viewModel.cerrarDialogoNuevaActividad() },
                                onCrear = { nombre, codigos ->
                                    viewModel.crearNuevaActividad(nombre, codigos)
                                }
                            )
                        }

                        // Diálogo para calificar un alumno en una actividad
                        val alumnoCalificando = uiState.alumnoParaCalificar
                        val actividadCalificando = uiState.actividadParaCalificar
                        if (alumnoCalificando != null && actividadCalificando != null) {
                            CalificarAlumnoDialog(
                                alumno = alumnoCalificando,
                                actividad = actividadCalificando,
                                criteriosCatalogo = criteriosEvaluacion,
                                notasActuales = notasEvaluacion,
                                onDismiss = { viewModel.cerrarCalificacion() },
                                onGuardar = { calificaciones ->
                                    viewModel.guardarCalificacionesAlumno(calificaciones)
                                }
                            )
                        }

                        // Diálogo para gestionar actividades (Rueda dentada ⚙️ en Evaluación)
                        if (uiState.mostrarGestionActividadesDialog) {
                            GestionActividadesDialog(
                                actividades = todasLasActividades,
                                onEliminarActividad = { id, nom -> viewModel.eliminarActividad(id, nom) },
                                onDismiss = { viewModel.cerrarGestionActividadesDialog() }
                            )
                        }
                    }
                }
            }
        }
    }
}
