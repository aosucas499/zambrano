package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.ActividadEvaluacion
import com.example.data.model.Alumno
import com.example.data.model.NotaEvaluacion
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

object SenecaCsvExporter {

    /**
     * Exporta los datos de una SdA concreta:
     * 1. Copia al portapapeles una tabla tabulada (\t) en orden alfabético estricto de Séneca.
     * 2. Genera un archivo CSV compatible con LibreOffice Calc y Séneca.
     */
    fun exportarSdaParaSeneca(
        context: Context,
        area: String,
        curso: String,
        grupo: String,
        unidadSda: String,
        alumnos: List<Alumno>,
        actividades: List<ActividadEvaluacion>,
        notas: List<NotaEvaluacion>
    ): Uri? {
        // Orden alfabético estricto por PrimerApellido, SegundoApellido, Nombre
        val alumnosOrdenados = alumnos.sortedWith(
            compareBy<Alumno> { it.primerApellido.lowercase() }
                .thenBy { it.segundoApellido.lowercase() }
                .thenBy { it.nombre.lowercase() }
        )

        // Cabeceras: "Apellidos, Nombre" + columnas por actividad
        val cabeceraLista = mutableListOf("Apellidos, Nombre")
        actividades.forEach { act ->
            cabeceraLista.add(act.nombre)
        }

        // 1. Construir texto tabulado para el portapapeles (ideal para pegar en LibreOffice Calc o Séneca)
        val sbTab = StringBuilder()
        sbTab.append(cabeceraLista.joinToString("\t")).append("\n")

        // 2. Construir contenido CSV (con comillas y separador coma / punto y coma estándar)
        val sbCsv = StringBuilder()
        // BOM UTF-8 para que LibreOffice Calc y Excel abran las tildes y eñes automáticamente
        sbCsv.append("\uFEFF")
        sbCsv.append(cabeceraLista.joinToString(";") { "\"$it\"" }).append("\n")

        alumnosOrdenados.forEach { al ->
            val nombreSeneca = when {
                al.primerApellido.isNotBlank() -> {
                    val apellidos = if (al.segundoApellido.isNotBlank()) "${al.primerApellido} ${al.segundoApellido}" else al.primerApellido
                    "$apellidos, ${al.nombre}".trim()
                }
                else -> al.nombreCompleto
            }

            val filaValoresTab = mutableListOf(nombreSeneca)
            val filaValoresCsv = mutableListOf("\"$nombreSeneca\"")

            actividades.forEach { act ->
                val notasAct = notas.filter { n ->
                    n.alumnoNombre.equals(al.nombreCompleto, ignoreCase = true) && n.actividadId == act.id
                }
                val valores = notasAct.mapNotNull { it.valor }
                val textoNota = if (valores.isNotEmpty()) {
                    valores.joinToString(" · ") { String.format(java.util.Locale.US, "%.1f", it) }
                } else {
                    "—"
                }
                filaValoresTab.add(textoNota)
                filaValoresCsv.add("\"$textoNota\"")
            }

            sbTab.append(filaValoresTab.joinToString("\t")).append("\n")
            sbCsv.append(filaValoresCsv.joinToString(";")).append("\n")
        }

        // Copiar al portapapeles de Android
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Séneca_${unidadSda}_$area", sbTab.toString())
            clipboard.setPrimaryClip(clip)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Guardar archivo CSV
        return try {
            val cacheDir = File(context.cacheDir, "exportaciones_seneca")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val cleanArea = area.replace(Regex("[^a-zA-Z0-9]"), "_")
            val cleanGrupo = grupo.replace(Regex("[^a-zA-Z0-9]"), "_")
            val nombreArchivoLimpio = "Seneca_${unidadSda}_${cleanArea}_${cleanGrupo}.csv"
            val file = File(cacheDir, nombreArchivoLimpio)
            FileOutputStream(file).use { fos ->
                fos.write(sbCsv.toString().toByteArray(StandardCharsets.UTF_8))
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
