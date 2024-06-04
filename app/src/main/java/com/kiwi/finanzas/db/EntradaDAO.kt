package com.kiwi.finanzas.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EntradaDAO {
    @Query("SELECT * FROM entradas")
    fun getAll(): Flow<List<Entrada>>

    @Insert
    suspend fun insert(entrada: Entrada)

    @Query("DELETE FROM entradas where id = :id")
    suspend fun insert(id: Int)

    @Update
    suspend fun update(entrada: Entrada)

    @Query("SELECT * FROM entradas where anno = :anno")
    fun getAllAnno(anno: Int): Flow<List<Entrada>>

    @Query("SELECT * FROM entradas where mes = :mes and anno = :anno")
    fun getAllMes(mes: Int, anno: Int): Flow<List<Entrada>>

    @Query("SELECT * FROM entradas where mes = :mes and anno = :anno and dia = :dia")
    fun getAllDia(mes: Int, dia: Int, anno: Int): Flow<List<Entrada>>
}