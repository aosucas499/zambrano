package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Alumno
import com.example.data.model.RegistroConducta
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumnoDetalleDialog(
    alumno: Alumno,
    registros: List<RegistroConducta>,
    totalPositivosCurso: Int,
    totalNegativosCurso: Int,
    onSumarPositivo: () -> Unit,
    onSumarNegativo: () -> Unit,
    onAbrirNota: () -> Unit,
    onEliminarRegistro: (RegistroConducta) -> Unit,
    onDismiss: () -> Unit
) {
    // Agrupar registros cronológicamente por fecha (los días más recientes primero)
    val registrosPorFecha = remember(registros) {
        registros.groupBy { it.fecha }
            .toList()
            .sortedByDescending { it.first }
    }

    val balanceCurso = totalPositivosCurso - totalNegativosCurso

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 580.dp)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("dialog_detalle_alumno_${alumno.id}"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Cabecera: Avatar, Nombre, Unidad y Botón Cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            val iniciales = "${alumno.primerApellido.take(1)}${alumno.nombre.take(1)}".uppercase()
                            Text(
                                text = if (iniciales.isNotBlank()) iniciales else "?",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Column {
                            Text(
                                text = alumno.nombreCompleto,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "Unidad: ${alumno.unidad}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_cerrar_detalle_alumno")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Resumen Global del Curso: 3 tarjetas métricas
                Text(
                    text = "Balance global del curso",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Positivos totales
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SentimentSatisfiedAlt,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "+$totalPositivosCurso",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                            Text(
                                text = "Positivas",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF1B5E20)
                            )
                        }
                    }

                    // Negativos totales
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFEBEE)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SentimentDissatisfied,
                                    contentDescription = null,
                                    tint = Color(0xFFC62828),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "-$totalNegativosCurso",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFC62828)
                                )
                            }
                            Text(
                                text = "Negativas",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFB71C1C)
                            )
                        }
                    }

                    // Balance Neto
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (balanceCurso > 0) "+$balanceCurso" else "$balanceCurso",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (balanceCurso > 0) Color(0xFF2E7D32)
                                else if (balanceCurso < 0) Color(0xFFC62828)
                                else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Neto",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Barra de acciones rápidas sobre el alumno
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Añadir registro hoy:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Sumar positivo
                            IconButton(
                                onClick = onSumarPositivo,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8F5E9))
                                    .testTag("btn_detalle_sumar_positivo")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SentimentSatisfiedAlt,
                                    contentDescription = "Sumar carita positiva",
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Sumar negativo
                            IconButton(
                                onClick = onSumarNegativo,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFEBEE))
                                    .testTag("btn_detalle_sumar_negativo")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SentimentDissatisfied,
                                    contentDescription = "Sumar carita negativa",
                                    tint = Color(0xFFC62828),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Añadir observación
                            IconButton(
                                onClick = onAbrirNota,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .testTag("btn_detalle_anotar_nota")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EventNote,
                                    contentDescription = "Añadir observación",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Historial agrupado por días (${registros.size} anotaciones)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Lista de días e historial
                if (registrosPorFecha.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "Sin incidencias ni observaciones",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Usa los botones superiores para registrar caritas u observaciones.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .testTag("lista_historial_conducta_alumno"),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        registrosPorFecha.forEach { (fechaStr, itemsDelDia) ->
                            item(key = "header_$fechaStr") {
                                DiaHeaderCard(fechaIso = fechaStr, itemsDelDia = itemsDelDia)
                            }

                            items(itemsDelDia, key = { it.id }) { registro ->
                                RegistroConductaItem(
                                    registro = registro,
                                    onEliminar = { onEliminarRegistro(registro) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaHeaderCard(fechaIso: String, itemsDelDia: List<RegistroConducta>) {
    val fechaFormateada = remember(fechaIso) {
        try {
            val ld = LocalDate.parse(fechaIso)
            val diaSemana = ld.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale("es", "ES"))
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            val resto = ld.format(DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy", Locale("es", "ES")))
            "$diaSemana, $resto"
        } catch (_: Exception) {
            fechaIso
        }
    }

    val posDia = itemsDelDia.count { it.tipo == "POSITIVO" }
    val negDia = itemsDelDia.count { it.tipo == "NEGATIVO" }
    val obsDia = itemsDelDia.count { it.tipo == "OBSERVACION" }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = fechaFormateada,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (posDia > 0) {
                    Text(
                        text = "+$posDia 😊",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
                if (negDia > 0) {
                    Text(
                        text = "-$negDia 😟",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                    )
                }
                if (obsDia > 0) {
                    Text(
                        text = "$obsDia 📝",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun RegistroConductaItem(
    registro: RegistroConducta,
    onEliminar: () -> Unit
) {
    val esPositivo = registro.tipo == "POSITIVO"
    val esNegativo = registro.tipo == "NEGATIVO"

    val containerColor = when {
        esPositivo -> Color(0xFFF1F8E9)
        esNegativo -> Color(0xFFFFEBEE)
        else -> MaterialTheme.colorScheme.surface
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Icono según tipo
                when {
                    esPositivo -> Icon(
                        imageVector = Icons.Default.SentimentSatisfiedAlt,
                        contentDescription = "Positivo",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(24.dp)
                    )
                    esNegativo -> Icon(
                        imageVector = Icons.Default.SentimentDissatisfied,
                        contentDescription = "Negativo",
                        tint = Color(0xFFC62828),
                        modifier = Modifier.size(24.dp)
                    )
                    else -> Icon(
                        imageVector = Icons.Default.EventNote,
                        contentDescription = "Observación",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = when {
                                esPositivo -> "Carita Positiva (+1)"
                                esNegativo -> "Carita Negativa (-1)"
                                else -> "Observación"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                esPositivo -> Color(0xFF2E7D32)
                                esNegativo -> Color(0xFFC62828)
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                        if (registro.hora.isNotBlank()) {
                            Text(
                                text = "• ${registro.hora}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (registro.observacion.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = registro.observacion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Botón para eliminar apunte erróneo
            IconButton(
                onClick = onEliminar,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("btn_borrar_registro_${registro.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Eliminar apunte",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
