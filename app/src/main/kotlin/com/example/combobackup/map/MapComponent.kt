package com.example.combobackup.map

import android.util.Log

class MapComponent {
    fun setMapDraw() {

        Log.d("MapComponent.setMapDraw()", "Injection Test");
    }
}

// region Not Used (Naver Map Legacy)
// class MapComponent {

//    lateinit var naverMap : NaverMap
//
//    //내 위치를 가져오는 코드
//    lateinit var fusedLocationProviderClient: FusedLocationProviderClient //자동으로 gps값을 받아온다.
//    lateinit var locationCallback: LocationCallback //gps응답 값을 가져온다.
//    lateinit var locationRequest : LocationRequest
//
//    // Single Path
//    var path : PathOverlay = PathOverlay()
//
//    // Multi Path
//    lateinit var multipartPath : MultipartPathOverlay
//
//    // Position List
//    private var lstLatLng: MutableList<LatLng?>? = null
//
//    var initMapDraw = false
//
//    fun setMapUpdate(activity: Activity)
//    {
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
//        setUpdateLocationListner()
//    }
//
//    // Chase Process
//    // n초에 한번씩 GPS로 부터 현재 위치를 수신하여 처리하는 부분
//    @SuppressLint("MissingPermission")
//    fun setUpdateLocationListner()
//    {
//        locationRequest = LocationRequest.create()
//        locationRequest.run {
//            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY // 적절히 효율적인 정확도
//            interval = 2000 // 요청 주기 (ms)
//        }
//
//        locationCallback = object : LocationCallback() {
//            override fun onLocationResult(locationResult: LocationResult) {
//                locationResult ?: return
//
//                // 일반적으로 1회 반복, Callback 등록한 갯수만큼.
//                for ((_, location) in locationResult.locations.withIndex())
//                {
//                    setLastLocation(location)
//                }
//            }
//        }
//
//        // location 요청 함수 (locationRequest, locationCallback)
//        fusedLocationProviderClient.requestLocationUpdates(
//            locationRequest,
//            locationCallback,
//            Looper.myLooper()
//        )
//    }
//
//    // Dispose location Callback
//    fun stopUpdateLocationListner()
//    {
//        if ( fusedLocationProviderClient != null)
//        {
//            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
//        }
//    }
//
//    fun setLastLocation(location: Location) {
//        val myLocation = LatLng(location.latitude, location.longitude)
//        Log.d("location: ", "${myLocation.latitude}, ${myLocation.longitude}")
//
//        lstLatLng?.add(myLocation)
//
//        if (!initMapDraw)
//        {
//            initMapDraw = true
//        }
//
//        // path.coords.add(myLocation)
//
//        if (path != null)
//        {
//            Log.d("drawLength", path.coords.size.toString())
//        }
//
//    }
//
//    fun setMapDraw(naverMap: NaverMap, currentLocation: Location?)
//    {
//        this.naverMap = naverMap
//
//        if ( lstLatLng == null )
//        {
//            lstLatLng = ArrayList()
//        }
//        currentLocation?.let { setLastLocation(it) }
//        currentLocation?.let { setLastLocation(it) }
//
//        path.color = Color.RED
//        path.isHideCollidedSymbols = false
//        path.coords = lstLatLng as List<LatLng>
//        path.map = naverMap

//        path = PathOverlay()
//        path.color = Color.RED
//        path.isHideCollidedSymbols = false
//        path.map = naverMap
//        path.coords = lstLatLng as List<LatLng?>
//    }
// }
// endregion