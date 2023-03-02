package com.example.combobackup

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.SearchManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.example.combobackup.ble.ConnectedThread
import com.example.combobackup.databinding.ConnectViewBinding
import java.io.IOException
import java.util.*

class ConnectActivity  : FragmentActivity() {

    val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val PERMISSIONS_S_ABOVE = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val REQUEST_ALL_PERMISSION = 2

    lateinit var binding : ConnectViewBinding

    var btAdapter: BluetoothAdapter? = null
    private val REQUEST_ENABLE_BT = 1

    // paired device list
    var pairedDevices: Set<BluetoothDevice>? = null
    var btArrayAdapter: ArrayAdapter<String>? = null
    var deviceAddressArray: ArrayList<String>? = null

    var btSocket: BluetoothSocket? = null
    var connectedThread: ConnectedThread? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ConnectViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // show paired devices
        btArrayAdapter = ArrayAdapter(this, R.layout.simple_list_item_1)

        /*SET THE ADAPTER TO LISTVIEW*/
        deviceAddressArray = ArrayList()
        binding.listview.setAdapter(btArrayAdapter)


        // Enable bluetooth
        btAdapter = BluetoothAdapter.getDefaultAdapter()
       // BluetoothAdapter.getDefaultAdapter().getBluetoothService(null)

        if (btAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
        }

        // 권한 체크
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if (!hasPermissions(this, PERMISSIONS_S_ABOVE)) {
                requestPermissions(PERMISSIONS_S_ABOVE, REQUEST_ALL_PERMISSION)
            }
        }else{
            if (!hasPermissions(this, PERMISSIONS)) {
                requestPermissions(PERMISSIONS, REQUEST_ALL_PERMISSION)
            }
        }

        binding.listview.setOnItemClickListener { parent, view, position, id ->

            val name: String = btArrayAdapter?.getItem(position).toString() // get name
            val address: String = deviceAddressArray?.get(position).toString()   // get address
            Log.d("onItemClick", "Try to Connect $name / $address")

            if ( btAdapter?.isDiscovering == true)
            {
                btAdapter?.cancelDiscovery()
            }

            val device: BluetoothDevice = btAdapter?.getRemoteDevice(address)!!

            // create & connect socket
            try {
                val UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

                // btSocket = device.createRfcommSocketToServiceRecord(UUID)
                // btSocket = device.createInsecureRfcommSocketToServiceRecord(UUID)
                var createMethod = device.javaClass.getMethod(
                    "createInsecureRfcommSocket", *arrayOf<Class<*>?>(
                        Int::class.javaPrimitiveType
                    )
                )
                btSocket = createMethod.invoke(device, 1) as BluetoothSocket
                Log.d("onItemClick", "Target device is ${device.name} / ${device.address}")

                // btSocket = createBluetoothSocket(device)
                // Log.d("onItemClick", "createBluetoothSocket!!")
                btSocket?.connect()

                Log.d("onItemClick", "connected to $name")
                connectedThread = btSocket?.let { ConnectedThread(it) }
                connectedThread?.start()!!
            } catch (e: IOException) {
                Log.d("onItemClick", "connection failed! ")
                e.printStackTrace()
            }
        }

        binding.btnPaired.setOnClickListener{
            btArrayAdapter!!.clear()
            if (deviceAddressArray != null && !deviceAddressArray!!.isEmpty()) {
                deviceAddressArray!!.clear()
            }
            pairedDevices = btAdapter?.getBondedDevices()!!
            if (pairedDevices?.size!! > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (device in pairedDevices!!) {
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    btArrayAdapter!!.add(deviceName)
                    deviceAddressArray!!.add(deviceHardwareAddress)
                }
            }
        }

        binding.btnSearch.setOnClickListener{

            // Check if the device is already discovering
            if (btAdapter?.isDiscovering == true) {
                Log.d("btnSearch.Pressed", "btAdapter.isDiscovering()")
                btAdapter?.cancelDiscovery()
            } else {
                if (btAdapter?.isEnabled == true) {
                    btAdapter?.startDiscovery()
                    btArrayAdapter!!.clear()
                    if (deviceAddressArray != null && !deviceAddressArray!!.isEmpty()) {
                        deviceAddressArray!!.clear()
                    }
                    Log.d("btnSearch.Pressed", "btAdapter.Initialize()")
                    
                    // Bluetooth 기기 검색에 대한 IntentFilter (Android System, other app에서 broadcast하는 message를 받을 수 있도록 설정하는 것) 생성,
                    // 어떤 메세지를 날렸을 떄 받을 것인지를 설정하는 것
                    val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
                    filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //BluetoothAdapter.ACTION_STATE_CHANGED : 블루투스 상태변화 액션
                    filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
                    filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED); //연결 확인
                    filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED); //연결 끊김 확인
                    filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);   //기기 검색 시작
                    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);  //기기 검색 종료
                    filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
                    this.registerReceiver(receiver, filter)
                } else {
                    Toast.makeText(
                        this.applicationContext,
                        "bluetooth not on",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.btnSend.setOnClickListener{
            if (connectedThread != null) {
                var writeText = binding.sendText.text.toString()
                Log.d("sendData", writeText)
                connectedThread!!.write(writeText)
            }
        }
    }

    override fun onPause()
    {
        super.onPause()
        unregisterReceiver(receiver)
    }

    @SuppressLint("MissingPermission")
    @Throws(IOException::class)
    private fun createBluetoothSocket(device: BluetoothDevice): BluetoothSocket? {
        val BT_MODULE_UUID =
            UUID.fromString("00000000-0000-1000-8000-00805F9B34FB") // "random" unique identifier
        try {
//            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
//            return (BluetoothSocket) m.invoke(device, BT_MODULE_UUID);
        } catch (e: Exception) {
            Log.e("TAG", "Could not create Insecure RFComm Connection", e)
        }
        return device.createRfcommSocketToServiceRecord(BT_MODULE_UUID)
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (BluetoothDevice.ACTION_FOUND == action) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                Log.d("onReceive()", "Bluetooth Device Receiver")
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (device != null) {
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    if (deviceName != null && deviceHardwareAddress != null) {
                        if (deviceName.contains("CWGEN") || deviceName.contains("SVPLN") ) {
                            btArrayAdapter!!.add(deviceName)
                            deviceAddressArray!!.add(deviceHardwareAddress)
                            btArrayAdapter!!.notifyDataSetChanged()
                            Log.d(
                                "onReceive()",
                                "device name = $deviceName, hw address$deviceHardwareAddress"
                            )
//
//                            if ( btAdapter?.isDiscovering == true)
//                            {
//                                btAdapter?.cancelDiscovery()
//                            }
//
//                            val device: BluetoothDevice = btAdapter?.getRemoteDevice(device.address)!!
//
//                            // create & connect socket
//                            try {
//                                val UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
//
//                                // btSocket = device.createRfcommSocketToServiceRecord(UUID)
//                                // btSocket = device.createInsecureRfcommSocketToServiceRecord(UUID)
//                                var createMethod = device.javaClass.getMethod(
//                                    "createInsecureRfcommSocket", *arrayOf<Class<*>?>(
//                                        Int::class.javaPrimitiveType
//                                    )
//                                )
//                                btSocket = createMethod.invoke(device, 1) as BluetoothSocket
//                                Log.d("onItemClick", "Target device is ${device.name} / ${device.address}")
//
//                                // btSocket = createBluetoothSocket(device)
//                                // Log.d("onItemClick", "createBluetoothSocket!!")
//                                btSocket?.connect()
//
//                                Log.d("onItemClick", "connected to $device.name")
//                                connectedThread = btSocket?.let { ConnectedThread(it) }
//                                connectedThread?.start()!!
//                            } catch (e: IOException) {
//                                Log.d("onItemClick", "connection failed! ")
//                                e.printStackTrace()
//                            }

                        } // end if (deviceName.contains("CWGEN") ) {
//                        Log.d(
//                            "onReceive()",
//                            "device name = $deviceName, hw address$deviceHardwareAddress"
//                        )
                    }
                }
            } // end if (BluetoothDevice.ACTION_FOUND.equals(action))
        }
    }

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    // Permission check
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ALL_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
                } else {
                    requestPermissions(permissions, REQUEST_ALL_PERMISSION)
                    Toast.makeText(this, "Permissions must be granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}