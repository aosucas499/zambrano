package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AlumnoDao
import com.example.data.dao.CompaneroDao
import com.example.data.dao.ConductaDao
import com.example.data.dao.ConfiguracionDao
import com.example.data.dao.DiarioDao
import com.example.data.dao.EvaluacionDao
import com.example.data.dao.HorarioDao
import com.example.data.model.ActividadEvaluacion
import com.example.data.model.Alumno
import com.example.data.model.CompaneroCen
import com.example.data.model.ConfiguracionApp
import com.example.data.model.CriterioEvaluacion
import com.example.data.model.DiarioClase
import com.example.data.model.HorarioSlot
import com.example.data.model.NotaEvaluacion
import com.example.data.model.RegistroConducta

@Database(
    entities = [
        HorarioSlot::class,
        DiarioClase::class,
        Alumno::class,
        RegistroConducta::class,
        CompaneroCen::class,
        ConfiguracionApp::class,
        CriterioEvaluacion::class,
        ActividadEvaluacion::class,
        NotaEvaluacion::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun horarioDao(): HorarioDao
    abstract fun diarioDao(): DiarioDao
    abstract fun alumnoDao(): AlumnoDao
    abstract fun conductaDao(): ConductaDao
    abstract fun companeroDao(): CompaneroDao
    abstract fun configuracionDao(): ConfiguracionDao
    abstract fun evaluacionDao(): EvaluacionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "docente_planner.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
