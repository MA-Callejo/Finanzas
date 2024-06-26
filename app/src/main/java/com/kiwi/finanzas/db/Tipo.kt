package com.kiwi.finanzas.db
import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tipos")
data class Tipo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val red: Float,
    val blue: Float,
    val green: Float,
    val disponible: Int = 1
){
    fun color(): Color {
        return Color(red, green, blue)
    }
    fun textColor(): Color {
        val luminance = (0.299 * red + 0.587 * green + 0.114 * blue)
        return if(luminance > 0.5) Color.Black else Color.White
    }
}
