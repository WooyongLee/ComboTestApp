package com.example.combobackup.roomdb.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.combobackup.roomdb.entity.SystemEntity


// Data Access Object
@Dao
interface SystemDao {
    @Query("SELECT * FROM system")
    fun getAll(): List<SystemEntity?>?

    @Query("SELECT COUNT() FROM system")
    fun getCount() : Int

    @Query("SELECT isSuccessfulExit FROM system LIMIT 1")
    fun getIsSuccessful() : Boolean

    @Query("UPDATE system SET frameIndex = :paramFrameIndex")
    fun update(paramFrameIndex: Int)

    @Query("UPDATE system SET isSuccessfulExit=:paramIsSuccessfulExit, frameIndex = :paramFrameIndex")
    fun update(paramIsSuccessfulExit: Boolean, paramFrameIndex: Int)

    @Insert
    fun insert(vararg system: SystemEntity?)

    @Update
    fun update(system: SystemEntity?)

}