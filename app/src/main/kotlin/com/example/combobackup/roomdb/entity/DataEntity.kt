package com.example.combobackup.roomdb.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sampledata")
class DataEntity
{
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo(name = "frequency")
    var frequency: Double = 0.0

    @ColumnInfo(name = "atten")
    var atten : Int = 0

    @ColumnInfo(name = "mode")
    var mode : Boolean = false
}