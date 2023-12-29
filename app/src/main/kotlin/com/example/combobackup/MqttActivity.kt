package com.example.combobackup

import android.annotation.SuppressLint
import android.app.usage.UsageEvents
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.combobackup.connection.ConnectionStatus
import com.example.combobackup.connection.IConnected
import com.example.combobackup.connection.IDeviceConnector
import com.example.combobackup.connection.IpFinder
import com.example.combobackup.connection.connectors.UsbTetherConnector
import com.example.combobackup.connection.connectors.WifiConnector
import com.example.combobackup.databinding.MqttRequesterViewBinding
import com.example.combobackup.mqtt.MqttService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Thread.sleep

public class MqttActivity : FragmentActivity() {

    val TAG = "MqttActivity"
    lateinit var binding : MqttRequesterViewBinding
    var connector : IDeviceConnector? = null
    private var isConnectionAuto = false

    companion object {

        val logList = ArrayList<String>() // 로그를 담을 리스트

        // region UI Adapter
        private lateinit var logAdapter: ArrayAdapter<String> // 어댑터 변수 선언
        // endregion

        // region UI Widget object
        private lateinit var editMQTTMessageLog: ListView // EditText를 클래스 변수로 선언
            private set

        private lateinit var inputPortText : EditText
            private set

        private lateinit var ssidText : EditText
            private set

        private lateinit var connStatusImageButton: Button
            private set
        // endregion

        // 기존 로그를 가져와서 새로운 로그를 이어붙이는 함수
        fun appendLog(tag : String, newLog: String) {

            // Add Log List
            logList.add(newLog)

            Handler(Looper.getMainLooper()).post {

                // Notify changed to UI
                logAdapter.notifyDataSetChanged() // 데이터 변경 시 어댑터에 알리기
            }

            Log.d(tag, newLog)
        }

        @SuppressLint("SetTextI18n")
        fun setSsid(ssid : String)
        {
            Handler(Looper.getMainLooper()).post {

                ssidText.setText("SSID : $ssid")

                if (ssid == "") {
                    ssidText.setText("SSID : - ")
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun setMqttStatus(eConnectionStatus : ConnectionStatus)
        {
            Handler(Looper.getMainLooper()).post {

                when (eConnectionStatus) {
                    ConnectionStatus.Standby -> {
                        // Standby 상태일 때의 처리
                        setConnStatusButtonProperty(Color.GRAY, "Standby")
                    }
                    ConnectionStatus.Progress -> {
                        // Progress 상태일 때의 처리
                        setConnStatusButtonProperty(Color.YELLOW, "Progress")
                    }
                    ConnectionStatus.Connected -> {
                        // Connected 상태일 때의 처리
                        setConnStatusButtonProperty(Color.GREEN, "Connected")
                    }
                    ConnectionStatus.Failed -> {
                        // Failed 상태일 때의 처리
                        setConnStatusButtonProperty(Color.RED, "Failed")
                    }
                }
            } // end Handler
        }

        private fun setConnStatusButtonProperty(color: Int, buttonText: String) {

            connStatusImageButton.setBackgroundColor(color)
            connStatusImageButton.text = buttonText
        }

        fun getPort() : Int
        {
            return inputPortText.text.toString().toInt()
        }
    }

    override fun onCreate(savedInstanceState : Bundle?){
        super.onCreate(savedInstanceState)
        binding = MqttRequesterViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Gey ListView from view (binding)
        editMQTTMessageLog = binding.etMQTTMessageLog

        // Set Adapter
        logAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, logList)
        editMQTTMessageLog.adapter = logAdapter

        inputPortText = binding.inputPortTextEdit
        ssidText = binding.etSSID
        connStatusImageButton = binding.connectionStatusImageButton

        setMqttStatus(ConnectionStatus.Standby)

        // Auto - Manual Switching
        binding.autoConnectionToggleButton.setOnCheckedChangeListener { _, isChecked ->

            isConnectionAuto = isChecked

            initModules()

            if (isChecked) {

                // Check Connection loop,  wait to connect
                connector = UsbTetherConnector()

                // appendLog(TAG, "UsbTetherConnector, Set to UsbTetherConnector Callback")
                (connector as UsbTetherConnector).setCallback(object : IConnected {

                    // 이벤트 처리
                    override fun onConnected(event: UsageEvents.Event) {

                        appendLog(TAG, "UsbTetherConnector, onConnected")

                        // Try to Connect
                        (connector as UsbTetherConnector).connect(context = applicationContext)

                        // Find Device IP
                        IpFinder.tryFindDevice{ foundIp ->

                            appendLog(IpFinder.TAG, "MQTT IP: $foundIp")

                            // Init MQTT Client
                            MqttService.getInstanceMqtt()?.initMqtt(foundIp)
                        }
                    }

                    override fun onDisconnected(event: UsageEvents.Event) {

                    }
                })

                // if not connected, show tethering setting window ONCE
                if (!(connector as UsbTetherConnector).isConnected())
                {
                    showTetherSettingMenu()

                    while (true) {

                        appendLog(TAG, "UsbTetherConnector, Wait to connect ... ")

                        // Retry Check isConnected Tethering
                        if ((connector as UsbTetherConnector).isConnected()) {
                            break
                        }
                        sleep(5000) // 연결이 확인되지 않았을 때 5초 대기 후 다시 확인
                    }
                }
                GlobalScope.launch {

                    (connector as UsbTetherConnector).loopOfCheckUsbTethering()
                }

            } else {

            }
        }

        binding.btnWifi.setOnClickListener {

            initModules()

            connector = WifiConnector()

            val filter = IntentFilter()
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            // filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            registerReceiver((connector as WifiConnector).getWifiReceiver(true), filter)

            // Check Wifi Status
            if (!connector?.isConnected()!!)
            {
                connector?.connect(this)
            }

            (connector as WifiConnector).setCallback(object : IConnected {

                // 이벤트 처리
                override fun onConnected(event: UsageEvents.Event) {

                    appendLog(TAG, "WifiConnector, onConnected")

                    // Combo 의 고정 IP로 연결
                    MqttService.getInstanceMqtt()?.initMqtt("192.168.10.1")

                    // Example
                    MqttService.getInstanceMqtt()?.sendCommand("0x22")
                }

                override fun onDisconnected(event: UsageEvents.Event) {

                    appendLog(TAG, "WifiConnector, onDisconnected")
                }
            })
        }

        binding.btnHotspot.setOnClickListener {

            initModules()

            // Hotspot - USB Tethering 과 처리방식 동일
            connector = UsbTetherConnector()

            connector?.isConnected()

        }

        binding.btnUsbTether.setOnClickListener {

            initModules()

            connector = UsbTetherConnector()

            // Check USB Tethering connected
            if (!connector?.isConnected()!!)
            {
                showTetherSettingMenu()
            }

            // Wait to set tethering connected
            connector?.connect(this)

            IpFinder.tryFindDevice{ foundIp ->

                appendLog(IpFinder.TAG, "MQTT IP: $foundIp")

                MqttService.getInstanceMqtt()?.initMqtt(foundIp)
            }

            // Try to MQTT Connection
            // MqttService.getInstanceMqtt()?.initMqtt("192.168.10.1")

        }

        binding.sendDummyButton.setOnClickListener {

            MqttService.getInstanceMqtt()?.sendCommand("0x11")
        }
    }

    override fun onDestroy() {

        super.onDestroy()

        initModules()
    }

    private fun initModules()
    {
        logList.clear()

        IpFinder.stopFindDevice()

        // unregister broadcast receiver
        if (connector != null)
        {
            if (connector is WifiConnector)
            {
                unregisterReceiver((connector as WifiConnector).getWifiReceiver(false))
            }
        }
    }

    private fun showTetherSettingMenu()
    {
        // Show tethering setting window
        Handler(Looper.getMainLooper()).postDelayed(
            {
                Toast.makeText(
                    this,
                    "Please set up USB tethering",
                    Toast.LENGTH_SHORT
                ).show()
            },
            0)

        val intent = Intent()
        intent.component = ComponentName("com.android.settings", "com.android.settings.TetherSettings")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        this.startActivity(intent)
    }
}