package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.example.data.model.Alumno
import com.example.data.model.RegistroConducta
import com.example.ui.DocenteUiState
import com.example.ui.components.AlumnoDetalleDialog

@Composable
fun AlumnosScreen(
    uiState: DocenteUiState,
    alumnos: List<Alumno>,
    unidades: List<String>,
    registrosAlumnoDetalle: List<RegistroConducta> = emptyList(),
    totalPositivosCurso: Int = 0,
    totalNegativosCurso: Int = 0,
    onSeleccionarUnidad: (String) -> Unit,
    onBusquedaChanged: (String) -> Unit,
    onAbrirDetalleAlumno: (Alumno) -> Unit,
    onCerrarDetalleAlumno: () -> Unit,
    onSumarPositivo: (Alumno) -> Unit,
    onSumarNegativo: (Alumno) -> Unit,
    onAbrirNota: (Alumno) -> Unit,
    onGuardarNota: (Alumno, String) -> Unit,
    onCerrarNota: () -> Unit,
    onEliminarRegistro: (RegistroConducta) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("pantalla_alumnos")
    ) {
        // Cabecera con selector de unidad y buscador
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                // Buscador de alumno
                OutlinedTextField(
                    value = uiState.busquedaAlumnos,
                    onValueChange = onBusquedaChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_buscar_alumnos"),
                    placeholder = { Text("Buscar alumno por nombre o unidad...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Buscar", tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        if (uiState.busquedaAlumnos.isNotBlank()) {
                            IconButton(onClick = { onBusquedaChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Selector horizontal de unidades escolares Séneca
                Text(
                    text = "Unidad / Clase:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.unidadSeleccionada.isBlank(),
                            onClick = { onSeleccionarUnidad("") },
                            label = { Text("Todas (${alumnos.size})") },
                            modifier = Modifier.testTag("chip_unidad_todas")
                        )
                    }

                    items(unidades) { unidad ->
                        val isSelected = uiState.unidadSeleccionada == unidad
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSeleccionarUnidad(unidad) },
                            label = { Text(unidad) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("chip_unidad_$unidad")
                        )
                    }
                }
            }
        }

        // Subtítulo con resumen de la clase actual
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (uiState.unidadSeleccionada.isBlank()) "Mostrando todo el alumnado"
                else "Alumnado de ${uiState.unidadSeleccionada}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "${alumnos.size} alumnos",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Lista de alumnos
        if (alumnos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "No se encontraron alumnos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Prueba con otra unidad o importa el archivo RegAlum.csv de Séneca en Resumen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("lista_alumnos"),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(alumnos, key = { it.id }) { alumno ->
                    AlumnoCard(
                        alumno = alumno,
                        onClickCard = { onAbrirDetalleAlumno(alumno) },
                        onSumarPositivo = { onSumarPositivo(alumno) },
                        onSumarNegativo = { onSumarNegativo(alumno) },
                        onAbrirNota = { onAbrirNota(alumno) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    // Diálogo para añadir observación o nota rápida al alumno
    uiState.alumnoParaObservacion?.let { alumno ->
        NotaObservacionDialog(
            alumno = alumno,
            onDismiss = onCerrarNota,
            onGuardar = { texto -> onGuardarNota(alumno, texto) }
        )
    }

    // Ficha modal de detalle de conducta y observaciones por alumno
    uiState.alumnoDetalle?.let { alumno ->
        AlumnoDetalleDialog(
            alumno = alumno,
            registros = registrosAlumnoDetalle,
            totalPositivosCurso = totalPositivosCurso,
            totalNegativosCurso = totalNegativosCurso,
            onSumarPositivo = { onSumarPositivo(alumno) },
            onSumarNegativo = { onSumarNegativo(alumno) },
            onAbrirNota = { onAbrirNota(alumno) },
            onEliminarRegistro = onEliminarRegistro,
            onDismiss = onCerrarDetalleAlumno
        )
    }
}

@Composable
private fun AlumnoCard(
    alumno: Alumno,
    onClickCard: () -> Unit,
    onSumarPositivo: () -> Unit,
    onSumarNegativo: () -> Unit,
    onAbrirNota: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClickCard)
            .testTag("card_alumno_${alumno.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Iniciales + Datos del Alumno
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar con iniciales
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    val iniciales = "${alumno.primerApellido.take(1)}${alumno.nombre.take(1)}".uppercase()
                    Text(
                        text = if (iniciales.isNotBlank()) iniciales else "?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Column {
                    Text(
                        text = alumno.nombreCompleto,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "Unidad: ${alumno.unidad}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Botones de Caritas: Verde (+1), Roja (-1) y Anotación rápida
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Carita Verde (Positivo)
                IconButton(
                    onClick = onSumarPositivo,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5E9))
                        .testTag("btn_positivo_${alumno.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.SentimentSatisfiedAlt,
                        contentDescription = "Positivo verde",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Carita Roja (Negativo)
                IconButton(
                    onClick = onSumarNegativo,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE))
                        .testTag("btn_negativo_${alumno.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.SentimentDissatisfied,
                        contentDescription = "Negativo rojo",
                        tint = Color(0xFFC62828),
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Botón de Observación / Nota rápida
                IconButton(
                    onClick = onAbrirNota,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("btn_nota_${alumno.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.NoteAdd,
                        contentDescription = "Añadir nota u observación",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NotaObservacionDialog(
    alumno: Alumno,
    onDismiss: () -> Unit,
    onGuardar: (String) -> Unit
) {
    var texto by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Anotación de conducta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Alumno: ${alumno.nombreCompleto} (${alumno.unidad})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    label = { Text("Escribe la observación...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_nota_observacion"),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (texto.isNotBlank()) {
                        onGuardar(texto.trim())
                    }
                },
                enabled = texto.isNotBlank(),
                modifier = Modifier.testTag("btn_guardar_nota_dialog")
            ) {
                Text("Guardar Anotación")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
