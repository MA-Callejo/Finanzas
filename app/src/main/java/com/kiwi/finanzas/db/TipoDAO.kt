package com.kiwi.finanzas.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TipoDAO {
    @Query("SELECT * FROM tipos")
    fun getAll(): Flow<List<Tipo>>

    @Insert
    suspend fun insert(tipos: Tipo)

    @Query("DELETE FROM tipos where id = :id")
    suspend fun insert(id: Int)

    @Update
    suspend fun update(tipo: Tipo)
}