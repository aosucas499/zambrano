package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.ActividadEvaluacion
import com.example.data.model.Alumno
import com.example.data.model.CriterioEvaluacion
import com.example.data.model.NotaEvaluacion
import org.json.JSONArray
import java.util.Locale

@Composable
fun CalificarAlumnoDialog(
    alumno: Alumno,
    actividad: ActividadEvaluacion,
    criteriosCatalogo: List<CriterioEvaluacion>,
    notasActuales: List<NotaEvaluacion>,
    onDismiss: () -> Unit,
    onGuardar: (Map<String, Double?>) -> Unit
) {
    // Extraer códigos de criterios de la actividad
    val codigosCriterios = remember(actividad.criteriosCodigosJson) {
        try {
            val arr = JSONArray(actividad.criteriosCodigosJson)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
            list
        } catch (e: Exception) {
            emptyList<String>()
        }
    }

    // Mapa de valores editados
    val mapaNotas = remember {
        val map = mutableStateMapOf<String, String>()
        codigosCriterios.forEach { cod ->
            val notaExistente = notasActuales.find {
                it.alumnoNombre.equals(alumno.nombreCompleto, ignoreCase = true) &&
                        it.actividadId == actividad.id &&
                        it.criterioCodigo == cod
            }
            map[cod] = notaExistente?.valor?.let { String.format(Locale.US, "%.1f", it) } ?: ""
        }
        map
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = alumno.nombreCompleto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${actividad.nombre} • ${actividad.unidadSda}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Introduce notas de 1 a 10 (escribe 0 o borra para quitar la nota):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(codigosCriterios) { cod ->
                        val criterio = criteriosCatalogo.find { it.codigo == cod }
                        val desc = criterio?.descripcion ?: ""
                        val textoNota = mapaNotas[cod] ?: ""

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "$cod ${if (desc.isNotBlank()) "— $desc" else ""}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            OutlinedTextField(
                                value = textoNota,
                                onValueChange = { nuevo ->
                                    val normalizado = nuevo.replace(',', '.')
                                    mapaNotas[cod] = normalizado
                                },
                                placeholder = { Text("Nota (1.0 - 10.0)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val resultado = mutableMapOf<String, Double?>()
                    codigosCriterios.forEach { cod ->
                        val str = mapaNotas[cod]?.trim() ?: ""
                        if (str.isNotBlank()) {
                            val num = str.toDoubleOrNull()
                            if (num != null && num > 0.0) {
                                resultado[cod] = minOf(10.0, num)
                            } else {
                                resultado[cod] = null
                            }
                        } else {
                            resultado[cod] = null
                        }
                    }
                    onGuardar(resultado)
                }
            ) {
                Text("Guardar Notas")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
