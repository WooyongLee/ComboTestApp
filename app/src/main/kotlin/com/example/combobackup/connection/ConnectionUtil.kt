package com.example.combobackup.connection

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.util.Log
import java.net.Inet4Address
import java.net.NetworkInterface
import kotlin.experimental.and

class ConnectionUtil {

    companion object {

        val TAG = "ConnectionUtil"

        // Tethering Interface name 들을 모두 반환
        fun getAvailableNetworkInterfaces(cm: ConnectivityManager, methodName : String): Array<String>? {
            val wmMethods = cm.javaClass.declaredMethods
            var available: Array<String>? = null

            wmMethods.forEach { method ->
                Log.d(TAG, "method name = " + method.name)
                if (method.name == methodName) {
                    try {
                        available = method.invoke(cm) as? Array<String>?
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            return available
        }

        // IPv4 을 aaa.bbb.ccc.ddd 형태로 반환
        fun getIPv4Address(intf : NetworkInterface): String {
            var ipv4Addresses = ""

            try {
                val interfaceAddresses = intf.inetAddresses
                while (interfaceAddresses.hasMoreElements()) {
                    val inetAddress = interfaceAddresses.nextElement()
                    if (inetAddress is Inet4Address) {
                        val addressBytes = inetAddress.address
                        val ipv4 = "${convertByteToUnsignedInt(addressBytes[0] and 0xFF.toByte())}." +
                                "${convertByteToUnsignedInt(addressBytes[1] and 0xFF.toByte())}." +
                                "${convertByteToUnsignedInt(addressBytes[2] and 0xFF.toByte())}." +
                                "${convertByteToUnsignedInt(addressBytes[3] and 0xFF.toByte())}"
                        ipv4Addresses += if (ipv4Addresses.isNotEmpty()) ", $ipv4" else ipv4
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return ipv4Addresses
        }

        fun convertByteToUnsignedInt(byteValue: Byte): Int {
            return byteValue.toInt() and 0xFF
        }
    }
}