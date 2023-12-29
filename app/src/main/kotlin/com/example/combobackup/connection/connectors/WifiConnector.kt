package com.example.combobackup.connection.connectors

import android.app.usage.UsageEvents
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import com.example.combobackup.MqttActivity
import com.example.combobackup.connection.IConnected
import com.example.combobackup.connection.IDeviceConnector
import java.util.Objects
import kotlin.concurrent.timer


class WifiConnector : ConnectorBase(), IDeviceConnector {

    var TAG = "WifiConnector"

    var isWifiConnected = false

    var isReceiverEnabled = false

    private var latestSsid = ""

    // Broadcast Receiver to get Wifi Status
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            when (Objects.requireNonNull(intent.action)) {
                WifiManager.WIFI_STATE_CHANGED_ACTION -> {

                    // Get Wifi State
                    val wifiState = intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN
                    )

                    when (wifiState) {
                        WifiManager.WIFI_STATE_DISABLING -> {
                            MqttActivity.appendLog(TAG, "wifi state, WIFI_STATE_DISABLING")
                        }

                        WifiManager.WIFI_STATE_DISABLED -> {
                            MqttActivity.appendLog(TAG, "wifi state, WIFI_STATE_DISABLED")
                            isWifiConnected = false
                            disconnectedTriggerEvent(UsageEvents.Event())
                        }

                        WifiManager.WIFI_STATE_ENABLING -> {
                            MqttActivity.appendLog(TAG, "wifi state, WIFI_STATE_ENABLING")
                        }

                        WifiManager.WIFI_STATE_ENABLED -> {
                            MqttActivity.appendLog(TAG, "wifi state, WIFI_STATE_ENABLED")
                            isWifiConnected = true
                            connectedTriggerEvent(UsageEvents.Event())
                        }
                    }
                } // end WifiManager.WIFI_STATE_CHANGED_ACTION

//                WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
//                    val info = intent.getParcelableExtra<Parcelable>(WifiManager.EXTRA_NETWORK_INFO) as NetworkInfo?
//                    val state = info!!.detailedState
//                    if (state == NetworkInfo.DetailedState.CONNECTED) { //네트워크 연결
//
//                    } else if (state == NetworkInfo.DetailedState.DISCONNECTED) { //네트워크 끊음
//
//                    }
//                }
            } // end when
        }
    }

    fun getWifiReceiver(isRegister : Boolean): BroadcastReceiver? {

        // AP SSID Checker Timer On
        isReceiverEnabled = isRegister

        return mReceiver
    }

    override fun isConnected(): Boolean {

        return isWifiConnected
    }

    override fun connect(context: Context) {

        // net. intf. wlan
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        // wifi 설정이 되어 wlan interface 가 살아있는 경우
        if (wifiManager.isWifiEnabled)
        {
            // Broadcast Receiver 에서 Connection 여부를 확인할 수 있음
            // 적절한 SSID 만 가져옴
            val ssid = getCurrentWifiSSID(context)
            latestSsid = ssid

            // Write SSID to log
            MqttActivity.appendLog(TAG, "SSID = $ssid")

            if (ssid == "")
            {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Check to Wifi Status", Toast.LENGTH_LONG).show()

                    Thread.sleep(2000)
                }
            }

            // if valid ssid
            else
            {
                startSsidCheckTimer(context)
            }
        }

        // Setting 에서 WIFI 가 켜져 있지 않은 경우
        else
        {
            MqttActivity.appendLog(TAG, "WIFI is off")

            // Show Wifi setting Window
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Please turn on wifi", Toast.LENGTH_LONG).show()

                Thread.sleep(2000)
            }

            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }


    private fun startSsidCheckTimer(context: Context)
    {
        timer(period = 3000, initialDelay = 1000)
        {
            val ssid = getCurrentWifiSSID(context)

            // Ssid가 변경되는 경우에
            if (latestSsid != ssid)
            {
                MqttActivity.appendLog(TAG, "SSID is changed, ssid = $ssid")

                // Combo의 SSID 로 변경되었을 때만 Connected Trigger
                if (ssid.startsWith("CA-0"))
                {
                    connectedTriggerEvent(UsageEvents.Event())
                }
            }

            // MqttActivity.appendLog(TAG, "latest ssid = $ssid")

            latestSsid = ssid

            if (!isReceiverEnabled)
            {
                cancel()
            }
        }
    }

    fun getCurrentWifiSSID(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var ssid = "Not Connected"

        if (wifiManager.isWifiEnabled) {
            val wifiInfo: WifiInfo = wifiManager.connectionInfo
            if (wifiInfo.networkId != -1) {
                ssid = wifiInfo.ssid
                
                // SSID 앞 뒤의 큰 따옴표 제거
                if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                    ssid = ssid.substring(1, ssid.length - 1)
                }
            }
        }

        else
        {
            return ""
        }
        return ssid
    }
}