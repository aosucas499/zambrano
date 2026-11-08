package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companeros_cen")
data class CompaneroCen(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val email: String
)
