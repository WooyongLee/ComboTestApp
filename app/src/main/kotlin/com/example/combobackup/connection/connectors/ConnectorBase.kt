package com.example.combobackup.connection.connectors

import android.app.usage.UsageEvents
import com.example.combobackup.connection.IConnected

open class ConnectorBase {
    // region Connected Callback, Trigger Event
    private var callback: IConnected? = null

    fun setCallback(callback: IConnected) {
        this.callback = callback
    }

    // 이벤트가 발생했을 때 호출되는 메서드
    fun connectedTriggerEvent(event: UsageEvents.Event) {
        callback?.onConnected(event)
    }

    fun disconnectedTriggerEvent(event: UsageEvents.Event) {
        callback?.onDisconnected(event)
    }
    // endregi
}