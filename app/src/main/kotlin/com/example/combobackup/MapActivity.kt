package com.example.combobackup

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.FragmentActivity
import com.example.combobackup.databinding.MapViewBinding
import com.example.combobackup.fragment.HalfViewFragment
import com.example.combobackup.fragment.MapviewFragment
import com.example.combobackup.fragment.TableViewFragment

//region GoogleMap 이용할 때
class MapActivity : FragmentActivity(),

    ActivityCompat.OnRequestPermissionsResultCallback  {

    var isFabOpen = false // Fab 버튼 default는 닫혀있음

    lateinit var binding: com.example.combobackup.databinding.MapViewBinding
    lateinit var locationManager: LocationManager

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // region 각종 권한 설정 및 위치정보를 가져오기 위한 부가설정
        var isPermissionCheck = checkPermission(Manifest.permission.READ_CONTACTS)

        if( isPermissionCheck) Log.d("onCreate()", "permission checked")

        locationManager = this.getSystemService(FragmentActivity.LOCATION_SERVICE) as LocationManager
        val isGpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        Log.d("Check Provider", "Gps Enable " + isGpsEnable + ", Network Enable " + isNetworkEnable)
        // endregion

        // GPS 설정 비활성화 시 활성화 하도록 가이드
        if (!isGpsEnable) {
            val settingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(settingIntent)
        }

        binding = MapViewBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // region FAB Button Click Listener
        binding.mainFabButton.bringToFront()
        binding.mainFabButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                toggleFabClose(isFabOpen)
            }
        })

        binding.fabMap.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?){
                // To Do :: View Map Only\
                // create new fragment & transaction
                var tableFrag = TableViewFragment()
                // var tableFragment = supportFragmentManager.findFragmentById(R.id.table_frag) as SupportMapFragment?
                if (tableFrag != null) {
                    Log.v("fabMap.setOnClickListener()", "tableFragment replaced")

                    // replace whatever is in the fragment_container view with this fragment
                    // and add the transaction to the back stack if needed
                    var transaction = supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.mapViewMainLayout, tableFrag)
                    transaction.addToBackStack(null)

                    // commit the transaction
                    transaction.commit()
                } else {
                    Log.v("fabMap.setOnClickListener()", "tableFragment is null")
                }

                toggleFabClose(true)
            }
        })

        binding.fabData.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?){
                // To Do :: View Map Only\
                // create new fragment & transaction
                var mapFrag = MapviewFragment()
                // var tableFragment = supportFragmentManager.findFragmentById(R.id.table_frag) as SupportMapFragment?
                if (mapFrag != null) {
                    Log.v("fabMap.setOnClickListener()", "tableFragment replaced")

                    // replace whatever is in the fragment_container view with this fragment
                    // and add the transaction to the back stack if needed
                    var transaction = supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.mapViewMainLayout, mapFrag)
                    transaction.addToBackStack(null)

                    // commit the transaction
                    transaction.commit()
                } else {
                    Log.v("fabMap.setOnClickListener()", "tableFragment is null")
                }

                toggleFabClose(true)
            }
        })

        binding.fabHalf.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                // To Do :: View - Data Table Pair
                Log.v("fabHalf.setOnClickListener()", "onClick()")

                var hafFrag = HalfViewFragment()
                if (hafFrag != null)
                {
                    var transaction = supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.mapViewMainLayout, hafFrag)
                    transaction.addToBackStack(null)

                    transaction.commit()
                }
                else
                {
                    Log.v("fabHalf.setOnClickListener()", "hafFragment is null")
                }

                toggleFabClose(true)
            }
        })
        // endregion
    }

    fun toggleFabClose(op : Boolean)
    {
        if ( op )
        {
            var mapAnimator = ObjectAnimator.ofFloat(binding.fabMap, "translationY", 0f)
            mapAnimator.start()

            var dataAnimator = ObjectAnimator.ofFloat(binding.fabData, "translationY", 0f)
            dataAnimator.start()

            var halfAnimator = ObjectAnimator.ofFloat(binding.fabHalf, "translationY", 0f)
            halfAnimator.start()

            ObjectAnimator.ofFloat(binding.mainFabButton, View.ROTATION, 45f, 0f).apply{start()}
            isFabOpen = false
        }

        else
        {
            Log.v("toggleFab()", "fab open")
            var mapAnimator = ObjectAnimator.ofFloat(binding.fabMap, "translationY", 150f)
            mapAnimator.start()

            var dataAnimator = ObjectAnimator.ofFloat(binding.fabData, "translationY", 300f)
            dataAnimator.start()

            var halfAnimator = ObjectAnimator.ofFloat(binding.fabHalf, "translationY", 450f)
            halfAnimator.start()


            ObjectAnimator.ofFloat(binding.mainFabButton, View.ROTATION, 0f, 45f).apply{start()}
            isFabOpen= true
        }
    }

    fun checkPermission(permissionName: String): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            val granted =
                ContextCompat.checkSelfPermission(this, permissionName)
            granted == PackageManager.PERMISSION_GRANTED
        } else {
            val granted =
                PermissionChecker.checkSelfPermission(this, permissionName)
            granted == PermissionChecker.PERMISSION_GRANTED
        }


    }
}
//endregion


//region Naver Map
//class MapActivity : FragmentActivity(), OnMapReadyCallback
//{
//    lateinit var binding : MapViewBinding
//
//    private lateinit var naverMap : NaverMap
//    private lateinit var locationSource : FusedLocationSource
//    private val mapView : MapView by lazy { findViewById(R.id.mapView)}
//
//    var isChaseModeOn = false;
//    lateinit var mapComponent : MapComponent
//
//    override fun onCreate(savedInstanceState: Bundle?)
//    {
//        Log.d("MapActivity", "onCreate()")
//
//        super.onCreate(savedInstanceState)
//        // setContentView(R.layout.map_view)
//        binding = MapViewBinding.inflate(layoutInflater)
//
//        // getRoot method로 layout 내부 최상위 위치 뷰의 instance를 활용하여 생성된 뷰를 activity에 표시
//        setContentView(binding.root)
//
//        mapView.onCreate(savedInstanceState)
//        mapView.getMapAsync(this)
//
//        mapComponent = MapComponent();
//
//        binding.ChaseButton.text = "추적 시작";
//        binding.ChaseButton.setOnClickListener(object: View.OnClickListener
//        {
//            @SuppressLint("MissingPermission")
//            override fun onClick(v: View?)
//            {
//                // Toggle Chase Mode
//                isChaseModeOn = !isChaseModeOn
//
//                if (isChaseModeOn)
//                {
//                    binding.ChaseButton.text = "추적 종료";
//
//                    // Location Chase And Draw Path on Map
//                    mapComponent.setMapUpdate(this@MapActivity)
//
//                    // Get Current Location
//                    val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
//                    val currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
//
//                    mapComponent.setMapDraw(naverMap, currentLocation)
//
//                    // Wait To Length of Coord >= 2 ?
//
//                }
//
//                else
//                {
//                    binding.ChaseButton.text = "추적 시작";
//
//                    // Chase Off, Save Paths
//                    mapComponent.stopUpdateLocationListner()
//                }
//            }
//        })
//
//        binding.CurrentLocationButton.text = "현재 위치";
//        binding.CurrentLocationButton.setOnClickListener(object: View.OnClickListener
//        {
//            @SuppressLint("MissingPermission")
//            override fun onClick(v: View?)
//            {
//                // Get Current Location
//                val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
//                val currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
//
//                Toast.makeText(applicationContext, "Current Location :: (" +
//                        String.format("%.7f", currentLocation?.latitude) + ", " +
//                        String.format("%.7f", currentLocation?.longitude) + ")", Toast.LENGTH_SHORT).show()
//
//                // Camera Update
//                if ( naverMap != null)
//                {
//                    val cameraUpdate = CameraUpdate.scrollTo(LatLng(currentLocation?.latitude!!, currentLocation?.longitude))
//                    naverMap.moveCamera(cameraUpdate)
//                }
//            }
//        })
//    }
//
//    override fun onStart() {
//        super.onStart()
//        mapView.onStart()
//
//        Log.d("MapActivity", "onStart()")
//    }
//
//    override fun onResume() {
//        super.onResume()
//        mapView.onStart()
//
//        Log.d("MapActivity", "onResume()")
//    }
//
//    override fun onPause() {
//        super.onPause()
//        mapView.onPause()
//
//        Log.d("MapActivity", "onPause()")
//    }
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        mapView.onSaveInstanceState(outState)
//
//        Log.d("MapActivity", "onSaveInstanceState()")
//    }
//
//    override fun onStop() {
//        super.onStop()
//        mapView.onStop()
//
//        Log.d("MapActivity", "onStop()")
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        mapView.onDestroy()
//
//        Log.d("MapActivity", "onDestroy()")
//    }
//
//    override fun onLowMemory() {
//        super.onLowMemory()
//        mapView.onLowMemory()
//
//        Log.d("MapActivity", "onLowMemory()")
//    }
//
//    @SuppressLint("Range")
//    override fun onMapReady(map: NaverMap)
//    {
//        Log.d("MapActivity", "onMapReady()")
//
//        naverMap = map
//
//        // 최대, 최소 Zoom 범위 설정
//        naverMap.maxZoom = 18.0
//        naverMap.minZoom = 6.0;
//
//        // Camera Update
//        val cameraUpdate = CameraUpdate.scrollTo(LatLng(37.481231, 126.729384))
//        naverMap.moveCamera(cameraUpdate)
//
//        // 현재 위치 이동 Button 활성화, 기능
//        val uiSetting = naverMap.uiSettings
//        uiSetting.isLocationButtonEnabled = true
//
//        locationSource = FusedLocationSource(this@MapActivity, LOCATION_PERMISSION_REQUEST_CODE)
//        naverMap.locationSource = locationSource
//
//        // Marker 기능
//        val marker = Marker()
//        marker.position = LatLng(37.492319, 126.723487)
//        marker.map = naverMap
//        marker.icon = MarkerIcons.BLACK
//        marker.iconTintColor = Color.RED
//    }
//
//
//    // 지도, 위치 관련한 권한 확인
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
//            return;
//        }
//
//        if ( locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults))
//        {
//            if ( !locationSource.isActivated)
//            {
//                naverMap.locationTrackingMode = LocationTrackingMode.None
//            }
//            return
//        }
//    }
//
//    companion object{
//        private const val LOCATION_PERMISSION_REQUEST_CODE = 1004
//    }
//}
//endregion
