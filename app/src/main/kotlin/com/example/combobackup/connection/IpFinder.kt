package com.example.combobackup.connection

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.combobackup.MqttActivity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

// 연결된 Ether Interface에 IP를 가져오기 위한 클래스
class IpFinder {

    companion object {
        val TAG = "IpFinder"

        // broadcast() 중 exception 발생하면서 재귀호출을 제어하기 위한 flag
        var isFindStart = false
        private var broadcastRun = false

        fun stopFindDevice()
        {
            isFindStart = false
        }

        fun tryFindDevice(callback: (String) -> Unit) {
            isFindStart = true
            broadcast { ip ->
                if (ip != null) {
                    callback(ip)
                    // MqttActivity.appendLog(TAG, "tryFindDevice: binding ip: $ip")
                }
            }
        }

        // broadcast 수행, ip를 반환하기 위해 콜백을 파라미터로 등록
        private fun broadcast(callback: (String?) -> Unit) {
            if (broadcastRun) {
                MqttActivity.appendLog(TAG, "Broadcast: already be executing")
                callback.invoke(null) // 이미 실행 중이면 null 반환
                return
            }

            broadcastRun = true

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val port = MqttActivity.getPort()
                    val message = ByteArray(30)

                    MqttActivity.appendLog(TAG, "Broadcast: binding port: $port")
                    val p = DatagramPacket(message, message.size)
                    val s = DatagramSocket(null)
                    s.reuseAddress = true
                    s.broadcast = true
                    s.bind(InetSocketAddress(port))
                    s.soTimeout = 5000
                    s.receive(p)

                    val receive = String(p.data, StandardCharsets.UTF_8)
                    val ip: String?

                    MqttActivity.appendLog(TAG, "Broadcast: Receive Address : " + p.address.toString())
                    MqttActivity.appendLog(TAG, "receive raw string : $receive")

                    if (receive.startsWith("COMBO")) {

                        val buf: List<String> = receive.replace("COMBO@", "").split(";")
                        ip = buf[0]
                        val ssid = buf[1].trim { it <= ' ' }

                        MqttActivity.appendLog(TAG, "ip : $ip, ssid : $ssid")
                    } else {
                        ip = null
                    }

                    callback.invoke(ip) // IP 값을 콜백으로 전달
                } catch (e: Exception) {
                    e.printStackTrace()

                    // try Find status 가 유지되는 경우에, broadcast 재귀 수행
                    if (isFindStart) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            Log.d(TAG, "Broadcast Exception")
                            broadcast(callback) // Recursive call to broadcast with the same callback
                        }, 3500)
                    }

                    callback.invoke(null) // 예외 발생 시 null 반환
                } finally {
                    broadcastRun = false
                }
            }
        }
    }
}