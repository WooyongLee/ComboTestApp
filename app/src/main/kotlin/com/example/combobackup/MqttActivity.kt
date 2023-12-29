package com.example.combobackup

import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.example.combobackup.databinding.MqttRequesterViewBinding
import com.example.combobackup.mqtt.MqttRequester
import java.net.InetAddress

class MqttActivity : FragmentActivity() {

    lateinit var binding : MqttRequesterViewBinding

    lateinit var mqttReq : MqttRequester

    override  fun onCreate(savedInstanceState : Bundle?){
        super.onCreate(savedInstanceState)
        binding = MqttRequesterViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mqttReq = MqttRequester()

        binding.connectButton.setOnClickListener {

            // 1) Connect To Wifi AP
            val ssid = binding.ssidText.text.toString()
            val pw = binding.pwText.text.toString()
            mqttReq.connectToWifi(this, ssid, pw)

            // 2) Connect MQTT Broker (process in other thread)
            // (Get Ip Address)
            val connectedUrl = "tcp://" + getWifiApIpAddress(this) + ":1883";
            Log.d("connectButton.setOnClickListener()", "URL = " + connectedUrl)

            Thread {
                mqttReq.connectToMqttBroker(connectedUrl, ssid, ssid, pw)
            }.start()
        }

        binding.sendButton.setOnClickListener{

        }
    }

    fun getWifiApIpAddress(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo: WifiInfo = wifiManager.connectionInfo
        val ipAddress = wifiInfo.ipAddress
        val ipByteArray = byteArrayOf(
            (ipAddress and 0xff).toByte(),
            (ipAddress shr 8 and 0xff).toByte(),
            (ipAddress shr 16 and 0xff).toByte(),
            (ipAddress shr 24 and 0xff).toByte()
        )
        return try {
            InetAddress.getByAddress(ipByteArray).hostAddress
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
}