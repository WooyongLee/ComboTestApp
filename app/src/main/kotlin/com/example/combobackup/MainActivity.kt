package com.example.combobackup

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.LongSparseArray
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.combobackup.databinding.ActivityMainBinding
import com.example.combobackup.roomdb.database.DataDB
import com.example.combobackup.roomdb.database.SystemDB
import com.example.combobackup.roomdb.entity.DataEntity
import com.example.combobackup.roomdb.entity.SystemEntity
import com.example.combobackup.wifi.DeviceConnector
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


public class MainActivity : FragmentActivity() {

    private var mBinding : ActivityMainBinding? = null
    val binding get() = mBinding!!

    private val REQUEST_CODE_ASK_PERMISSIONS = 123
    private var mContext: Context? = null

    private var isSuccessful = false
    private var mClockHandler: Handler? = null

//    private var wifiManager: WifiManager? = null
//    private var scanDatas // ScanResult List
//            : List<ScanResult>? = null
//
//    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            val action = intent.action
//            if (action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
//                scanDatas = wifiManager!!.scanResults
//                Toast.makeText(applicationContext, (scanDatas as MutableList<ScanResult>?)?.get(0)!!.SSID, Toast.LENGTH_SHORT).show()
//            } else if (action == WifiManager.NETWORK_STATE_CHANGED_ACTION) {
//                sendBroadcast(Intent("wifi.ON_NETWORK_STATE_CHANGED"))
//            }
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_main)

        mContext = getApplicationContext();

        val PERMS_INITIAL = arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION)
        ActivityCompat.requestPermissions(this, PERMS_INITIAL, 127)

        // ?????? ????????? ??? ????????? ??????????????? inflate() ?????? - Activity?????? ????????? Binding class??? Instance ??????
        mBinding = ActivityMainBinding.inflate(layoutInflater)

        // getRoot method??? layout ?????? ????????? ?????? ?????? instance??? ???????????? ????????? ?????? activity??? ??????
        setContentView(binding.root)

        mClockHandler = @SuppressLint("HandlerLeak")
        object : Handler(){
            override  fun handleMessage(msg: Message){
                var cal = Calendar.getInstance()

                val sdf = SimpleDateFormat("HH:mm:ss")
                val strTime = sdf.format(cal.time)

                mBinding!!.clock?.setText(strTime)

                // Log.d("mClockHandler", "Receive Message From Thread")
            }
        }

        thread(start = true) {
            while ( true )
            {
                Thread.sleep(1000)
                mClockHandler?.sendEmptyMessage(0)
            }
        }

        // Check And Print Package Name Log
        Thread {
            while (true) {
                if (!checkPermission()) continue
                var packName = getPackageName(mContext);
                if (packName.isNotEmpty())
                {
                    Log.d("Check Package Name Thread", packName)
                }
                try {
                    Thread.sleep(2000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()

        binding.saButton.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {
                Thread({
                    SystemDB.getInstance(applicationContext)?.SystemDao()?.update(2)
                    Log.e("DB Update", false.toString() + ", " + "FrameIndex 2")
                }, "Thread Name saButtonClickLister()").start()

                var intent = Intent(this@MainActivity, SaActivity::class.java)
                startActivity(intent)
            }
        })

        binding.mapButton.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                var intent = Intent(this@MainActivity, MapActivity::class.java)
                startActivity(intent)
            }
        })

//        wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager;
//        if(!wifiManager!!.isWifiEnabled()){
//            wifiManager!!.setWifiEnabled(true);
//        }

        // lambda??? ?????? ???????????? ????????? ?????? ( View.OnClickListener ??????, ?????? ??????????????? it ????????? ?????? ??????)
        binding.connectButton.setOnClickListener {

//            val intentFilter = IntentFilter()
//            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
//            registerReceiver(receiver, intentFilter)
//            wifiManager!!.startScan()

            // ?????? ??????????????? :: Load Wifi - AP WYLEE ???????????? ??????
            // + ?????????????????? ????????? ??????
            // ?????? ?????? URL ??????
            // ???????????? ?????? ?????? ???????????? return ?????????

            // val ssid = DeviceConnector.wifiScan(this)

//            Thread() {
//                val ssid = DeviceConnector.wifiScan(this)
//
//                // wait to receive ssid
//
//                MqttService.getInstanceMqtt()?.initMqtt(ssid)
//            }.start()
            var intent = Intent(this@MainActivity, ConnectActivity::class.java)
            startActivity(intent)
        }

        // DB Update Button CLick Listner
        binding.updateDbButton.setOnClickListener{
            Thread {
                isSuccessful = !isSuccessful

                SystemDB.getInstance(applicationContext)?.SystemDao()?.update(isSuccessful, 1)
                Log.e("DB Update", isSuccessful.toString())

                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "DB Update, isSuccesfulExit " + isSuccessful.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.start()
        }

        // Check DB Button CLick Listner
        binding.checkDbButton.setOnClickListener{
            Thread {
                // Check System DB
                val resultBoolean =
                    SystemDB.getInstance(applicationContext)?.SystemDao()?.getIsSuccessful()
                Log.d("DB Select [System]", resultBoolean.toString())

                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "DB Select, isSuccesfulExit " + resultBoolean.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // Check Data DB
                val dbData = DataDB.getInstance(applicationContext)?.DataDao()?.getAll()
                if (dbData?.size!! > 0)
                {
                    Log.d("DB Select [Data]", "freq " + dbData[0].frequency.toString())
                    Log.d("DB Select [Data]", "atten " + dbData[0].atten.toString())
                    Log.d("DB Select [Data]", "mode " + dbData[0].mode.toString())
                }
            }.start()
        }

        // go to mqtt fragment
        binding.mqttServiceButton.setOnClickListener {

            var intent = Intent(this@MainActivity, MqttActivity::class.java)
            startActivity(intent)
        }

        // <editor-fold desc="Invisible Button Click Listners (Not Used)">
        // ?????? ????????? ?????? Button Click Listener
        binding.permissionOpenButton.setOnClickListener(View.OnClickListener {
            if (!checkPermission()) {
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
            else
            {
                Toast.makeText(
                    applicationContext,
                    "Permission is Already Done",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        // ?????? ?????? Button Click Listener
        binding.forceExitButton.setOnClickListener {
            Process.killProcess(Process.myPid());
        }

        binding.exitComboButton.setOnClickListener {

            // var activity_manager : ActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager;
            var activity_manager : ActivityManager = applicationContext.getSystemService(
                ACTIVITY_SERVICE
            ) as ActivityManager;
            var app_list = activity_manager.runningAppProcesses;
            var running_service_list = activity_manager.getRunningServices(1000)

            var size = running_service_list.size - 1
            for ( i : Int in 0 .. size )
            {
                var running_service_info = running_service_list[i]
                Log.d("run service", "Package Name : " + running_service_info.service.packageName )
                Log.d("run service", "Class Name : " + running_service_info.service.className )
            }

//            Log.d("exit Combo!", "Try kill process..")
//
//            for ( i in app_list.indices)
//            {
//                if("com.example.combobackup".equals(app_list.get(i).processName) == false)	{
//                    Process.sendSignal(app_list.get(i).pid, Process.SIGNAL_KILL);
//                    activity_manager.killBackgroundProcesses(app_list.get(i).processName);
//                }
//            }

//            var appListSize = app_list.size - 1
//            for (i : Int in 0 .. appListSize)
//            {
//		        var app_name : String = app_list.get(i).processName;
//
//                if("com.example.combobackup".equals(app_list.get(i).processName) == false)	{
//                    Process.sendSignal(app_list.get(i).pid, Process.SIGNAL_KILL);
//                    activity_manager.killBackgroundProcesses(app_list.get(i).processName);
//                }
//           	}
//
//            val pkgmgr = applicationContext.packageManager
//            val appinfo: ApplicationInfo?
//            appinfo = try {
//                pkgmgr.getApplicationInfo(this.packageName, 0)
//            } catch (e: PackageManager.NameNotFoundException) {
//                null
//            }
//            val applicationName =
//                (if (appinfo != null) pkgmgr.getApplicationLabel(appinfo) else "(unknown)") as String


//            var targetPid = android.os.Process.getGidForName("com.dabinsystems.pact_app")
//            android.os.Process.killProcess(targetPid)
        }
        // </editor-fold>

        // Frequency Input ????????? ?????? Text Change Listener
        binding.frequencyText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                Log.d("Text Changed", s.toString())
                var doubleFreq = binding.frequencyText.text.toString().toDouble()
                Thread { DataDB.getInstance(applicationContext)?.DataDao()?.update(doubleFreq) }.start()
            }
        })

        val view = this.currentFocus
        if (view != null) {
            val imm: InputMethodManager =
                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        // Atten Input Button Click??? ?????? Listener
        binding.attenButton.setOnClickListener {
            val alert: AlertDialog.Builder = AlertDialog.Builder(this)

            alert.setTitle("Title")
            alert.setMessage("Message")

            // Set an EditText view to get user input
            val input = EditText(this)
            alert.setView(input)

            alert.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, whichButton ->
                runOnUiThread {
                    // Atten ????????? ?????? ????????? ???????????? ?????????
                    var atten = input.toString().toInt()
                    binding.attenButton.text = atten.toString()
                    Thread{
                        DataDB.getInstance(applicationContext)?.DataDao()?.update(atten)
                    }.start()
                }
            })

            alert.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, whichButton ->
                })

            alert.show()
        }

        binding.modeButton.setOnClickListener {
            Thread {
                var mode = DataDB.getInstance(applicationContext)?.DataDao()?.getMode()
                mode = !mode!!
                DataDB.getInstance(applicationContext)?.DataDao()?.update(mode)
                runOnUiThread{
                    binding.modeButton.text = mode.toString()
                }
            }.start()
        }


        class DbRunnable : Runnable{
            override fun run() {
                var dbCount = SystemDB.getInstance(applicationContext)?.SystemDao()?.getCount()

                val handler: Handler = Handler(Looper.getMainLooper())

                // ???????????? ?????? ?????? Insert
                if ( dbCount == 0)
                {
                    SystemDB.getInstance(applicationContext)?.SystemDao()?.insert(SystemEntity(false, 1))
                    Log.e("DB Insert", "isSuccesfulExit True")

                    handler.postDelayed(Runnable
                    {
                        Toast.makeText(
                            applicationContext,
                            "DB Insert, isSuccesfulExit True",
                            Toast.LENGTH_SHORT
                        ).show()}, 0)
                }

                // ?????? ?????? Update
                else {
                    var systemComps : List<SystemEntity>?
                            = SystemDB.getInstance(applicationContext)?.SystemDao()?.getAll() as List<SystemEntity>?

                    Log.e(
                        "DB Select Items",
                        systemComps!![0].id.toString() + " "
                                + systemComps!![0].isSuccessfulExit.toString() + " "
                                + systemComps!![0].frameIndex.toString()
                    )

                    SystemDB.getInstance(applicationContext)?.SystemDao()?.update(false, 1)
                    Log.e("DB Update", "isSuccesfulExit False")

                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            "DB Update, isSuccesfulExit False",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                // Data DB ??????
                var dataDbCount = DataDB.getInstance(applicationContext)?.DataDao()?.getCount()
                if ( dataDbCount == 0)
                {
                    // Insert Data DB First
                    DataDB.getInstance(applicationContext)?.DataDao()?.insert(DataEntity())
                    Log.d("Insert Data","freq is " + DataDB.getInstance(applicationContext)?.DataDao()?.getFrequency().toString())

                    runOnUiThread {
                        binding.frequencyText.setText((0.0).toString())
                        binding.attenButton.text = 0.toString()
                        binding.modeButton.text = false.toString()
                    }
                }

                // ????????? ???????????? UI??? ?????? Setting
                else
                {
                    val dbData = DataDB.getInstance(applicationContext)?.DataDao()?.getAll()
                    if (dbData?.size!! > 0)
                    {
                        runOnUiThread {
                            binding.frequencyText.setText(dbData[0].frequency.toString())
                            binding.attenButton.text = dbData[0].atten.toString()
                            binding.modeButton.text = dbData[0].mode.toString()
                        }
//                        Log.d("DB Select [Data]", "freq " + dbData[0].frequency.toString())
//                        Log.d("DB Select [Data]", "atten " + dbData[0].atten.toString())
//                        Log.d("DB Select [Data]", "mode " + dbData[0].mode.toString())
                    }
                }
            }
        }
        val dbRunnable = DbRunnable()
        val t = Thread(dbRunnable)
        t.start()
    }

    fun getPackageName(context: Context?) : String{

        var usageStatsManager = context?.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        var lastRunnAppTimeStamp = 0L

        // ??????????????? ???????????? ????????? ?????? ????????? ???????????? ??? ????????? (begin ~ end ????????? App Name ??????)
        val INTERVAL = 10000;
        var end = System.currentTimeMillis()
        // 1min ago
        var begin = end - INTERVAL

        // LongSparseArray packageNameMap = new LongSparseArray<>();
        val packageNameMap: LongSparseArray<String> = LongSparseArray<String>()

        // ????????? ??????????????? ?????? ?????? UsageEvents
        val usageEvents = usageStatsManager.queryEvents(begin, end)

        while (usageEvents.hasNextEvent())
        {
            val event = UsageEvents.Event()
            usageEvents.getNextEvent(event)

            // if Current Event is foreground state ...
            if ( isForeGroundEvent(event) )
            {
                // ?????? App Name??? packageNameMap??? ??????
                packageNameMap.put(event.timeStamp, event.packageName)

                // ?????? ?????? ?????? ??? ???????????? ?????? TIme Stamp Update
                if ( event.timeStamp > lastRunnAppTimeStamp)
                {
                    lastRunnAppTimeStamp = event.timeStamp;
                }
            }
        }
        return packageNameMap.get(lastRunnAppTimeStamp, "").toString()
    }

    private fun isForeGroundEvent(event: UsageEvents.Event?): Boolean {
        if (event == null) return false
        return if (BuildConfig.VERSION_CODE >= 29) event.eventType == UsageEvents.Event.ACTIVITY_RESUMED else event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
    }

    private fun checkPermission(): Boolean {
        var granted = false
        val appOps = applicationContext
            .getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(), applicationContext.packageName
        )
        granted = if (mode == AppOpsManager.MODE_DEFAULT) {
            applicationContext.checkCallingOrSelfPermission(
                Manifest.permission.PACKAGE_USAGE_STATS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }

        // ?????? ????????? ???????????? ?????? ??????
        val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)
        
        val REQUEST_PERMISSION_CODE = 1

        for (permission in PERMISSIONS)
        {
            if (ActivityCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED)
            {
                return false;
            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            &&
            ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            askForLocationPermissions();
        } else {
            //do your work
        }

        return granted
    }

    private fun askForLocationPermissions() {

        var LOCATION_PERMISSION_REQUEST_CODE = 1

        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            AlertDialog.Builder(this)
                .setTitle("Location permessions needed")
                .setMessage("you need to allow this permission!")
                .setPositiveButton("Sure", DialogInterface.OnClickListener { dialog, which ->
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                })
                .setNegativeButton("Not now",
                    DialogInterface.OnClickListener { dialog, which ->
                        //                                        //Do nothing
                    })
                .show()

            // Show an expanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
        } else {

            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )

            // MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }

    override fun onResume() {
        Log.e("onResume()","Call!!")
        super.onResume()
    }

    override fun onDestroy() {
        Log.e("onDestroy()", "Call!!")

        Thread({
            SystemDB.getInstance(applicationContext)?.SystemDao()?.update(true, 1)
            Log.e("DB Update", true.toString() + " FrameIndex 1")
        }, "Thread Name onDestroy()").start()

        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        when (requestCode) {
            REQUEST_CODE_ASK_PERMISSIONS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                DeviceConnector.wifiScan(this)
                Log.d("wifi", "In this")
            } else {
                // Permission Denied
                Log.d("wifi", "permission denied")
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    // Touch Event Test
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        var bReturn = false

        // Prev task to draw
        runOnUiThread {
            Toast.makeText(
                applicationContext,
                "dispatchTouchEvent(), Prev Task to draw",
                Toast.LENGTH_SHORT
            ).show()
        }

        bReturn = super.dispatchTouchEvent(event)

        // After task to process

        return bReturn
    }

//    override fun onInterceptTouchEvent(ev : MotionEvent) : Boolean {
//
//        var b = when (ev.actionMasked)
//        {
//            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
//                false
//            }
//            MotionEvent.ACTION_MOVE -> {
//                // currently Scrolling -> intercept the touch event
//            }
//
//
//            else -> {
//                // ??????????????? Intercept touch event??? ????????? ?????? ??????.
//                // child view??? ?????? handled ????????? ???????
//                false
//            }
//        }
//
//
//        return false
//    }

}

