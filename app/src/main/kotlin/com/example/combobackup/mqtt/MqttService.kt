package com.example.combobackup.mqtt

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.example.combobackup.MqttActivity
import com.example.combobackup.connection.ConnectionStatus
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttClientState
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import java.util.*
import java.util.function.BiConsumer

import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import kotlin.concurrent.timer

class MqttService : Service() {

    private val TAG = "MqttService"

    // Declare Static Instance of this
    companion object {
        private var instanceMqtt: MqttService? = null;

        fun getInstanceMqtt(): MqttService? {

            if ( instanceMqtt == null)
            {
                instanceMqtt = MqttService()
            }
            return instanceMqtt;
        }
    }

    private var mMqttClient: Mqtt3AsyncClient? = null

    var isConnected: Boolean = false
        private set

    private var mMqttConnectionTimeoutHandler: Handler? = null
    private var mDatetimeoutHandler: Handler? = null

    private val TOPIC_COMMAND = "pact/command"
    private val TOPIC_DATA1 = "pact/data1"
    private val TOPIC_DATA2 = "pact/data2"

    // ms
    private var mMqttTimeout : Long = 100000

    private var isRequestMqttConnection = false

    private var latestMQttStatus : MqttClientState = MqttClientState.DISCONNECTED

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    // MQTT Ping 송신
    fun startMqttPingTimer()
    {
        var second = 0
        timer(period = 6000, initialDelay = 1000)
        {

            if (mMqttClient != null)
            {
                // 이전 상태와 비교하여 변경점이 있는 경우에
                if (latestMQttStatus != mMqttClient?.state!!)
                {
                    MqttActivity.appendLog(TAG, "MqttClientState is " + mMqttClient?.state)

                    when (mMqttClient?.state) {
                        MqttClientState.CONNECTING -> MqttActivity.setMqttStatus(ConnectionStatus.Progress)
                        MqttClientState.CONNECTED -> MqttActivity.setMqttStatus(ConnectionStatus.Connected)
                        MqttClientState.DISCONNECTED -> MqttActivity.setMqttStatus(ConnectionStatus.Failed)

                        // Standby state
                        MqttClientState.CONNECTING_RECONNECT -> MqttActivity.setMqttStatus(
                            ConnectionStatus.Standby
                        )

                        MqttClientState.DISCONNECTED_RECONNECT -> MqttActivity.setMqttStatus(
                            ConnectionStatus.Standby
                        )
                    }
                }
                latestMQttStatus = mMqttClient?.state!!
            }


            // 더 이상 MQTT 요청을 하지 않는 경우에 Cancel
            if (!isRequestMqttConnection)
            {
                cancel()
            }
        }
    }

    fun initMqtt(url: String)
    {
        // MQTT Ping Timer 생성
        startMqttPingTimer()

        isRequestMqttConnection = true

        var uuid = UUID.randomUUID().toString()

        MqttActivity.appendLog(TAG, "Mqtt Initialize, ip :: $url \n UUID : $uuid");
        mMqttClient = MqttClient.builder()
            .useMqttVersion3()
            .identifier(uuid)
            .serverHost(url)
            .serverPort(1883)
            .buildAsync();

        connectClient()
    }

    fun connectClient()
    {
        MqttActivity.appendLog(TAG, "connectClient(), Try To Connect Client")

        mMqttClient?.connect()?.whenComplete { connAck, throwable ->

            if (throwable != null) {
                // Handle Failure
                MqttActivity.appendLog(TAG, "connectClient(), connect fail");
                isConnected = false

            }

            // 연결 성공시에 대한 처리
            else
            {
                MqttActivity.appendLog(TAG, "connectClient(), Connect Success")
                MqttActivity.appendLog(TAG, "connection ack return code = " + connAck.returnCode.code)

                isConnected= true
                subscribe()
            }

            // latestMQttStatus = mMqttClient?.state!!
        }
    }

    // Client, Broker Server에 구독
    private fun subscribe()
    {
        // /data1 topic에 구독, 콜백 등록 및 구독여부 확인
        mMqttClient?.subscribeWith()?.topicFilter(TOPIC_DATA1)?.callback { publish ->
            // Topic1 Thread put publish
        }?.send()?.whenComplete { subAck, throwable ->
            if (throwable != null)
            {
                MqttActivity.appendLog(TAG, "subscribe(), Handle failure to subscribe")
            }
            else
            {
                MqttActivity.appendLog(TAG, "subscribe(), Handle successful to subscribe")

                // Test Send Command
                sendCommand("0x11");
            }
        }

        mMqttClient?.subscribeWith()?.topicFilter(TOPIC_DATA2)?.callback { publish ->
            // Topic2 Thread put publish
        }?.send()?.whenComplete { subAck, throwable ->
            if (throwable != null)
            {
                MqttActivity.appendLog(TAG, "subscribe(), Handle failure to subscribe")
            }
            else
            {
                MqttActivity.appendLog(TAG, "subscribe(), Handle successful to subscribe")
            }
        }
    }

    fun sendCommand(command: String)
    {
        if (isConnected)
        {
            try
            {
                MqttActivity.appendLog(TAG, "sendCommand(), send command = $command")
                sendCommand(command.toByteArray())
            }
            catch (e:java.lang.Exception)
            {
                e.printStackTrace()
            }
        }
        else
        {

        }
    }

    // Command Topic으로 MQTT Message를 송신함
    fun sendCommand(command: ByteArray) {
        mMqttClient!!.publishWith()
            .topic(TOPIC_COMMAND) //.qos(MqttQos.AT_LEAST_ONCE)
            .payload(command)
            .send()
            .whenComplete(BiConsumer { mqtt3Publish: Mqtt3Publish?, throwable: Throwable? ->
                if (throwable != null) {
                    // handle failure to publish
                    Log.d("sendCommand", "failure to publish")

                    // 연결 끊겼을 시 연결 재시도
                    if (isConnected) {
                        isConnected = false
                        connectClient()
                    }
                    Log.d("SendCommand", "Command(byte[]) : $command")
                }
                else {
                    // handle successful publish, e.g. logging or incrementing a metric
                    //Log.d("sendCommand", "handle successful publish");
                }
            })
    }
}

