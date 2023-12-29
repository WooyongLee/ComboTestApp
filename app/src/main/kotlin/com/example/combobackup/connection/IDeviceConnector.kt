package com.example.combobackup.connection

import android.content.Context

interface IDeviceConnector {

    // Check Current Connection Status
    fun isConnected() : Boolean

    // Try Connect
    fun connect(context : Context)
}