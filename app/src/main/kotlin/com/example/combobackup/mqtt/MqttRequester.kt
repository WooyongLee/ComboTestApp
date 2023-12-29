package com.example.combobackup.mqtt

import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.util.Log
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MqttRequester {
    // New Connect Wifi
    fun connectToWifi(context : Context, ssid : String, password : String) : Boolean {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val networkConfig = WifiConfiguration()
        networkConfig.SSID = "CA-00038" // param ssid
        networkConfig.preSharedKey = "88888888" // param password

        val networkId = wifiManager.addNetwork(networkConfig)

        if (networkId != -1)
        {
            wifiManager.disconnect()
            wifiManager.enableNetwork(networkId, true)
            wifiManager.reconnect()
            Log.d("MqttRequester.connectToWifi()", "Wifi is Successive Connected");
            return true
        }

        Log.d("MqttRequester.connectToWifi()", "Wifi is DisConnected");
        return false
    }

    fun connectToMqttBroker(
        brokerUrl: String,
        clientId: String,
        username: String,
        password: String
    ) {
        val persistence = MemoryPersistence()
        val mqttClient = MqttClient(brokerUrl, clientId, persistence)
        val connOpts = MqttConnectOptions()
        connOpts.userName = username
        connOpts.password = password.toCharArray()

        val result = mqttClient.connectWithResult(connOpts)

        if (result.isComplete)
        {
            Log.d("connectToMqttBroker", "Connected to broker")
        }

        else
        {
            Log.e("connectToMqttBroker", "Failed to connect to broker")
        }

//        mqttClient.connect(connOpts, object : IMqttActionListener {
//            override fun onSuccess(asyncActionToken: IMqttToken) {
//                onConnectionComplete.invoke()
//            }
//
//            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
//                onFailure.invoke(exception)
//            }
//        })
    }
}