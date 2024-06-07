package com.kiwi.finanzas

enum class Mes(val nombre: String, val dias: Int) {
    ENERO("Enero", 31),
    FEBRERO("Febrero", 28),
    MARZO("Marzo", 31),
    ABRIL("Abril", 30),
    MAYO("Mayo", 31),
    JUNIO("Junio", 30),
    JULIO("Julio", 31),
    AGOSTO("Agosto", 31),
    SEPTIEMBRE("Septiembre", 30),
    OCTUBRE("Octubre", 31),
    NOVIEMBRE("Noviembre", 30),
    DICIEMBRE("Diciembre", 31);

    companion object {
        fun obtenerPorIndice(indice: Int): Mes {
            return entries.getOrNull(indice) ?: ENERO
        }
    }
}