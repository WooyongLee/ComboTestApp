package com.example.combobackup.connection.connectors

import android.app.usage.UsageEvents
import android.content.Context
import android.net.ConnectivityManager
import com.example.combobackup.MqttActivity
import com.example.combobackup.connection.ConnectionUtil
import com.example.combobackup.connection.IConnected
import com.example.combobackup.connection.IDeviceConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.reflect.InvocationTargetException
import java.net.NetworkInterface
import kotlin.concurrent.timer

class UsbTetherConnector : ConnectorBase(), IDeviceConnector {

    var TAG = "HotspotConnector"
    var D = false
    var isLastConnectedState = false

    override fun isConnected() : Boolean {

        if (D) MqttActivity.appendLog(TAG, "Try to USB Tethering")
        try {
            val en = NetworkInterface.getNetworkInterfaces()

            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val ip = ConnectionUtil.getIPv4Address(intf)
                if (D) MqttActivity.appendLog(TAG, "NetworkInterface " + intf.displayName + ", " + intf.name + ", " + ip)

                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (D) MqttActivity.appendLog(TAG, "InetAddress = $inetAddress")

                    if (!intf.isLoopback) {
                        // rndis : virtual ethernet link, net. interface type usb 추가
                        if (intf.name.contains("rndis") || intf.name.contains("usb")) {

                            MqttActivity.appendLog(TAG, "isUsbTethering enabled : " + intf.name)

                            return true
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    override fun connect(context : Context) {

        // check turn on usb tethering
        turnOn(context)
    }

    // usb tethering turn on
    private fun turnOn(context: Context) : Boolean {

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        var isTurnOn = false

        if (cm != null) {
            MqttActivity.appendLog(TAG, "try to find tethering interface")

            val wmMethods = cm.javaClass.declaredMethods

            // ConnectionUtil.getAvailableTetheringInterfaces(cm, "getTetheredIfaces")
            val available: Array<String>? = ConnectionUtil.getAvailableNetworkInterfaces(cm, "getTetherableIfaces")

            // Connect to Tether Interface
            wmMethods.forEach { method ->

                if (method.name.contains("tether"))
                {
                    MqttActivity.appendLog("in usb tethering method", "${method.name}<>")
                }

                // Check tether.. tether is not
                if (method.name == "tether") {
                    MqttActivity.appendLog(TAG, "gg==${method.name}")
                    MqttActivity.appendLog("in if", " case matches ${method.name} and str is tether")
                    try {
                        for (s in available!!) {
                            MqttActivity.appendLog(TAG, "available = $s")
                        }
                        val code = method.invoke(cm, "usb0") as Int
                        MqttActivity.appendLog(TAG, "code===$code")

                        isTurnOn = if (code==0) {
                            MqttActivity.appendLog(TAG, "Enable usb tethering successfully!");
                            true
                        } else {
                            MqttActivity.appendLog(TAG,"Enable usb tethering failed!");
                            false
                        }
                        return true
                    } catch (e: IllegalArgumentException) {
                        MqttActivity.appendLog(TAG, "error== gg $e")
                        e.printStackTrace()
                    } catch (e: IllegalAccessException) {
                        MqttActivity.appendLog(TAG, "error== gg $e")
                        e.printStackTrace()
                    } catch (e: InvocationTargetException) {
                        MqttActivity.appendLog(TAG, "error== gg $e")
                        e.printStackTrace()
                    }
                }
            }
        }

        return isTurnOn
    }

    fun loopOfCheckUsbTethering()
    {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {

                if (D) MqttActivity.appendLog(TAG, "UsbTetherConnector, Wait to connect ... ")

                // Retry Check isConnected Tethering
                val isConnectedState = isConnected()

                MqttActivity.appendLog(TAG, "in loop, last connectedState = $isLastConnectedState, connectedState = $isConnectedState\"")

                if (isLastConnectedState != isConnectedState) {
                    if (isConnectedState) {
                        connectedTriggerEvent(UsageEvents.Event())
                    } else {
                        disconnectedTriggerEvent(UsageEvents.Event())
                    }
                }

                isLastConnectedState = isConnectedState

                Thread.sleep(5000) // 연결이 확인되지 않았을 때 5초 대기 후 다시 확인
            }
        }
    }

}
