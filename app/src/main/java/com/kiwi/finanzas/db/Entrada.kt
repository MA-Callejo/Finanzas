package com.kiwi.finanzas.db
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entradas")
data class Entrada(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val concepto: String,
    val cantidad: Double,
    val dia: Int,
    val mes: Int,
    val anno: Int,
    val hora: Int,
    val min: Int,
    val tipo: Int
)
