package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.DocenteRepository
import com.example.data.model.ActividadEvaluacion
import com.example.data.model.Alumno
import com.example.data.model.CompaneroCen
import com.example.data.model.ConfiguracionApp
import com.example.data.model.DiarioClase
import com.example.data.model.HorarioSlot
import com.example.data.model.NotaEvaluacion
import com.example.data.model.RegistroConducta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ResumenCopiaSeguridad(
    val alumnos: Int = 0,
    val companeros: Int = 0,
    val slotsHorario: Int = 0,
    val clasesDiario: Int = 0,
    val conductas: Int = 0,
    val actividades: Int = 0,
    val notas: Int = 0,
    val configs: Int = 0
)

object BackupManager {

    suspend fun exportarCopiaSeguridad(context: Context, repository: DocenteRepository): Uri? = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject()
            root.put("version", 1)
            root.put("app", "Cuaderno de Zambrano")
            root.put("fechaExportacion", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))

            // 1. Alumnos
            val alumnos = repository.obtenerTodosLosAlumnosDirecto()
            val jsonAlumnos = JSONArray()
            alumnos.forEach { a ->
                val obj = JSONObject()
                obj.put("id", a.id)
                obj.put("nie", a.nie)
                obj.put("nombreCompleto", a.nombreCompleto)
                obj.put("primerApellido", a.primerApellido)
                obj.put("segundoApellido", a.segundoApellido)
                obj.put("nombre", a.nombre)
                obj.put("curso", a.curso)
                obj.put("unidad", a.unidad)
                obj.put("unidadNormalizada", a.unidadNormalizada)
                obj.put("telefonoUrgencia", a.telefonoUrgencia)
                obj.put("email", a.email)
                jsonAlumnos.put(obj)
            }
            root.put("alumnos", jsonAlumnos)

            // 2. Claustro
            val companeros = repository.obtenerTodosLosCompanerosDirecto()
            val jsonCompaneros = JSONArray()
            companeros.forEach { c ->
                val obj = JSONObject()
                obj.put("id", c.id)
                obj.put("nombre", c.nombre)
                obj.put("email", c.email)
                jsonCompaneros.put(obj)
            }
            root.put("companeros", jsonCompaneros)

            // 3. Horario semanal
            val slots = repository.obtenerTodosLosSlotsDirecto()
            val jsonSlots = JSONArray()
            slots.forEach { s ->
                val obj = JSONObject()
                obj.put("id", s.id)
                obj.put("docente", s.docente)
                obj.put("diaSemana", s.diaSemana)
                obj.put("horaInicio", s.horaInicio)
                obj.put("horaFin", s.horaFin)
                obj.put("grupo", s.grupo)
                obj.put("asignatura", s.asignatura)
                obj.put("aula", s.aula)
                obj.put("colorHex", s.colorHex)
                jsonSlots.put(obj)
            }
            root.put("horario", jsonSlots)

            // 4. Diario de clase
            val diarios = repository.obtenerTodosLosDiariosDirecto()
            val jsonDiarios = JSONArray()
            diarios.forEach { d ->
                val obj = JSONObject()
                obj.put("id", d.id)
                obj.put("fecha", d.fecha)
                obj.put("fechaVisible", d.fechaVisible)
                obj.put("hora", d.hora)
                obj.put("grupo", d.grupo)
                obj.put("actividad", d.actividad)
                obj.put("observaciones", d.observaciones)
                obj.put("completada", d.completada)
                obj.put("llaveUnica", d.llaveUnica)
                obj.put("orden", d.orden)
                jsonDiarios.put(obj)
            }
            root.put("diario", jsonDiarios)

            // 5. Conducta y observaciones (caritas)
            val conductas = repository.obtenerTodasLasConductasDirecto()
            val jsonConductas = JSONArray()
            conductas.forEach { c ->
                val obj = JSONObject()
                obj.put("id", c.id)
                obj.put("alumnoId", c.alumnoId)
                obj.put("fecha", c.fecha)
                obj.put("hora", c.hora)
                obj.put("tipo", c.tipo)
                obj.put("observacion", c.observacion)
                obj.put("timestamp", c.timestamp)
                jsonConductas.put(obj)
            }
            root.put("conducta", jsonConductas)

            // 6. Actividades de Evaluación
            val actividades = repository.obtenerTodasLasActividadesDirecto()
            val jsonActividades = JSONArray()
            actividades.forEach { act ->
                val obj = JSONObject()
                obj.put("id", act.id)
                obj.put("nombre", act.nombre)
                obj.put("area", act.area)
                obj.put("curso", act.curso)
                obj.put("grupo", act.grupo)
                obj.put("unidadSda", act.unidadSda)
                obj.put("criteriosCodigosJson", act.criteriosCodigosJson)
                obj.put("fechaCreacion", act.fechaCreacion)
                jsonActividades.put(obj)
            }
            root.put("actividades", jsonActividades)

            // 7. Notas de Evaluación
            val notas = repository.obtenerTodasLasNotasDirecto()
            val jsonNotas = JSONArray()
            notas.forEach { n ->
                val obj = JSONObject()
                obj.put("id", n.id)
                obj.put("alumnoNombre", n.alumnoNombre)
                obj.put("actividadId", n.actividadId)
                obj.put("area", n.area)
                obj.put("curso", n.curso)
                obj.put("grupo", n.grupo)
                obj.put("unidadSda", n.unidadSda)
                obj.put("criterioCodigo", n.criterioCodigo)
                if (n.valor != null) obj.put("valor", n.valor)
                jsonNotas.put(obj)
            }
            root.put("notas", jsonNotas)

            // 8. Configuraciones (asignaciones de áreas por unidad, docente activo, etc.)
            val configs = repository.obtenerTodasLasConfigsDirecto()
            val jsonConfigs = JSONArray()
            configs.forEach { cfg ->
                val obj = JSONObject()
                obj.put("clave", cfg.clave)
                obj.put("valor", cfg.valor)
                jsonConfigs.put(obj)
            }
            root.put("configuracion", jsonConfigs)

            // Escribir archivo JSON en cache
            val fechaStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
            val fileName = "copia_seguridad_cuaderno_zambrano_$fechaStr.json"
            val file = File(context.cacheDir, fileName)

            FileOutputStream(file).use { fos ->
                fos.write(root.toString(2).toByteArray(Charsets.UTF_8))
            }

            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun compartirCopiaSeguridad(context: Context, uri: Uri) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Copia de Seguridad - Cuaderno de Zambrano")
            putExtra(Intent.EXTRA_TEXT, "Adjunto archivo de copia de seguridad completa de Cuaderno de Zambrano.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Guardar o compartir Copia de Seguridad").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    suspend fun restaurarCopiaSeguridad(
        context: Context,
        uri: Uri,
        repository: DocenteRepository
    ): ResumenCopiaSeguridad = withContext(Dispatchers.IO) {
        val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader(Charsets.UTF_8).readText()
        } ?: throw IllegalArgumentException("No se pudo leer el archivo de copia de seguridad.")

        val root = JSONObject(jsonString)

        // Parsear Alumnos
        val alumnos = mutableListOf<Alumno>()
        val jsonAlumnos = root.optJSONArray("alumnos") ?: JSONArray()
        for (i in 0 until jsonAlumnos.length()) {
            val obj = jsonAlumnos.getJSONObject(i)
            alumnos.add(
                Alumno(
                    id = obj.optLong("id", 0L),
                    nie = obj.optString("nie", ""),
                    nombreCompleto = obj.optString("nombreCompleto", ""),
                    primerApellido = obj.optString("primerApellido", ""),
                    segundoApellido = obj.optString("segundoApellido", ""),
                    nombre = obj.optString("nombre", ""),
                    curso = obj.optString("curso", ""),
                    unidad = obj.optString("unidad", ""),
                    unidadNormalizada = obj.optString("unidadNormalizada", ""),
                    telefonoUrgencia = obj.optString("telefonoUrgencia", ""),
                    email = obj.optString("email", "")
                )
            )
        }

        // Parsear Claustro
        val companeros = mutableListOf<CompaneroCen>()
        val jsonCompaneros = root.optJSONArray("companeros") ?: JSONArray()
        for (i in 0 until jsonCompaneros.length()) {
            val obj = jsonCompaneros.getJSONObject(i)
            companeros.add(
                CompaneroCen(
                    id = obj.optLong("id", 0L),
                    nombre = obj.optString("nombre", ""),
                    email = obj.optString("email", "")
                )
            )
        }

        // Parsear Horario
        val slots = mutableListOf<HorarioSlot>()
        val jsonSlots = root.optJSONArray("horario") ?: JSONArray()
        for (i in 0 until jsonSlots.length()) {
            val obj = jsonSlots.getJSONObject(i)
            slots.add(
                HorarioSlot(
                    id = obj.optLong("id", 0L),
                    docente = obj.optString("docente", ""),
                    diaSemana = obj.optInt("diaSemana", 1),
                    horaInicio = obj.optString("horaInicio", "09:00"),
                    horaFin = obj.optString("horaFin", "10:00"),
                    grupo = obj.optString("grupo", ""),
                    asignatura = obj.optString("asignatura", ""),
                    aula = obj.optString("aula", ""),
                    colorHex = obj.optString("colorHex", "#1E88E5")
                )
            )
        }

        // Parsear Diario
        val diarios = mutableListOf<DiarioClase>()
        val jsonDiarios = root.optJSONArray("diario") ?: JSONArray()
        for (i in 0 until jsonDiarios.length()) {
            val obj = jsonDiarios.getJSONObject(i)
            diarios.add(
                DiarioClase(
                    id = obj.optLong("id", 0L),
                    fecha = obj.optString("fecha", ""),
                    fechaVisible = obj.optString("fechaVisible", ""),
                    hora = obj.optString("hora", ""),
                    grupo = obj.optString("grupo", ""),
                    actividad = obj.optString("actividad", ""),
                    observaciones = obj.optString("observaciones", ""),
                    completada = obj.optBoolean("completada", false),
                    llaveUnica = obj.optString("llaveUnica", ""),
                    orden = obj.optInt("orden", 0)
                )
            )
        }

        // Parsear Conducta y Observaciones
        val conductas = mutableListOf<RegistroConducta>()
        val jsonConductas = root.optJSONArray("conducta") ?: JSONArray()
        for (i in 0 until jsonConductas.length()) {
            val obj = jsonConductas.getJSONObject(i)
            conductas.add(
                RegistroConducta(
                    id = obj.optLong("id", 0L),
                    alumnoId = obj.optLong("alumnoId", 0L),
                    fecha = obj.optString("fecha", ""),
                    hora = obj.optString("hora", ""),
                    tipo = obj.optString("tipo", "POSITIVO"),
                    observacion = obj.optString("observacion", ""),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                )
            )
        }

        // Parsear Actividades
        val actividades = mutableListOf<ActividadEvaluacion>()
        val jsonActividades = root.optJSONArray("actividades") ?: JSONArray()
        for (i in 0 until jsonActividades.length()) {
            val obj = jsonActividades.getJSONObject(i)
            actividades.add(
                ActividadEvaluacion(
                    id = obj.optLong("id", 0L),
                    nombre = obj.optString("nombre", ""),
                    area = obj.optString("area", ""),
                    curso = obj.optString("curso", ""),
                    grupo = obj.optString("grupo", ""),
                    unidadSda = obj.optString("unidadSda", "SA1"),
                    criteriosCodigosJson = obj.optString("criteriosCodigosJson", "[]"),
                    fechaCreacion = obj.optLong("fechaCreacion", System.currentTimeMillis())
                )
            )
        }

        // Parsear Notas
        val notas = mutableListOf<NotaEvaluacion>()
        val jsonNotas = root.optJSONArray("notas") ?: JSONArray()
        for (i in 0 until jsonNotas.length()) {
            val obj = jsonNotas.getJSONObject(i)
            val valor = if (obj.has("valor") && !obj.isNull("valor")) obj.optDouble("valor") else null
            notas.add(
                NotaEvaluacion(
                    id = obj.optLong("id", 0L),
                    alumnoNombre = obj.optString("alumnoNombre", ""),
                    actividadId = obj.optLong("actividadId", 0L),
                    area = obj.optString("area", ""),
                    curso = obj.optString("curso", ""),
                    grupo = obj.optString("grupo", ""),
                    unidadSda = obj.optString("unidadSda", "SA1"),
                    criterioCodigo = obj.optString("criterioCodigo", ""),
                    valor = valor
                )
            )
        }

        // Parsear Configuraciones
        val configs = mutableListOf<ConfiguracionApp>()
        val jsonConfigs = root.optJSONArray("configuracion") ?: JSONArray()
        for (i in 0 until jsonConfigs.length()) {
            val obj = jsonConfigs.getJSONObject(i)
            configs.add(
                ConfiguracionApp(
                    clave = obj.optString("clave", ""),
                    valor = obj.optString("valor", "")
                )
            )
        }

        // Restaurar en la base de datos
        repository.restaurarBaseDeDatosCompleta(
            alumnos = alumnos,
            companeros = companeros,
            slots = slots,
            diarios = diarios,
            conductas = conductas,
            actividades = actividades,
            notas = notas,
            configs = configs
        )

        // Asegurar que los criterios oficiales estén cargados
        repository.asegurarCriteriosPrecargados(context)

        ResumenCopiaSeguridad(
            alumnos = alumnos.size,
            companeros = companeros.size,
            slotsHorario = slots.size,
            clasesDiario = diarios.size,
            conductas = conductas.size,
            actividades = actividades.size,
            notas = notas.size,
            configs = configs.size
        )
    }
}
