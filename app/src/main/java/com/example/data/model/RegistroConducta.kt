package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "registros_conducta",
    indices = [
        Index(value = ["alumnoId"]),
        Index(value = ["fecha"])
    ]
)
data class RegistroConducta(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val alumnoId: Long,
    val fecha: String, // "yyyy-MM-dd"
    val hora: String = "",
    val tipo: String, // "POSITIVO" o "NEGATIVO"
    val observacion: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
