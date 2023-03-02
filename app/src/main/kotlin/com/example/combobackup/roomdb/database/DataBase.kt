package com.example.combobackup.roomdb.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.combobackup.roomdb.dao.DataDao
import com.example.combobackup.roomdb.dao.SystemDao
import com.example.combobackup.roomdb.entity.DataEntity
import com.example.combobackup.roomdb.entity.SystemEntity


@Database(entities = [SystemEntity::class], version = 2, exportSchema = false)
abstract class SystemDB : RoomDatabase() {
    abstract fun SystemDao(): SystemDao?

    companion object {
        private val DB_NAME = "system.db"
        private var INSTANCE: SystemDB? = null
        fun getInstance(context: Context): SystemDB? {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    SystemDB::class.java, DB_NAME
                ).fallbackToDestructiveMigration().build()
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}

@Database(entities = [DataEntity::class], version = 1, exportSchema = false)
abstract class DataDB : RoomDatabase() {
    abstract fun DataDao(): DataDao?

    companion object {
        private val DB_NAME = "data.db"
        private var INSTANCE: DataDB? = null
        fun getInstance(context: Context): DataDB? {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    DataDB::class.java, DB_NAME
                ).fallbackToDestructiveMigration().build()
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}