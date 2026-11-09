package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.DiarioClase
import java.io.File
import java.io.FileOutputStream

object PdfExporter {

    private const val PAGE_WIDTH = 595 // A4 standard width in points
    private const val PAGE_HEIGHT = 842 // A4 standard height in points
    private const val MARGIN_LEFT = 40f
    private const val MARGIN_RIGHT = 555f
    private const val MARGIN_TOP = 50f
    private const val MARGIN_BOTTOM = 790f
    private const val CONTENT_WIDTH = MARGIN_RIGHT - MARGIN_LEFT

    /**
     * Genera un documento PDF profesional con la programación del día,
     * equivalente al método crearDocumentoDia del script original.
     */
    fun generarPdfProgramacion(
        context: Context,
        fechaVisible: String,
        fechaLarga: String,
        clases: List<DiarioClase>
    ): Uri? {
        val document = PdfDocument()

        val paintTitle = Paint().apply {
            color = Color.rgb(21, 101, 192) // Primary Sapphire
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintSubtitle = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paintHeaderClass = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintLabel = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintObsLabel = Paint().apply {
            color = Color.rgb(211, 47, 47) // Red observation
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintBody = Paint().apply {
            color = Color.rgb(51, 65, 85)
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paintDivider = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 1f
            isAntiAlias = true
        }

        val paintHeaderBg = Paint().apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var y = MARGIN_TOP

        fun dibujarEncabezadoPagina(c: Canvas) {
            c.drawText("PROGRAMACIÓN DOCENTE", MARGIN_LEFT, y, paintTitle)
            y += 20f
            c.drawText(fechaLarga, MARGIN_LEFT, y, paintSubtitle)
            y += 14f
            c.drawLine(MARGIN_LEFT, y, MARGIN_RIGHT, y, paintDivider)
            y += 20f
        }

        dibujarEncabezadoPagina(canvas)

        if (clases.isEmpty()) {
            canvas.drawText("No hay sesiones de clase registradas para esta jornada.", MARGIN_LEFT, y, paintBody)
        } else {
            clases.forEach { clase ->
                // Verificamos si cabe la sesión en esta página
                if (y > MARGIN_BOTTOM - 90f) {
                    document.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    y = MARGIN_TOP
                    dibujarEncabezadoPagina(canvas)
                }

                // Fondo suave para cabecera de la sesión
                canvas.drawRect(MARGIN_LEFT, y - 12f, MARGIN_RIGHT, y + 14f, paintHeaderBg)
                val check = if (clase.completada) "[Realizada] " else ""
                val cabecera = "$check${clase.hora}   |   ${clase.grupo}"
                canvas.drawText(cabecera, MARGIN_LEFT + 8f, y + 5f, paintHeaderClass)
                y += 28f

                // Actividad
                canvas.drawText("ACTIVIDAD:", MARGIN_LEFT + 8f, y, paintLabel)
                y += 15f
                val textoActividad = clase.actividad.ifBlank { "Sin actividad registrada" }
                val lineasActividad = dividirTextoEnLineas(textoActividad, paintBody, CONTENT_WIDTH - 20f)
                lineasActividad.forEach { linea ->
                    canvas.drawText(linea, MARGIN_LEFT + 12f, y, paintBody)
                    y += 14f
                }

                // Observaciones
                if (clase.observaciones.isNotBlank()) {
                    y += 6f
                    canvas.drawText("OBSERVACIONES / TAREAS:", MARGIN_LEFT + 8f, y, paintObsLabel)
                    y += 15f
                    val lineasObs = dividirTextoEnLineas(clase.observaciones, paintBody, CONTENT_WIDTH - 20f)
                    lineasObs.forEach { linea ->
                        canvas.drawText(linea, MARGIN_LEFT + 12f, y, paintBody)
                        y += 14f
                    }
                }

                y += 12f
                canvas.drawLine(MARGIN_LEFT, y, MARGIN_RIGHT, y, paintDivider)
                y += 18f
            }
        }

        // Pie de página
        val paintFooter = Paint().apply {
            color = Color.rgb(148, 163, 184)
            textSize = 9f
            isAntiAlias = true
        }
        canvas.drawText(
            "Cuaderno de Zambrano • Página $pageNumber • Documento generado para seguimiento pedagógico",
            MARGIN_LEFT,
            PAGE_HEIGHT - 30f,
            paintFooter
        )

        document.finishPage(page)

        // Guardar archivo PDF en directorio seguro de caché
        return try {
            val fileName = "programacion_${fechaVisible.replace("/", "-")}.pdf"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            document.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            null
        }
    }

    private fun dividirTextoEnLineas(texto: String, paint: Paint, anchoMax: Float): List<String> {
        val lineasResultado = mutableListOf<String>()
        val parrafos = texto.split("\n")

        parrafos.forEach { parrafo ->
            if (parrafo.isBlank()) {
                lineasResultado.add("")
            } else {
                val palabras = parrafo.split(" ")
                var lineaActual = StringBuilder()

                for (palabra in palabras) {
                    val prueba = if (lineaActual.isEmpty()) palabra else "$lineaActual $palabra"
                    val ancho = paint.measureText(prueba)
                    if (ancho <= anchoMax) {
                        lineaActual = StringBuilder(prueba)
                    } else {
                        if (lineaActual.isNotEmpty()) {
                            lineasResultado.add(lineaActual.toString())
                        }
                        lineaActual = StringBuilder(palabra)
                    }
                }
                if (lineaActual.isNotEmpty()) {
                    lineasResultado.add(lineaActual.toString())
                }
            }
        }
        return if (lineasResultado.isEmpty()) listOf("") else lineasResultado
    }

    fun abrirOCompartirPdf(context: Context, pdfUri: Uri, titulo: String = "Programación Docente PDF") {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            putExtra(Intent.EXTRA_SUBJECT, titulo)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Compartir o imprimir PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
