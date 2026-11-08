package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DiarioClase

@Composable
fun EditClaseDialog(
    clase: DiarioClase,
    onDismiss: () -> Unit,
    onSave: (id: Long, actividad: String, observaciones: String) -> Unit
) {
    var actividad by remember { mutableStateOf(clase.actividad) }
    var observaciones by remember { mutableStateOf(clase.observaciones) }

    val sugerenciasRapidas = listOf(
        "Examen / Control",
        "Corrección de ejercicios",
        "Explicación nuevo tema",
        "Trabajo cooperativo",
        "Práctica de problemas",
        "Lectura guiada",
        "Repaso trimestral"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dialog_edit_clase"),
        title = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = clase.grupo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${clase.fechaVisible} • ${clase.hora}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Sugerencias rápidas:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sugerenciasRapidas.forEach { sug ->
                        AssistChip(
                            onClick = {
                                actividad = if (actividad.isBlank()) sug else "$actividad\n$sug"
                            },
                            label = { Text(sug, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = actividad,
                    onValueChange = { actividad = it },
                    label = { Text("Actividad planificada / Lección") },
                    placeholder = { Text("Ej. Tema 4: Fracciones algebraicas y ejercicios 1 a 6 pág 82...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("input_edit_actividad"),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 4,
                    maxLines = 8
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones / Tarea / Incidencias") },
                    placeholder = { Text("Ej. Traer compás el jueves; Pedir justificante a Juan...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("input_edit_observaciones"),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(clase.id, actividad, observaciones) },
                modifier = Modifier.testTag("btn_guardar_edicion")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancelar_edicion")
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cancelar")
            }
        }
    )
}
