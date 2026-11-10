package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ActividadEvaluacion
import com.example.data.model.Alumno
import com.example.data.model.CompaneroCen
import com.example.data.model.CriterioEvaluacion
import com.example.data.model.NotaEvaluacion
import com.example.ui.DocenteUiState
import com.example.ui.TipoImportacion
import com.example.util.CsvParser

@Composable
fun ResumenScreen(
    uiState: DocenteUiState,
    companeros: List<CompaneroCen>,
    cursosDisponibles: List<String>,
    areasDisponibles: List<String>,
    criterios: List<CriterioEvaluacion>,
    actividades: List<ActividadEvaluacion>,
    notas: List<NotaEvaluacion>,
    alumnosEvaluacion: List<Alumno>,
    onCambiarModoEvaluacion: (String) -> Unit,
    onCambiarUnidadSda: (String) -> Unit,
    onCambiarCursoEvaluacion: (String) -> Unit,
    onCambiarAreaEvaluacion: (String) -> Unit,
    onAbrirNuevaActividad: () -> Unit,
    onEliminarActividad: (Long, String) -> Unit,
    onCalificarAlumno: (Alumno, ActividadEvaluacion) -> Unit,
    onExportarSeneca: () -> Unit,
    onExportarPdfGlobal: () -> Unit,
    onAbrirGestionActividades: () -> Unit,
    onBusquedaCompaneroChanged: (String) -> Unit,
    onImportarCsv: (Uri, TipoImportacion) -> Unit,
    onToggleUnidadMarcada: (String) -> Unit,
    onMarcarTodasUnidades: (Boolean) -> Unit,
    onConfirmarImportacionUnidades: (Set<String>, Map<String, Set<String>>) -> Unit,
    onCancelarSeleccionUnidades: () -> Unit,
    onConfirmarDocenteFet: (ByteArray, String) -> Unit,
    onCancelarSeleccionFet: () -> Unit,
    onVaciarBaseDeDatos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var pestanaActual by remember { mutableIntStateOf(0) } // 0 = Evaluación, 1 = Claustro, 2 = Importar archivos

    val importSenecaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { onImportarCsv(it, TipoImportacion.ALUMNOS_SENECA) }
    }

    val importFetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { onImportarCsv(it, TipoImportacion.HORARIO_FET) }
    }

    val importClaustroLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { onImportarCsv(it, TipoImportacion.CLAUSTRO) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("pantalla_resumen")
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            TabRow(
                selectedTabIndex = pestanaActual,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = pestanaActual == 0,
                    onClick = { pestanaActual = 0 },
                    text = { Text("Evaluación") },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = pestanaActual == 1,
                    onClick = { pestanaActual = 1 },
                    text = { Text("Claustro (${companeros.size})") },
                    icon = { Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = pestanaActual == 2,
                    onClick = { pestanaActual = 2 },
                    text = { Text("Importar Archivos") },
                    icon = { Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (pestanaActual) {
                0 -> SeccionEvaluacion(
                    context = context,
                    modo = uiState.evaluacionModo,
                    unidadSda = uiState.evaluacionUnidadSda,
                    cursoSeleccionado = uiState.evaluacionCursoSeleccionado,
                    areaSeleccionada = uiState.evaluacionAreaSeleccionada,
                    cursosDisponibles = cursosDisponibles,
                    areasDisponibles = areasDisponibles,
                    criterios = criterios,
                    actividades = actividades,
                    notas = notas,
                    alumnos = alumnosEvaluacion,
                    onCambiarModo = onCambiarModoEvaluacion,
                    onCambiarUnidadSda = onCambiarUnidadSda,
                    onCambiarCurso = onCambiarCursoEvaluacion,
                    onCambiarArea = onCambiarAreaEvaluacion,
                    onAbrirNuevaActividad = onAbrirNuevaActividad,
                    onEliminarActividad = onEliminarActividad,
                    onCalificarAlumno = onCalificarAlumno,
                    onExportarSeneca = onExportarSeneca,
                    onExportarPdfGlobal = onExportarPdfGlobal,
                    onAbrirGestionActividades = onAbrirGestionActividades,
                    onIrAImportarArchivos = { pestanaActual = 2 }
                )

                1 -> SeccionClaustro(
                    context = context,
                    companeros = companeros,
                    busqueda = uiState.busquedaCompaneros,
                    onBusquedaChanged = onBusquedaCompaneroChanged
                )

                2 -> SeccionImportacionArchivos(
                    docenteActivo = uiState.docenteActivo,
                    onImportarSeneca = { importSenecaLauncher.launch(arrayOf("text/*", "application/*")) },
                    onImportarFet = { importFetLauncher.launch(arrayOf("text/*", "application/*")) },
                    onImportarClaustro = { importClaustroLauncher.launch(arrayOf("text/*", "application/*")) },
                    onVaciarDatos = onVaciarBaseDeDatos
                )
            }
        }
    }

    // Diálogo con casillas de verificación para seleccionar unidades y áreas al cargar RegAlum.csv
    if (uiState.unidadesParaSeleccion.isNotEmpty() && uiState.bytesPendientesAlumnos != null) {
        DialogoSelectorUnidades(
            unidades = uiState.unidadesParaSeleccion,
            unidadesMarcadasInicial = uiState.unidadesMarcadas,
            todasLasAreas = uiState.todasLasAreasCatalogo,
            asignacionesGuardadas = uiState.asignacionesUnidadesAreas,
            onConfirmar = { unidades, areas ->
                onConfirmarImportacionUnidades(unidades, areas)
            },
            onDismiss = onCancelarSeleccionUnidades
        )
    }

    // Diálogo con desplegable de docentes al cargar horario FET
    if (uiState.docentesParaSeleccionFet.isNotEmpty() && uiState.bytesPendientesFet != null) {
        DialogoSelectorDocenteFet(
            docentes = uiState.docentesParaSeleccionFet,
            docenteInicial = uiState.docenteActivo,
            onConfirmar = { docenteElegido ->
                onConfirmarDocenteFet(uiState.bytesPendientesFet, docenteElegido)
            },
            onDismiss = onCancelarSeleccionFet
        )
    }
}

@Composable
private fun SeccionEstadisticas(
    totalClases: Int,
    totalCompletadas: Int,
    porcentaje: Int,
    totalSlotsSemanales: Int,
    totalPositivos: Int,
    totalNegativos: Int,
    onCompartirWhatsApp: () -> Unit,
    onExportarPdf: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Balance Pedagógico de Sesiones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Seguimiento acumulado de clases y registros de conducta del alumnado",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricaCard(
                    titulo = "Clases en Diario",
                    valor = totalClases.toString(),
                    icono = Icons.Default.Assessment,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                MetricaCard(
                    titulo = "Completadas",
                    valor = "$porcentaje%",
                    icono = Icons.Default.CheckCircle,
                    color = Color(0xFF388E3C),
                    modifier = Modifier.weight(1f)
                )

                MetricaCard(
                    titulo = "Tramos Semanales",
                    valor = totalSlotsSemanales.toString(),
                    icono = Icons.Default.Info,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricaCard(
                    titulo = "Caritas Verdes (+)",
                    valor = "+$totalPositivos",
                    icono = Icons.Default.SentimentSatisfiedAlt,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )

                MetricaCard(
                    titulo = "Caritas Rojas (-)",
                    valor = "-$totalNegativos",
                    icono = Icons.Default.SentimentDissatisfied,
                    color = Color(0xFFC62828),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Exportación de Informes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Genera el informe oficial en PDF para claustro/inspección o comparte la sesión seleccionada por WhatsApp.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onCompartirWhatsApp,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_resumen_whatsapp"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp")
                        }

                        Button(
                            onClick = onExportarPdf,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_resumen_pdf"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Exportar PDF")
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SeccionClaustro(
    context: Context,
    companeros: List<CompaneroCen>,
    busqueda: String,
    onBusquedaChanged: (String) -> Unit
) {
    // Si la búsqueda está vacía, se muestran absolutamente todos los profesores ordenados por Nombre
    val filtrados = if (busqueda.isBlank()) {
        companeros
    } else {
        val q = busqueda.lowercase().trim()
        companeros.filter { it.nombre.lowercase().contains(q) || it.email.lowercase().contains(q) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("lista_companeros_claustro"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Buscador interactivo
        item(key = "search_field") {
            OutlinedTextField(
                value = busqueda,
                onValueChange = onBusquedaChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_buscar_companeros"),
                placeholder = { Text("Buscar por nombre, apellidos o correo...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    if (busqueda.isNotBlank()) {
                        IconButton(onClick = { onBusquedaChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // 2. Cabecera con contador de profesores
        item(key = "header_count") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "${filtrados.size} profesores en plantilla",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // 3. Estado vacío si no hay profesores o sin coincidencias de búsqueda
        if (filtrados.isEmpty()) {
            item(key = "empty_state") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = if (companeros.isEmpty()) "No hay profesorado cargado" else "No se encontraron coincidencias",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (companeros.isEmpty())
                                "Puedes importar la plantilla docente desde la pestaña 'Importar Archivos'."
                            else "No hay ningún docente que coincida con '$busqueda'.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // 4. Lista de docentes con tarjetas nítidas y contraste asegurado
            items(
                items = filtrados,
                key = { comp -> if (comp.id > 0L) comp.id else comp.hashCode() }
            ) { comp ->
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_profesor_${comp.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                val inicial = CsvParser.extraerNombreDePila(comp.nombre).take(1).uppercase()
                                Text(
                                    text = if (inicial.isNotBlank()) inicial else "?",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = comp.nombre,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = comp.email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:${comp.email}")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .testTag("btn_email_${comp.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Enviar email a ${comp.nombre}",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun SeccionImportacionArchivos(
    docenteActivo: String,
    onImportarSeneca: () -> Unit,
    onImportarFet: () -> Unit,
    onImportarClaustro: () -> Unit,
    onVaciarDatos: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 30.dp)
    ) {
        // Tarjeta Informativa: Guía de obtención de archivos (Profesorado, Alumnado y Horarios)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = "¿Cómo obtener tus archivos para la app?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Epígrafe 1: Profesorado / Claustro
                    Text(
                        text = "1. Profesorado y Claustro",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "• Puede facilitarlo el equipo directivo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "• Ruta en Séneca: Personal > Personal de centro > Botón 'Exportar datos' > Formato CSV (RelPerCen.csv).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "• Columnas: En el archivo basta con las columnas 'Empleado/a' y 'Cuenta Google/Microsoft' (o Correo).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Epígrafe 2: Alumnado
                    Text(
                        text = "2. Alumnado",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "• Opción A (Cada docente): Séneca > Alumnado > Mis alumnos y alumnas > Seleccionar curso > Botón exportar datos > Formato CSV (RegMisAlu.csv).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "• Opción B (Centro completo): Archivo general del centro facilitado por el equipo directivo (RegAlum.csv). Al cargarlo podrás seleccionar exactamente tus unidades con casillas de verificación.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Epígrafe 3: Horarios
                    Text(
                        text = "3. Horario Semanal",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "• Archivo en formato .csv generado por el programa de horarios FET (horario.csv).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "• Contiene las columnas Activity Id, Day, Hour, Students Sets, Subject, Teachers y Room. Al cargarlo, podrás seleccionar tu nombre del desplegable de docentes para importar automáticamente todo tu horario.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        item {
            Text(
                text = "Archivos locales del centro",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Selecciona tus archivos descargados en el teléfono para cargar tu horario, alumnado o claustro de forma 100% privada y sin conexión:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Botón 1: Horario FET
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "1. Horario FET (horario.csv)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Selecciona tu nombre del desplegable de 'Teachers'. Fusiona tramos de 30 min.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(onClick = onImportarFet) {
                        Text("Cargar")
                    }
                }
            }
        }

        // Botón 2: Alumnado Séneca
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "2. Alumnado Séneca (RegMisAlu o RegAlum)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Carga por curso o selecciona tus unidades de todo el centro.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(onClick = onImportarSeneca) {
                        Text("Cargar")
                    }
                }
            }
        }

        // Botón 3: Claustro Docente
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "3. Claustro Docente (RelPerCen.csv)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Directorio de compañeros ordenados por nombre con acceso directo a email.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(onClick = onImportarClaustro) {
                        Text("Cargar")
                    }
                }
            }
        }

        // Vaciar base de datos para nuevo centro
        item {
            OutlinedButton(
                onClick = onVaciarDatos,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Limpiar todos los datos (dejar app en blanco)")
            }
        }
    }
}

@Composable
private fun DialogoSelectorUnidades(
    unidades: List<String>,
    unidadesMarcadasInicial: Set<String>,
    todasLasAreas: List<String>,
    asignacionesGuardadas: Map<String, Set<String>>,
    onConfirmar: (Set<String>, Map<String, Set<String>>) -> Unit,
    onDismiss: () -> Unit
) {
    var unidadesMarcadas by remember {
        mutableStateOf(if (unidadesMarcadasInicial.isNotEmpty()) unidadesMarcadasInicial else unidades.toSet())
    }
    var areasPorUnidad by remember {
        mutableStateOf(
            unidades.associateWith { u ->
                val guardadas = asignacionesGuardadas[u]
                if (!guardadas.isNullOrEmpty()) guardadas else todasLasAreas.toSet()
            }.toMutableMap()
        )
    }
    var unidadesExpandidas by remember { mutableStateOf(setOf<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Selecciona Unidades y Áreas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Marca las clases que impartes y despliega para elegir las áreas:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { unidadesMarcadas = unidades.toSet() }) {
                        Text("Todas las unidades", style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(onClick = { unidadesMarcadas = emptySet() }) {
                        Text("Desmarcar todas", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(unidades, key = { it }) { unidad ->
                        val isChecked = unidadesMarcadas.contains(unidad)
                        val isExpandida = unidadesExpandidas.contains(unidad)
                        val areasElegidas = areasPorUnidad[unidad] ?: todasLasAreas.toSet()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isChecked) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                // Fila principal de la unidad
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { chk ->
                                            unidadesMarcadas = if (chk) unidadesMarcadas + unidad else unidadesMarcadas - unidad
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                unidadesMarcadas = if (isChecked) unidadesMarcadas - unidad else unidadesMarcadas + unidad
                                            }
                                    ) {
                                        Text(
                                            text = unidad,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isChecked) "${areasElegidas.size} de ${todasLasAreas.size} áreas" else "No importada",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Botón desplegable de áreas
                                    IconButton(
                                        onClick = {
                                            unidadesExpandidas = if (isExpandida) unidadesExpandidas - unidad else unidadesExpandidas + unidad
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isExpandida) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = "Desplegar áreas de $unidad",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                // Contenido desplegable con las áreas
                                if (isExpandida) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Áreas impartidas en $unidad:",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Row {
                                            TextButton(
                                                onClick = {
                                                    val map = areasPorUnidad.toMutableMap()
                                                    map[unidad] = todasLasAreas.toSet()
                                                    areasPorUnidad = map
                                                },
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                            ) {
                                                Text("Todas", style = MaterialTheme.typography.labelSmall)
                                            }
                                            TextButton(
                                                onClick = {
                                                    val map = areasPorUnidad.toMutableMap()
                                                    map[unidad] = emptySet()
                                                    areasPorUnidad = map
                                                },
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                            ) {
                                                Text("Ninguna", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }

                                    todasLasAreas.chunked(2).forEach { par ->
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            par.forEach { area ->
                                                val areaChecked = areasElegidas.contains(area)
                                                Row(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable {
                                                            val nuevas = areasElegidas.toMutableSet()
                                                            if (areaChecked) nuevas.remove(area) else nuevas.add(area)
                                                            val map = areasPorUnidad.toMutableMap()
                                                            map[unidad] = nuevas
                                                            areasPorUnidad = map
                                                        }
                                                        .padding(vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = areaChecked,
                                                        onCheckedChange = { chk ->
                                                            val nuevas = areasElegidas.toMutableSet()
                                                            if (chk) nuevas.add(area) else nuevas.remove(area)
                                                            val map = areasPorUnidad.toMutableMap()
                                                            map[unidad] = nuevas
                                                            areasPorUnidad = map
                                                        },
                                                        modifier = Modifier.size(32.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(
                                                        text = area,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                            if (par.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmar(unidadesMarcadas, areasPorUnidad)
                },
                enabled = unidadesMarcadas.isNotEmpty()
            ) {
                Text("Importar (${unidadesMarcadas.size} unidades)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoSelectorDocenteFet(
    docentes: List<String>,
    docenteInicial: String,
    onConfirmar: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var seleccionado by remember {
        mutableStateOf(
            docentes.firstOrNull { it.equals(docenteInicial, ignoreCase = true) } ?: docentes.firstOrNull() ?: ""
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("¿Quién eres en este horario FET?")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Selecciona tu perfil de la columna 'Teachers' para extraer tus clases y tramos horarios:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = seleccionado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Profesor / Maestro") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        docentes.forEach { doc ->
                            DropdownMenuItem(
                                text = { Text(doc, fontWeight = if (doc == seleccionado) FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    seleccionado = doc
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmar(seleccionado) },
                enabled = seleccionado.isNotBlank()
            ) {
                Text("Importar mi horario")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun MetricaCard(
    titulo: String,
    valor: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
