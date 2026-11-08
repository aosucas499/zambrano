package com.example.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DiarioClase
import com.example.ui.DocenteUiState
import com.example.ui.theme.NoteYellow
import com.example.ui.theme.NoteYellowBorder
import com.example.ui.theme.ObsRed
import com.example.ui.theme.ObsRedContainer
import com.example.util.DateUtils
import java.time.LocalDate

@Composable
fun DiarioScreen(
    uiState: DocenteUiState,
    clases: List<DiarioClase>,
    onFechaSeleccionada: (LocalDate) -> Unit,
    onDiaAnterior: () -> Unit,
    onDiaSiguiente: () -> Unit,
    onIrAHoy: () -> Unit,
    onIrAlLunes: () -> Unit,
    onEditarClase: (DiarioClase) -> Unit,
    onToggleCompletada: (DiarioClase) -> Unit,
    onCompartirWhatsApp: () -> Unit,
    onExportarPdf: () -> Unit,
    onIrAAlumnosUnidad: (String) -> Unit,
    onIrAHorarioTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fechaActual = uiState.fechaSeleccionada

    val datePickerDialog = rememberDatePicker(
        context = context,
        fechaInicial = fechaActual,
        onFechaElegida = onFechaSeleccionada
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("pantalla_diario")
    ) {
        // Barra superior de navegación de fechas
        NavegadorFechaCard(
            fecha = fechaActual,
            docenteActivo = uiState.docenteActivo,
            onDiaAnterior = onDiaAnterior,
            onDiaSiguiente = onDiaSiguiente,
            onIrAHoy = onIrAHoy,
            onAbrirCalendario = { datePickerDialog.show() },
            onCompartirWhatsApp = onCompartirWhatsApp,
            onExportarPdf = onExportarPdf
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (uiState.esFinDeSemana) {
            TarjetaFinDeSemana(
                fecha = fechaActual,
                onIrAlLunes = onIrAlLunes
            )
        } else if (clases.isEmpty()) {
            TarjetaSinClases(
                docente = uiState.docenteActivo,
                onIrAHorario = onIrAHorarioTab
            )
        } else {
            // Lista de clases
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("lista_clases_diario"),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(clases, key = { it.id }) { clase ->
                    ClaseCard(
                        clase = clase,
                        onEdit = { onEditarClase(clase) },
                        onToggleDone = { onToggleCompletada(clase) },
                        onVerAlumnos = { onIrAAlumnosUnidad(clase.grupo) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun NavegadorFechaCard(
    fecha: LocalDate,
    docenteActivo: String,
    onDiaAnterior: () -> Unit,
    onDiaSiguiente: () -> Unit,
    onIrAHoy: () -> Unit,
    onAbrirCalendario: () -> Unit,
    onCompartirWhatsApp: () -> Unit,
    onExportarPdf: () -> Unit
) {
    val esHoy = fecha == LocalDate.now()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onDiaAnterior,
                    modifier = Modifier.testTag("btn_dia_anterior")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Día anterior",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onAbrirCalendario() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Elegir fecha",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = fecha.format(DateUtils.displayFormatter),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onDiaSiguiente,
                    modifier = Modifier.testTag("btn_dia_siguiente")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Día siguiente",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${DateUtils.formatoFechaLarga(fecha)} • Docente: $docenteActivo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fila de acciones rápidas (Hoy, WhatsApp, PDF)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!esHoy) {
                    OutlinedButton(
                        onClick = onIrAHoy,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_ir_a_hoy"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hoy", style = MaterialTheme.typography.labelMedium)
                    }
                }

                // Botón WhatsApp con verde oficial
                Button(
                    onClick = onCompartirWhatsApp,
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("btn_compartir_whatsapp"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                // Botón PDF
                Button(
                    onClick = onExportarPdf,
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("btn_exportar_pdf"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ClaseCard(
    clase: DiarioClase,
    onEdit: () -> Unit,
    onToggleDone: () -> Unit,
    onVerAlumnos: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_clase_${clase.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Barra decorativa izquierda
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(150.dp)
                    .background(
                        if (clase.completada) Color(0xFF4CAF50)
                        else MaterialTheme.colorScheme.primary
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Cabecera: Hora + Estado + Botón Editar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⏰ ${clase.hora}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onToggleDone,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_toggle_done_${clase.id}")
                        ) {
                            Icon(
                                imageVector = if (clase.completada) Icons.Default.CheckCircle
                                else Icons.Default.RadioButtonUnchecked,
                                contentDescription = if (clase.completada) "Completada" else "Pendiente",
                                tint = if (clase.completada) Color(0xFF388E3C)
                                else MaterialTheme.colorScheme.outline
                            )
                        }

                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_edit_clase_${clase.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar actividad",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Grupo y Materia + Botón para saltar a Alumnado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "👥 ${clase.grupo}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    // Botón para acceder directamente a la unidad de alumnado
                    FilledTonalButton(
                        onClick = onVerAlumnos,
                        modifier = Modifier.testTag("btn_ver_alumnos_${clase.id}"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Alumnado", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cuadro de Actividad (Post-it amarillo)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NoteYellow)
                        .border(1.dp, NoteYellowBorder, RoundedCornerShape(10.dp))
                        .clickable { onEdit() }
                        .padding(12.dp)
                        .testTag("caja_actividad_${clase.id}")
                ) {
                    if (clase.actividad.isNotBlank()) {
                        Text(
                            text = clase.actividad,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF263238)
                        )
                    } else {
                        Text(
                            text = "Toca para programar actividad didáctica...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFF78909C)
                        )
                    }
                }

                // Observaciones
                if (clase.observaciones.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ObsRedContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEdit() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = null,
                                tint = ObsRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = clase.observaciones,
                                style = MaterialTheme.typography.bodySmall,
                                color = ObsRed,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaFinDeSemana(
    fecha: LocalDate,
    onIrAlLunes: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("tarjeta_fin_de_semana"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Weekend,
                contentDescription = null,
                modifier = Modifier.size(54.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "¡Es fin de semana!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "No hay clases programadas para hoy. Tómate un merecido descanso o adelanta la planificación del lunes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Button(
                onClick = onIrAlLunes,
                modifier = Modifier.testTag("btn_planificar_lunes")
            ) {
                Icon(imageVector = Icons.Default.EditCalendar, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Planificar el próximo lunes")
            }
        }
    }
}

@Composable
private fun TarjetaSinClases(
    docente: String,
    onIrAHorario: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("tarjeta_sin_clases"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Sin clases programadas para $docente",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "No hay tramos horarios configurados para este día de la semana. Puedes comprobar o ajustar el horario del docente en la rueda de ajustes ⚙️.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilledTonalButton(
                onClick = onIrAHorario,
                modifier = Modifier.testTag("btn_ir_a_configurar_horario")
            ) {
                Text("Ver y configurar horario")
            }
        }
    }
}

@Composable
private fun rememberDatePicker(
    context: android.content.Context,
    fechaInicial: LocalDate,
    onFechaElegida: (LocalDate) -> Unit
): DatePickerDialog {
    return DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onFechaElegida(LocalDate.of(year, month + 1, dayOfMonth))
        },
        fechaInicial.year,
        fechaInicial.monthValue - 1,
        fechaInicial.dayOfMonth
    )
}
