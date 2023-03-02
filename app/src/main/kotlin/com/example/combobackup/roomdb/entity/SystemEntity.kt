package com.example.combobackup.roomdb.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "system")
class SystemEntity() {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo(name = "isSuccessfulExit")
    var isSuccessfulExit: Boolean = true

    // 현재 화면의 Index
    @ColumnInfo(name = "frameIndex")
    var frameIndex : Int? = 0

    @Ignore
    constructor(_isSuccessfulExit : Boolean) : this() {
        this.isSuccessfulExit = _isSuccessfulExit
    }

    @Ignore
    constructor(_isSuccessfulExit: Boolean, _frameIndex: Int) : this(_isSuccessfulExit)
    {
        this.frameIndex = _frameIndex
    }
}