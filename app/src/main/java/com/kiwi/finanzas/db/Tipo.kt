package com.kiwi.finanzas.db
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tipos")
data class Tipo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val color: String
)
