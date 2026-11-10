package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ActividadEvaluacion
import com.example.data.model.Alumno
import com.example.data.model.CriterioEvaluacion
import com.example.data.model.NotaEvaluacion
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeccionEvaluacion(
    context: Context,
    modo: String, // "SDA" o "GLOBAL"
    unidadSda: String, // "SA1" .. "SA12"
    cursoSeleccionado: String,
    areaSeleccionada: String,
    cursosDisponibles: List<String>,
    areasDisponibles: List<String>,
    criterios: List<CriterioEvaluacion>,
    actividades: List<ActividadEvaluacion>,
    notas: List<NotaEvaluacion>,
    alumnos: List<Alumno>,
    onCambiarModo: (String) -> Unit,
    onCambiarUnidadSda: (String) -> Unit,
    onCambiarCurso: (String) -> Unit,
    onCambiarArea: (String) -> Unit,
    onAbrirNuevaActividad: () -> Unit,
    onEliminarActividad: (Long, String) -> Unit,
    onCalificarAlumno: (Alumno, ActividadEvaluacion) -> Unit,
    onExportarSeneca: () -> Unit,
    onExportarPdfGlobal: () -> Unit,
    onAbrirGestionActividades: () -> Unit,
    onIrAImportarArchivos: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedCurso by remember { mutableStateOf(false) }
    var expandedArea by remember { mutableStateOf(false) }
    var expandedSda by remember { mutableStateOf(false) }

    val listaSdas = remember {
        listOf(
            "SA1" to "SA 1", "SA2" to "SA 2", "SA3" to "SA 3", "SA4" to "SA 4",
            "SA5" to "SA 5", "SA6" to "SA 6", "SA7" to "SA 7", "SA8" to "SA 8",
            "SA9" to "SA 9", "SA10" to "SA 10", "SA11" to "SA 11", "SA12" to "SA 12"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("seccion_evaluacion")
    ) {
        // --- 1. PESTAÑAS PRINCIPALES: SÁBANA GLOBAL vs SITUACIONES DE APRENDIZAJE ---
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            androidx.compose.material3.TabRow(
                selectedTabIndex = if (modo == "GLOBAL") 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                androidx.compose.material3.Tab(
                    selected = modo == "GLOBAL",
                    onClick = { onCambiarModo("GLOBAL") },
                    text = { Text("📊 Sábana Global (Criterios)", fontWeight = FontWeight.Bold) }
                )
                androidx.compose.material3.Tab(
                    selected = modo == "SDA",
                    onClick = { onCambiarModo("SDA") },
                    text = { Text("📂 Por SdAs (Actividades)", fontWeight = FontWeight.Bold) }
                )
            }
        }

        // --- 2. BARRA DE FILTROS Y CONTROLES SEGÚN EL MODO SELECCIONADO ---
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Selector Grupo / Curso
                    ExposedDropdownMenuBox(
                        expanded = expandedCurso,
                        onExpandedChange = { expandedCurso = !expandedCurso },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = cursoSeleccionado.ifBlank { cursosDisponibles.firstOrNull() ?: "Grupo" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Grupo / Unidad") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCurso) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCurso,
                            onDismissRequest = { expandedCurso = false }
                        ) {
                            if (cursosDisponibles.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Sin grupos importados") },
                                    onClick = { expandedCurso = false }
                                )
                            } else {
                                cursosDisponibles.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c, fontWeight = if (c == cursoSeleccionado) FontWeight.Bold else FontWeight.Normal) },
                                        onClick = {
                                            onCambiarCurso(c)
                                            expandedCurso = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Selector Área
                    ExposedDropdownMenuBox(
                        expanded = expandedArea,
                        onExpandedChange = { expandedArea = !expandedArea },
                        modifier = Modifier.weight(1.2f)
                    ) {
                        OutlinedTextField(
                            value = areaSeleccionada.ifBlank { areasDisponibles.firstOrNull() ?: "Área" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Área (LOMLOE)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedArea) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedArea,
                            onDismissRequest = { expandedArea = false }
                        ) {
                            areasDisponibles.forEach { a ->
                                DropdownMenuItem(
                                    text = { Text(a, fontWeight = if (a == areaSeleccionada) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        onCambiarArea(a)
                                        expandedArea = false
                                    }
                                )
                            }
                        }
                    }

                    // Si estamos en modo SDA, mostrar el selector de SdA (SA1 .. SA12)
                    if (modo == "SDA") {
                        ExposedDropdownMenuBox(
                            expanded = expandedSda,
                            onExpandedChange = { expandedSda = !expandedSda },
                            modifier = Modifier.weight(0.9f)
                        ) {
                            OutlinedTextField(
                                value = unidadSda,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("SdA") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSda) },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedSda,
                                onDismissRequest = { expandedSda = false }
                            ) {
                                listaSdas.forEach { (clave, etiqueta) ->
                                    DropdownMenuItem(
                                        text = { Text(etiqueta) },
                                        onClick = {
                                            onCambiarUnidadSda(clave)
                                            expandedSda = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Botón Rueda de Gestión de Actividades
                    IconButton(
                        onClick = onAbrirGestionActividades,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Gestión de Actividades",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Barra de Acciones Dedicada
                if (modo == "GLOBAL") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sábana Global de Criterios y Medias",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Grupo: ${cursoSeleccionado.ifBlank { "Todos" }} · Área: $areaSeleccionada (${alumnos.size} alumnos)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Button(
                                onClick = onExportarPdfGlobal,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("btn_informe_global_pdf")
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Informe PDF", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onAbrirNuevaActividad,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("btn_nueva_actividad")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nueva Actividad en $unidadSda", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        Button(
                            onClick = onExportarSeneca,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier
                                .weight(0.9f)
                                .testTag("btn_exportar_seneca")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copiar Séneca", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        // --- 3. CONTENIDO DE LA TABLA: SÁBANA GLOBAL O SDA ---
        if (cursosDisponibles.isEmpty() && alumnos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Importar Alumnado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Para poder ver la sábana de evaluación y calificar criterios, importa tu archivo de alumnado de Séneca (RegAlum.csv o RegMisAlu.csv) en 'Importar Archivos'.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onIrAImportarArchivos) {
                            Text("Ir a Importar Archivos")
                        }
                    }
                }
            }
        } else if (modo == "SDA") {
            TablaSdaMatriz(
                unidadSda = unidadSda,
                alumnos = alumnos,
                actividades = actividades,
                notas = notas,
                onEliminarActividad = onEliminarActividad,
                onCalificarAlumno = onCalificarAlumno,
                onAbrirNuevaActividad = onAbrirNuevaActividad
            )
        } else {
            // Sábana Global siempre visible y calculada dinámicamente
            TablaGlobalMatriz(
                area = areaSeleccionada,
                curso = cursoSeleccionado,
                alumnos = alumnos,
                criterios = criterios,
                notas = notas
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TablaSdaMatriz(
    unidadSda: String,
    alumnos: List<Alumno>,
    actividades: List<ActividadEvaluacion>,
    notas: List<NotaEvaluacion>,
    onEliminarActividad: (Long, String) -> Unit,
    onCalificarAlumno: (Alumno, ActividadEvaluacion) -> Unit,
    onAbrirNuevaActividad: () -> Unit
) {
    if (actividades.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Sin actividades creadas en $unidadSda",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Pulsa el botón superior para crear una actividad y asignarle criterios oficiales.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = onAbrirNuevaActividad) {
                    Text("+ Crear Actividad en $unidadSda")
                }
            }
        }
    } else {
        val horizontalScrollState = rememberScrollState()

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // Cabecera pegajosa (Sticky Header)
            stickyHeader {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(Color(0xFF1E293B)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Columna Fija: Alumnado
                    Box(
                        modifier = Modifier
                            .width(155.dp)
                            .fillMaxHeight()
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "Alumnado (${alumnos.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Divisor vertical
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(Color(0xFF475569))
                    )

                    // Celdas de cabecera de actividades con scroll horizontal
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .horizontalScroll(horizontalScrollState),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        actividades.forEach { act ->
                            Row(
                                modifier = Modifier
                                    .width(130.dp)
                                    .fillMaxHeight()
                                    .border(0.5.dp, Color(0xFF334155))
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = act.nombre,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onEliminarActividad(act.id, act.nombre) },
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Borrar actividad",
                                        tint = Color(0xFFFF6B6B),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Filas de alumnado
            itemsIndexed(alumnos, key = { _, al -> al.id }) { index, al ->
                val fondoFila = if (index % 2 == 1) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                else MaterialTheme.colorScheme.surface

                val nombreSeneca = when {
                    al.primerApellido.isNotBlank() -> {
                        val apellidos = if (al.segundoApellido.isNotBlank()) "${al.primerApellido} ${al.segundoApellido}" else al.primerApellido
                        "$apellidos, ${al.nombre}".trim()
                    }
                    else -> al.nombreCompleto
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .background(fondoFila)
                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Columna Fija: Alumno
                    Surface(
                        modifier = Modifier
                            .width(155.dp)
                            .fillMaxHeight(),
                        color = fondoFila
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}. ",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = nombreSeneca,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Divisor vertical
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )

                    // Celdas de calificaciones por actividad (scroll horizontal sincronizado)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .horizontalScroll(horizontalScrollState),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        actividades.forEach { act ->
                            val notasAct = notas.filter { n ->
                                n.alumnoNombre.equals(al.nombreCompleto, ignoreCase = true) && n.actividadId == act.id
                            }
                            val valores = notasAct.mapNotNull { it.valor }

                            Box(
                                modifier = Modifier
                                    .width(130.dp)
                                    .fillMaxHeight()
                                    .clickable { onCalificarAlumno(al, act) }
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (valores.isNotEmpty()) {
                                    val mediaAct = valores.average()
                                    val colorBg = if (mediaAct >= 5.0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                    val colorTxt = if (mediaAct >= 5.0) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                                    val texto = valores.joinToString(" · ") { String.format(Locale.US, "%.1f", it) }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = colorBg,
                                        modifier = Modifier.padding(2.dp)
                                    ) {
                                        Text(
                                            text = texto,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = colorTxt,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "—",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TablaGlobalMatriz(
    area: String,
    curso: String,
    alumnos: List<Alumno>,
    criterios: List<CriterioEvaluacion>,
    notas: List<NotaEvaluacion>
) {
    if (alumnos.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (curso.isNotBlank()) "No hay alumnado registrado en el grupo $curso" else "Selecciona un grupo para ver la sábana de evaluación",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    if (criterios.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No se encontraron criterios de evaluación para el área de $area",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val horizontalScrollState = rememberScrollState()

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Sticky Header de la tabla
        stickyHeader {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(Color(0xFF1E293B)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Columna Fija: Alumnado
                Box(
                    modifier = Modifier
                        .width(155.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Alumnado (${alumnos.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Divisor vertical
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF475569))
                )

                // Celdas de cabecera: Criterios + Media Área
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .horizontalScroll(horizontalScrollState),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    criterios.forEach { c ->
                        Column(
                            modifier = Modifier
                                .width(64.dp)
                                .fillMaxHeight()
                                .border(0.5.dp, Color(0xFF334155))
                                .padding(horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = c.codigo,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Text(
                                text = "CE ${c.ceNumero}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = androidx.compose.ui.unit.TextUnit(9f, androidx.compose.ui.unit.TextUnitType.Sp)),
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }

                    // Columna Media Área
                    Box(
                        modifier = Modifier
                            .width(90.dp)
                            .fillMaxHeight()
                            .background(Color(0xFF059669))
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "MEDIA ÁREA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Filas de alumnado
        itemsIndexed(alumnos, key = { _, al -> al.id }) { index, al ->
            val fondoFila = if (index % 2 == 1) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surface

            val nombreSeneca = when {
                al.primerApellido.isNotBlank() -> {
                    val apellidos = if (al.segundoApellido.isNotBlank()) "${al.primerApellido} ${al.segundoApellido}" else al.primerApellido
                    "$apellidos, ${al.nombre}".trim()
                }
                else -> al.nombreCompleto
            }

            // Cálculo directo de la media de cada criterio evaluado
            val mediasPorCrit = mutableMapOf<String, Double>()
            criterios.forEach { c ->
                val notasHist = notas.filter { n ->
                    n.alumnoNombre.equals(al.nombreCompleto, ignoreCase = true) && n.criterioCodigo == c.codigo && n.valor != null
                }
                if (notasHist.isNotEmpty()) {
                    mediasPorCrit[c.codigo] = notasHist.mapNotNull { it.valor }.average()
                }
            }
            // Media global de todos los criterios evaluados
            val mediaGlobal = if (mediasPorCrit.isNotEmpty()) mediasPorCrit.values.average() else null

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .background(fondoFila)
                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Columna Fija: Nombre del alumno
                Surface(
                    modifier = Modifier
                        .width(155.dp)
                        .fillMaxHeight(),
                    color = fondoFila
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}. ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = nombreSeneca,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Divisor vertical
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )

                // Celdas deslizables horizontalmente
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .horizontalScroll(horizontalScrollState),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    criterios.forEach { c ->
                        val mediaC = mediasPorCrit[c.codigo]
                        Box(
                            modifier = Modifier
                                .width(64.dp)
                                .fillMaxHeight()
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (mediaC != null) {
                                val colorBg = if (mediaC >= 5.0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                val colorTxt = if (mediaC >= 5.0) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = colorBg,
                                    modifier = Modifier.padding(2.dp)
                                ) {
                                    Text(
                                        text = String.format(Locale.US, "%.1f", mediaC),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = colorTxt,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = "—",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                )
                            }
                        }
                    }

                    // Celda Media Área Global
                    Box(
                        modifier = Modifier
                            .width(90.dp)
                            .fillMaxHeight()
                            .background(
                                when {
                                    mediaGlobal == null -> Color.Transparent
                                    mediaGlobal >= 5.0 -> Color(0xFFD1FAE5)
                                    else -> Color(0xFFFEE2E2)
                                }
                            )
                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (mediaGlobal != null) String.format(Locale.US, "%.2f", mediaGlobal) else "—",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                mediaGlobal == null -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                mediaGlobal >= 5.0 -> Color(0xFF065F46)
                                else -> Color(0xFF991B1B)
                            }
                        )
                    }
                }
            }
        }
    }
}
