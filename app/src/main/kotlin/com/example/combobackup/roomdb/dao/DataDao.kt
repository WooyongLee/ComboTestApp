package com.example.combobackup.roomdb.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.combobackup.roomdb.entity.DataEntity

@Dao
interface DataDao {
    @Query("SELECT * FROM sampledata")
    fun getAll() : List<DataEntity>

    @Query("SELECT COUNT() FROM sampledata")
    fun getCount() : Int

    @Query("SELECT frequency FROM sampledata LIMIT 1")
    fun getFrequency() : Double

    @Query("SELECT mode FROM sampledata LIMIT 1")
    fun getMode() : Boolean

    @Query("UPDATE sampledata SET frequency =:paramFrequency")
    fun update(paramFrequency : Double)

    @Query("UPDATE sampledata SET Atten =:paramAtten")
    fun update(paramAtten : Int)

    @Query("UPDATE sampledata SET mode =:paramMode")
    fun update(paramMode : Boolean)

    @Insert
    fun insert(vararg sampledata: DataEntity?)

    @Update
    fun update(sampledata : DataEntity?)

}