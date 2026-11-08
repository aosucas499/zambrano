package com.example.data

import com.example.data.model.Alumno
import com.example.data.model.CompaneroCen
import com.example.data.model.HorarioSlot

object FictionalDataSeeder {

    val DOCENTE_DEFECTO = "Miguel de Cervantes"

    fun getDocentesEjemplo(): List<String> = listOf(
        "Miguel de Cervantes",
        "Marie Curie",
        "Aristóteles",
        "Hipatia de Alejandría",
        "Albert Einstein",
        "Rosalind Franklin",
        "Leonardo da Vinci",
        "Ada Lovelace"
    )

    fun getClaustroEjemplo(): List<CompaneroCen> = listOf(
        CompaneroCen(nombre = "Cervantes Saavedra, Miguel de", email = "mcervantes@g.educaand.es"),
        CompaneroCen(nombre = "Curie Sklodowska, Marie", email = "mcurie@g.educaand.es"),
        CompaneroCen(nombre = "Estagira, Aristóteles de", email = "aristoteles@g.educaand.es"),
        CompaneroCen(nombre = "Alejandría, Hipatia de", email = "hipatia@g.educaand.es"),
        CompaneroCen(nombre = "Einstein, Albert", email = "aeinstein@g.educaand.es"),
        CompaneroCen(nombre = "Franklin, Rosalind", email = "rfranklin@g.educaand.es"),
        CompaneroCen(nombre = "Da Vinci, Leonardo", email = "ldavinci@g.educaand.es"),
        CompaneroCen(nombre = "Lovelace Byron, Ada", email = "alovelace@g.educaand.es")
    )

    fun getAlumnosEjemplo(): List<Alumno> = listOf(
        Alumno(
            nie = "10001",
            nombreCompleto = "Principito, El",
            primerApellido = "Principito",
            segundoApellido = "del Asteroide B-612",
            nombre = "El",
            curso = "3º Educación Primaria",
            unidad = "3º Literatura Infantil",
            unidadNormalizada = "3º Lit.",
            telefonoUrgencia = "600123456",
            email = "principito@g.educaand.es"
        ),
        Alumno(
            nie = "10002",
            nombreCompleto = "Wormwood, Matilda",
            primerApellido = "Wormwood",
            segundoApellido = "Honey",
            nombre = "Matilda",
            curso = "3º Educación Primaria",
            unidad = "3º Literatura Infantil",
            unidadNormalizada = "3º Lit.",
            telefonoUrgencia = "600234567",
            email = "matilda@g.educaand.es"
        ),
        Alumno(
            nie = "10003",
            nombreCompleto = "Pan, Peter",
            primerApellido = "Pan",
            segundoApellido = "de Nunca Jamás",
            nombre = "Peter",
            curso = "3º Educación Primaria",
            unidad = "3º Literatura Infantil",
            unidadNormalizada = "3º Lit.",
            telefonoUrgencia = "600345678",
            email = "peterpan@g.educaand.es"
        ),
        Alumno(
            nie = "10004",
            nombreCompleto = "Gale, Dorothy",
            primerApellido = "Gale",
            segundoApellido = "de Kansas",
            nombre = "Dorothy",
            curso = "3º Educación Primaria",
            unidad = "3º Literatura Infantil",
            unidadNormalizada = "3º Lit.",
            telefonoUrgencia = "600456789",
            email = "dorothy@g.educaand.es"
        ),
        Alumno(
            nie = "10005",
            nombreCompleto = "Calzaslargas, Pippi",
            primerApellido = "Calzaslargas",
            segundoApellido = "Villa Villekulla",
            nombre = "Pippilotta",
            curso = "3º Educación Primaria",
            unidad = "3º Literatura Infantil",
            unidadNormalizada = "3º Lit.",
            telefonoUrgencia = "600567890",
            email = "pippi@g.educaand.es"
        ),
        Alumno(
            nie = "10006",
            nombreCompleto = "Selva, Mowgli de la",
            primerApellido = "Selva",
            segundoApellido = "Seoni",
            nombre = "Mowgli",
            curso = "3º Educación Primaria",
            unidad = "3º Literatura Infantil",
            unidadNormalizada = "3º Lit.",
            telefonoUrgencia = "600678901",
            email = "mowgli@g.educaand.es"
        ),
        Alumno(
            nie = "10007",
            nombreCompleto = "Sawyer, Tom",
            primerApellido = "Sawyer",
            segundoApellido = "Polly",
            nombre = "Tom",
            curso = "3º Educación Primaria",
            unidad = "3º Literatura Infantil",
            unidadNormalizada = "3º Lit.",
            telefonoUrgencia = "600789012",
            email = "tomsawyer@g.educaand.es"
        ),
        Alumno(
            nie = "10008",
            nombreCompleto = "Maravillas, Alicia en el País de las",
            primerApellido = "Maravillas",
            segundoApellido = "Carroll",
            nombre = "Alicia",
            curso = "3º Educación Primaria",
            unidad = "3º Literatura Infantil",
            unidadNormalizada = "3º Lit.",
            telefonoUrgencia = "600890123",
            email = "alicia@g.educaand.es"
        ),
        Alumno(
            nie = "10009",
            nombreCompleto = "Madera, Pinocho de",
            primerApellido = "Madera",
            segundoApellido = "Geppetto",
            nombre = "Pinocho",
            curso = "3º Educación Primaria",
            unidad = "3º Literatura Infantil",
            unidadNormalizada = "3º Lit.",
            telefonoUrgencia = "600901234",
            email = "pinocho@g.educaand.es"
        ),
        Alumno(
            nie = "10010",
            nombreCompleto = "Baggins, Frodo",
            primerApellido = "Baggins",
            segundoApellido = "de la Comarca",
            nombre = "Frodo",
            curso = "3º Educación Primaria",
            unidad = "3º Literatura Infantil",
            unidadNormalizada = "3º Lit.",
            telefonoUrgencia = "600012345",
            email = "frodo@g.educaand.es"
        )
    )

    fun getHorarioEjemplo(docente: String = DOCENTE_DEFECTO): List<HorarioSlot> = listOf(
        // Lunes (1)
        HorarioSlot(
            docente = docente,
            diaSemana = 1,
            horaInicio = "09:00",
            horaFin = "10:00",
            grupo = "3º Literatura Infantil",
            asignatura = "Lengua Castellana y Literatura",
            aula = "Aula Cervantes",
            colorHex = "#1E88E5"
        ),
        HorarioSlot(
            docente = docente,
            diaSemana = 1,
            horaInicio = "10:00",
            horaFin = "11:00",
            grupo = "3º Literatura Infantil",
            asignatura = "Taller de Cuentos y Creatividad",
            aula = "Biblioteca",
            colorHex = "#00897B"
        ),
        HorarioSlot(
            docente = docente,
            diaSemana = 1,
            horaInicio = "11:30",
            horaFin = "12:30",
            grupo = "3º Literatura Infantil",
            asignatura = "Filosofía para Niños",
            aula = "Aula 3º",
            colorHex = "#F57F17"
        ),
        HorarioSlot(
            docente = docente,
            diaSemana = 1,
            horaInicio = "12:30",
            horaFin = "13:30",
            grupo = "3º Literatura Infantil",
            asignatura = "Dramatización y Teatro",
            aula = "Salón de Actos",
            colorHex = "#8E24AA"
        ),

        // Martes (2)
        HorarioSlot(
            docente = docente,
            diaSemana = 2,
            horaInicio = "09:00",
            horaFin = "10:00",
            grupo = "3º Literatura Infantil",
            asignatura = "Matemáticas en la Literatura",
            aula = "Aula Cervantes",
            colorHex = "#1E88E5"
        ),
        HorarioSlot(
            docente = docente,
            diaSemana = 2,
            horaInicio = "10:00",
            horaFin = "11:00",
            grupo = "3º Literatura Infantil",
            asignatura = "Ciencias Naturales y Exploración",
            aula = "Laboratorio",
            colorHex = "#43A047"
        ),
        HorarioSlot(
            docente = docente,
            diaSemana = 2,
            horaInicio = "11:30",
            horaFin = "12:30",
            grupo = "3º Literatura Infantil",
            asignatura = "Lectura Comprensiva",
            aula = "Biblioteca",
            colorHex = "#00897B"
        ),

        // Miércoles (3)
        HorarioSlot(
            docente = docente,
            diaSemana = 3,
            horaInicio = "09:00",
            horaFin = "10:30",
            grupo = "3º Literatura Infantil",
            asignatura = "Taller de Escritura Mágica",
            aula = "Aula Cervantes",
            colorHex = "#E53935"
        ),
        HorarioSlot(
            docente = docente,
            diaSemana = 3,
            horaInicio = "11:30",
            horaFin = "12:30",
            grupo = "Tutoría",
            asignatura = "Atención a Familias",
            aula = "Despacho",
            colorHex = "#546E7A"
        ),

        // Jueves (4)
        HorarioSlot(
            docente = docente,
            diaSemana = 4,
            horaInicio = "09:00",
            horaFin = "10:00",
            grupo = "3º Literatura Infantil",
            asignatura = "Poesía y Rima",
            aula = "Aula Cervantes",
            colorHex = "#8E24AA"
        ),
        HorarioSlot(
            docente = docente,
            diaSemana = 4,
            horaInicio = "10:00",
            horaFin = "11:00",
            grupo = "3º Literatura Infantil",
            asignatura = "Educación Artística",
            aula = "Taller de Arte",
            colorHex = "#F57F17"
        ),
        HorarioSlot(
            docente = docente,
            diaSemana = 4,
            horaInicio = "11:30",
            horaFin = "12:30",
            grupo = "3º Literatura Infantil",
            asignatura = "Inglés / English Literature",
            aula = "Aula 3º",
            colorHex = "#039BE5"
        ),

        // Viernes (5)
        HorarioSlot(
            docente = docente,
            diaSemana = 5,
            horaInicio = "09:00",
            horaFin = "10:30",
            grupo = "3º Literatura Infantil",
            asignatura = "Círculo de Debate y Filosofía",
            aula = "Patio / Ágora",
            colorHex = "#00897B"
        ),
        HorarioSlot(
            docente = docente,
            diaSemana = 5,
            horaInicio = "11:30",
            horaFin = "12:30",
            grupo = "3º Literatura Infantil",
            asignatura = "Evaluación Semanal y Caritas",
            aula = "Aula Cervantes",
            colorHex = "#43A047"
        )
    )
}
