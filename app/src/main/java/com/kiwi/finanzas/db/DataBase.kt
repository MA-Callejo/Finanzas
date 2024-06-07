package com.kiwi.finanzas.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Entrada::class, Tipo::class], version = 3)
abstract class DataBase : RoomDatabase() {
    abstract fun entryDao(): EntradaDAO
    abstract fun typeDao(): TipoDAO

    companion object {
        @Volatile
        private var INSTANCE: DataBase? = null
        val MIGRATION_1_2 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tipos ADD COLUMN disponible INTEGER NOT NULL DEFAULT 1")
            }
        }

        fun getDatabase(context: Context): DataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DataBase::class.java,
                    "app_database"
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}