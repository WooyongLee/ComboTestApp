package com.example.combobackup.connection

import android.app.usage.UsageEvents

interface IConnected {

    // 연결 성공에 대한 콜백
    fun onConnected(event: UsageEvents.Event)

    fun onDisconnected(event: UsageEvents.Event)

}