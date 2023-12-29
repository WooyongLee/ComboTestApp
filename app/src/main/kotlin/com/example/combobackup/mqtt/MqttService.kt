package com.example.combobackup.mqtt

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import java.lang.IllegalArgumentException
import java.util.*
import java.util.function.BiConsumer


class MqttService : Service() {

    // Declare Static Instance of this
    companion object {
        private var instanceMqtt: MqttService? = null;
        public fun getInstanceMqtt(): MqttService? {
            if ( instanceMqtt == null)
            {
                instanceMqtt = MqttService()
            }
            return instanceMqtt;
        }
    }

    private var mMqttClient: Mqtt3AsyncClient? = null

    private var isConnected: Boolean = false

    private var mMqttConnectionTimeoutHandler: Handler? = null
    private var mDatetimeoutHandler: Handler? = null

    private val TOPIC_COMMAND = "pact/command"
    private val TOPIC_DATA1 = "pact/data1"
    private val TOPIC_DATA2 = "pact/data2"

    // ms
    private var mMqttTimeout : Long = 100000

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    fun initMqtt(url: String)
    {
        var uuid = UUID.randomUUID().toString()
        var tmpUrl = "192.168.10.1"
        Log.d("Mqtt Initialize", "ip :: " + url + " UUID : " + uuid);
        mMqttClient = MqttClient.builder()
            .useMqttVersion3()
            .identifier(uuid)
            .serverHost(tmpUrl)
            .serverPort(1883)
            .buildAsync();

        connectClient()
    }

    fun connectClient()
    {
        Log.d("connectClient()", "Try To Connect Client")
        mMqttClient?.connect()?.whenComplete { connAck, throwable ->
            if (throwable != null) {
                // Handle Failure
                Log.e("Connect Client", "connect fail");
                isConnected = false

                // Try to reconnect
                if (mMqttClient != null) {
                    startCheckMqttConnectionHandler()
                }
            }

            // 연결 성공시에 대한 처리
            else
            {
                isConnected= true
                connectCompleted()
            }
        }
    }

    private fun connectCompleted()
    {
        subscribe()

        sendCommand("0x11");

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
                Log.d("subscribe()", "Handle failure to subscribe")
            }
            else
            {
                Log.d("subscribe()", "Handle succesful to subscribe")
            }
        }

        mMqttClient?.subscribeWith()?.topicFilter(TOPIC_DATA2)?.callback { publish ->
            // Topic2 Thread put publish
        }?.send()?.whenComplete { subAck, throwable ->
            if (throwable != null)
            {
                Log.d("subscribe()", "Handle failure to subscribe")
            }
            else
            {
                Log.d("subscribe()", "Handle succesful to subscribe")
            }
        }
    }

    fun sendCommand(command: String)
    {
        if (isConnected)
        {
            try
            {
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

    private fun startCheckMqttConnectionHandler()
    {
        // To Do :: 무선 연결 체크

        Log.d("startCheckMqttConnectionHandler()", "Check Mqtt Connection")

        if ( mMqttConnectionTimeoutHandler == null)
        {
            mMqttConnectionTimeoutHandler = Handler()
        }

        mMqttConnectionTimeoutHandler!!.removeCallbacksAndMessages(mMqttConnectionTimeoutRun)
        mMqttConnectionTimeoutHandler!!.postDelayed(mMqttConnectionTimeoutRun, 2000)

    }

    private fun startDateTimeoutHandler()
    {
        Log.d("startDateTimeoutHandler()", "Start DateTimeout")

        if ( mDatetimeoutHandler == null )
        {
            mDatetimeoutHandler = Handler()
        }

        mDatetimeoutHandler!!.removeCallbacks(mDateTimeoutRunnable)
        mDatetimeoutHandler!!.removeCallbacksAndMessages(null)
        mDatetimeoutHandler!!.postDelayed(mDateTimeoutRunnable, mMqttTimeout)
    }

    private val mMqttConnectionTimeoutRun: Runnable = label@ Runnable {
        val d = Log.d("mMqttConnectionTimeout", "in")

        try
        {
            if ( getDeviceConnected() )
            {
                Log.d("mMqttConnectionTimeoutRun Runnable", "Disconnected PACT Wifi")
                return@Runnable
            }

            if ( mMqttClient != null && !isConnected)
            {
                Log.d("mMqttConnectionTimeoutRun Runnable", "Check MQTT Connection")
                connectClient()
            }
        }
        catch (e:Exception)
        {
            e.printStackTrace()
        }
    }

    private val mDateTimeoutRunnable: Runnable = Runnable {

    }

    public fun getDeviceConnected() : Boolean {
        // To Do :: Wifi Check Logic 구현할 것
        
        return true;
    }
}

