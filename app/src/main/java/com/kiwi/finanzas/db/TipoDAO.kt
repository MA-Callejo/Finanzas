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

    @Query("UPDATE tipos SET disponible=0 where id = :id")
    suspend fun delete(id: Int)

    @Query("UPDATE tipos SET disponible=1 where id = :id")
    suspend fun restore(id: Int)

    @Update
    suspend fun update(tipo: Tipo)
}