package com.example.ui.components

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.HorarioSlot
import com.example.util.DateUtils

@Composable
fun EditHorarioSlotDialog(
    slot: HorarioSlot,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (HorarioSlot) -> Unit,
    onDelete: (HorarioSlot) -> Unit
) {
    var diaSemana by remember { mutableIntStateOf(slot.diaSemana) }
    var horaInicio by remember { mutableStateOf(slot.horaInicio) }
    var horaFin by remember { mutableStateOf(slot.horaFin) }
    var grupo by remember { mutableStateOf(slot.grupo) }
    var asignatura by remember { mutableStateOf(slot.asignatura) }
    var aula by remember { mutableStateOf(slot.aula) }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    val dias = listOf(1 to "Lun", 2 to "Mar", 3 to "Mié", 4 to "Jue", 5 to "Vie")

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dialog_edit_slot"),
        title = {
            Text(
                text = if (isNew) "Añadir tramo al horario" else "Modificar tramo horario",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Día de la semana:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dias.forEach { (num, nombre) ->
                        FilterChip(
                            selected = diaSemana == num,
                            onClick = { diaSemana = num },
                            label = { Text(nombre) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = horaInicio,
                        onValueChange = { horaInicio = it },
                        label = { Text("Hora Inicio") },
                        placeholder = { Text("08:15") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_hora_inicio"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = horaFin,
                        onValueChange = { horaFin = it },
                        label = { Text("Hora Fin") },
                        placeholder = { Text("09:15") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_hora_fin"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = grupo,
                    onValueChange = { grupo = it },
                    label = { Text("Grupo / Curso") },
                    placeholder = { Text("Ej. 2º ESO B o Tutoría") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_grupo"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = asignatura,
                    onValueChange = { asignatura = it },
                    label = { Text("Asignatura / Función") },
                    placeholder = { Text("Ej. Matemáticas o Guardia") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_asignatura"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = aula,
                    onValueChange = { aula = it },
                    label = { Text("Aula / Espacio (Opcional)") },
                    placeholder = { Text("Ej. Aula 104 o Lab. Física") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_aula"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (horaInicio.isBlank()) {
                        errorMsg = "Introduce la hora de inicio (ej. 08:15)"
                        return@Button
                    }
                    if (grupo.isBlank() && asignatura.isBlank()) {
                        errorMsg = "Introduce al menos el grupo o la asignatura"
                        return@Button
                    }
                    onSave(
                        slot.copy(
                            diaSemana = diaSemana,
                            horaInicio = horaInicio.trim(),
                            horaFin = horaFin.trim(),
                            grupo = grupo.trim(),
                            asignatura = asignatura.trim(),
                            aula = aula.trim()
                        )
                    )
                },
                modifier = Modifier.testTag("btn_guardar_slot")
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Guardar")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (!isNew) {
                    OutlinedButton(
                        onClick = { onDelete(slot) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.testTag("btn_eliminar_slot")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar")
                    }
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("btn_cancelar_slot")
                ) {
                    Text("Cancelar")
                }
            }
        }
    )
}
