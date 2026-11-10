package com.example.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.Alumno
import com.example.data.model.CriterioEvaluacion
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object EvaluacionPdfExporter {

    private const val PAGE_WIDTH = 842 // A4 Landscape width in points
    private const val PAGE_HEIGHT = 595 // A4 Landscape height in points
    private const val MARGIN_LEFT = 35f
    private const val MARGIN_RIGHT = 807f
    private const val MARGIN_TOP = 40f
    private const val MARGIN_BOTTOM = 560f

    data class FilaAlumnoGlobal(
        val alumno: Alumno,
        val mediasCriterios: Map<String, Double>,
        val mediaGlobal: Double?
    )

    fun generarPdfGlobal(
        context: Context,
        area: String,
        curso: String,
        grupo: String,
        criterios: List<CriterioEvaluacion>,
        filas: List<FilaAlumnoGlobal>
    ): Uri? {
        val document = PdfDocument()

        val paintTitle = Paint().apply {
            color = Color.rgb(44, 62, 80) // Dark Slate
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintSubtitle = Paint().apply {
            color = Color.rgb(80, 90, 100)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paintHeaderCell = Paint().apply {
            color = Color.rgb(255, 255, 255)
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val paintHeaderBg = Paint().apply {
            color = Color.rgb(44, 62, 80)
            style = Paint.Style.FILL
        }

        val paintHeaderMediaBg = Paint().apply {
            color = Color.rgb(39, 174, 96) // Emerald green
            style = Paint.Style.FILL
        }

        val paintCellText = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paintCellNum = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val paintMediaNum = Paint().apply {
            color = Color.rgb(21, 101, 192)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val paintRowAlt = Paint().apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }

        val paintBorder = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 0.5f
            style = Paint.Style.STROKE
        }

        val alumnosPorPagina = 18
        val totalPaginas = maxOf(1, (filas.size + alumnosPorPagina - 1) / alumnosPorPagina)

        val colNombreWidth = 160f
        val colMediaWidth = 55f
        val numCriterios = maxOf(1, criterios.size)
        val espacioDisponible = (MARGIN_RIGHT - MARGIN_LEFT) - colNombreWidth - colMediaWidth
        val colCritWidth = espacioDisponible / numCriterios.toFloat()

        for (pagina in 0 until totalPaginas) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pagina + 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var y = MARGIN_TOP

            // Cabecera superior del documento
            canvas.drawText("CUADERNO DE ZAMBRANO — SÁBANA GLOBAL DE EVALUACIÓN LOMLOE", MARGIN_LEFT, y, paintTitle)
            y += 15f
            val fechaActual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", Locale("es", "ES")))
            canvas.drawText("Área: $area   |   Curso: $curso   |   Grupo: $grupo   |   Fecha: $fechaActual   |   Página ${pagina + 1} de $totalPaginas", MARGIN_LEFT, y, paintSubtitle)
            y += 18f

            // Fila de cabecera de la tabla
            val headerHeight = 22f
            canvas.drawRect(MARGIN_LEFT, y, MARGIN_LEFT + colNombreWidth, y + headerHeight, paintHeaderBg)
            paintHeaderCell.textAlign = Paint.Align.LEFT
            canvas.drawText("  Alumnado (Apellidos, Nombre)", MARGIN_LEFT + 5f, y + 15f, paintHeaderCell)
            paintHeaderCell.textAlign = Paint.Align.CENTER

            var currentX = MARGIN_LEFT + colNombreWidth
            criterios.forEach { crit ->
                canvas.drawRect(currentX, y, currentX + colCritWidth, y + headerHeight, paintHeaderBg)
                canvas.drawText(crit.codigo, currentX + (colCritWidth / 2f), y + 14f, paintHeaderCell)
                currentX += colCritWidth
            }

            canvas.drawRect(currentX, y, currentX + colMediaWidth, y + headerHeight, paintHeaderMediaBg)
            canvas.drawText("MEDIA", currentX + (colMediaWidth / 2f), y + 14f, paintHeaderCell)
            y += headerHeight

            // Filas de alumnos
            val inicioIdx = pagina * alumnosPorPagina
            val finIdx = minOf(inicioIdx + alumnosPorPagina, filas.size)
            val rowHeight = 20f

            for (i in inicioIdx until finIdx) {
                val f = filas[i]
                if (i % 2 == 1) {
                    canvas.drawRect(MARGIN_LEFT, y, MARGIN_RIGHT, y + rowHeight, paintRowAlt)
                }

                // Columna Alumno
                val nombreSeneca = when {
                    f.alumno.primerApellido.isNotBlank() -> {
                        val apellidos = if (f.alumno.segundoApellido.isNotBlank()) "${f.alumno.primerApellido} ${f.alumno.segundoApellido}" else f.alumno.primerApellido
                        "$apellidos, ${f.alumno.nombre}".trim()
                    }
                    else -> f.alumno.nombreCompleto
                }
                val nombreTruncado = if (nombreSeneca.length > 28) nombreSeneca.take(26) + "…" else nombreSeneca
                canvas.drawText(nombreTruncado, MARGIN_LEFT + 6f, y + 13f, paintCellText)

                var xCrit = MARGIN_LEFT + colNombreWidth
                criterios.forEach { crit ->
                    val nota = f.mediasCriterios[crit.codigo]
                    val textoNota = if (nota != null) String.format(Locale.US, "%.1f", nota) else "—"
                    canvas.drawText(textoNota, xCrit + (colCritWidth / 2f), y + 13f, paintCellNum)
                    xCrit += colCritWidth
                }

                // Media final
                val mediaFinalText = if (f.mediaGlobal != null) String.format(Locale.US, "%.2f", f.mediaGlobal) else "—"
                canvas.drawText(mediaFinalText, xCrit + (colMediaWidth / 2f), y + 13f, paintMediaNum)

                // Línea inferior de fila
                canvas.drawLine(MARGIN_LEFT, y + rowHeight, MARGIN_RIGHT, y + rowHeight, paintBorder)
                y += rowHeight
            }

            // Pie de página
            canvas.drawText("Generado por Cuaderno de Zambrano • Sistema de Evaluación Continua LOMLOE", MARGIN_LEFT, MARGIN_BOTTOM + 15f, paintSubtitle)

            document.finishPage(page)
        }

        return try {
            val cacheDir = File(context.cacheDir, "informes_evaluacion")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val cleanArea = area.replace(Regex("[^a-zA-Z0-9]"), "_")
            val cleanGrupo = grupo.replace(Regex("[^a-zA-Z0-9]"), "_")
            val archivo = File(cacheDir, "Informe_Global_${cleanArea}_${cleanGrupo}.pdf")
            FileOutputStream(archivo).use { fos ->
                document.writeTo(fos)
            }
            document.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                archivo
            )
        } catch (e: Exception) {
            e.printStackTrace()
            try { document.close() } catch (_: Exception) {}
            null
        }
    }
}
