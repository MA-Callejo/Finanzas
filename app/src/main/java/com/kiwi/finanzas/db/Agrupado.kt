package com.kiwi.finanzas.db
import androidx.compose.ui.graphics.Color

data class Agrupado(
    var tipoId: Int,
    var red: Float,
    var green: Float,
    var blue: Float,
    var nombre: String,
    var total: Double
){
    fun color(): Color {
        return Color(red, green, blue)
    }
    fun textColor(): Color {
        val luminance = (0.299 * red + 0.587 * green + 0.114 * blue)
        return if(luminance > 0.5) Color.Black else Color.White
    }
}
