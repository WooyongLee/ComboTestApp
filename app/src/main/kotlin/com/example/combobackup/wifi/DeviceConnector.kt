package com.example.combobackup.wifi

import android.content.*
import android.content.Context.WIFI_SERVICE
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Handler
import android.util.Log
import com.example.combobackup.mqtt.MqttService
import java.math.BigInteger
import java.net.InetAddress
import java.net.UnknownHostException

class DeviceConnector {

    companion object {

        fun wifiScan(paramContext: Context) : String {
            var retID : String  = "";

            // late initialization :: 필요할 때 초기화해서 사용 가능
            lateinit var wifiManager : WifiManager

            wifiManager = paramContext.getSystemService(WIFI_SERVICE) as WifiManager

            // 주기적으로 Wifi 신호를 받아오는 BroadcastReceiver
            val wifiScanReceiver: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(c: Context, intent: Intent) {
                    val action = intent.action

                    if (action == WifiManager.WIFI_STATE_CHANGED_ACTION) {
                        var wifiState = intent.getIntExtra(
                            WifiManager.EXTRA_WIFI_STATE,
                            WifiManager.WIFI_STATE_UNKNOWN
                        )

                        // Wifi 활성화 여부에 따른 분기
                        when (wifiState) {
                            WifiManager.WIFI_STATE_DISABLING -> Log.d(
                                "WifiState",
                                "Deactivating..."
                            ) // 비활성화 중
                            WifiManager.WIFI_STATE_DISABLED -> {
                                Log.d("WifiState", "Deactivated")

                                // Wifi 활성화 요청
                                // To Do :: Be Wifi Enable
                            }
                            WifiManager.WIFI_STATE_ENABLING -> Log.d(
                                "WifiState",
                                "Enabling..."
                            ) // 활성화 중
                            WifiManager.WIFI_STATE_ENABLED -> {
                                Log.d("WifiState", "Enabled")
                                var networkSsid = wifiManager.connectionInfo.getSSID()

                            }
                        }
                    }

                    if (action == WifiManager.NETWORK_STATE_CHANGED_ACTION)
                    {
                        var info = intent.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)

                        var state = info?.detailedState

                        Log.d("WifiState", "The network is changed. " + state)

                        if (state == NetworkInfo.DetailedState.CONNECTED)
                        {

                        }
                    }


                    if (action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
                    {
                        val success = intent.getBooleanExtra(
                            WifiManager.EXTRA_RESULTS_UPDATED, false)
                        if (success)
                        {
                            retID = scanSuccess(wifiManager)

                            var ipAddress : Long = wifiManager.connectionInfo.ipAddress.toLong();
                            var ipByteArray = BigInteger.valueOf(ipAddress).toByteArray()
                            var ipAddressString : String = ""
                            try{
                                ipAddressString = InetAddress.getByAddress(ipByteArray).hostAddress
                            }
                            catch (e : UnknownHostException)
                            {
                                Log.e("WIFIIP", "Unable to get host address")
                            }

                            val retIntent = Intent(c, paramContext.javaClass)
                            c.sendBroadcast(retIntent)

                            MqttService.getInstanceMqtt()?.initMqtt(ipAddressString)
                        }
                        else
                        {
                            // scan failure handling
                            scanFailure()
                        }
                    }
                }
            }

            val intentFilter : IntentFilter = IntentFilter()
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION) // WIFI, Network 관련한 ACTION 들 intentFilter 에 추가
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            paramContext.registerReceiver(wifiScanReceiver, intentFilter)

            val success = wifiManager.startScan()
            if (!success)
            {
                // scan failure handling
                scanFailure()
            }

            else
            {
                // wait to receive ssid
                Log.d("wifiScan", "wait to receive ssid")
            }

            Log.d("wifiScan", "SSID : " + retID)
            return retID;
        }

        private fun scanSuccess(wifiManager : WifiManager) : String {
            var scanSsid = ""

            // WifiManager에서 Scan한 결과를 SSID 리스트로 반환,
            val FindStr = "WYLEE"
            val results = wifiManager.scanResults.toList().map { it.SSID }

            val lengthOfResults = results.count()
            Log.d("scanSuccess()", "results")
            results.forEach { Log.d("wifiScan", it) }

            // 특정 이름을 가진 SSID를 찾아서 ID를 반환함
//            if (results != null) {
//                scanSsid = results.findLast { it?.contains(FindStr) == true }!!
//                Log.d("scanSuccess()", "Find results " + scanSsid)
//                // val resIp = wifiManager.scanResults
//            }
            return scanSsid;
        }

        private fun scanFailure() {
            // handle failure: new scan did NOT succeed
            Log.d("wifi", "scanFailure")
        }
    }
}