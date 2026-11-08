package com.example.util

import android.content.Context
import android.content.Intent
import com.example.data.model.DiarioClase

object ShareUtils {

    /**
     * Replicación mejorada del formato de WhatsApp del script original.
     */
    fun formatearTextoWhatsApp(fechaVisible: String, clases: List<DiarioClase>): String {
        val sb = StringBuilder()
        sb.append("📋 *PROGRAMACIÓN DOCENTE - $fechaVisible*\n")
        sb.append("═══════════════════════════\n\n")

        if (clases.isEmpty()) {
            sb.append("No hay sesiones programadas para este día.\n")
        } else {
            clases.forEach { clase ->
                val estado = if (clase.completada) "✅" else "⏰"
                sb.append("$estado *${clase.hora}* | *${clase.grupo}*\n")
                
                val act = clase.actividad.ifBlank { "Sin actividad definida" }
                sb.append("  • Actividad: $act\n")

                if (clase.observaciones.isNotBlank()) {
                    sb.append("  📌 Obs: ${clase.observaciones}\n")
                }
                sb.append("\n")
            }
        }

        sb.append("═══════════════════════════\n")
        sb.append("Generado con *Planificador Docente*")
        return sb.toString()
    }

    fun compartirPorWhatsApp(context: Context, texto: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, texto)
            setPackage("com.whatsapp")
        }

        try {
            context.startActivity(sendIntent)
        } catch (e: Exception) {
            // Si WhatsApp no está instalado o falla, abrimos el menú estándar de compartir
            val genericIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, texto)
            }
            context.startActivity(Intent.createChooser(genericIntent, "Compartir programación"))
        }
    }
}
