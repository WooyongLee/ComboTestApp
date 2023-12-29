package com.example.combobackup.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.combobackup.R
import com.example.combobackup.databinding.MapViewFragmentBinding
import com.example.combobackup.map.MapDrawer
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.model.*
import kotlin.random.Random


class MapviewFragment : Fragment(), OnMapReadyCallback,

    GoogleMap.OnMapClickListener,
    GoogleMap.OnMapLongClickListener,
    GoogleMap.OnCameraMoveListener,
    GoogleMap.OnCameraMoveStartedListener,

    ActivityCompat.OnRequestPermissionsResultCallback,

    LocationListener {

    var isChaseModeOn = false;

    private var thisView : View? = null

    var _binding: MapViewFragmentBinding? = null
    lateinit var mGoogleMapView : MapView
    lateinit var mGoogleMap: GoogleMap
    lateinit var locationManager: LocationManager
    lateinit var currentContext: Context

    lateinit var buttonRemove : Button
    lateinit var buttonObject : Button
    lateinit var buttonLeft : Button
    lateinit var buttonRight : Button

    var arrayPoints = ArrayList<LatLng>()
    var polylineOptions = PolylineOptions()
    lateinit var mPolyline: Polyline

    lateinit var mapDrawer : MapDrawer

    private val binding get() = _binding!!

    var angle = 0.0

    var gpsLocationListener = LocationListener {
        var latLng = LatLng(it.latitude, it.longitude)

        if (mGoogleMap != null) {
            Log.d("arrayPoints", "Count = " + arrayPoints.size)

            arrayPoints.add(latLng)
            // polylineOptions.addAll(arrayPoints)
            mPolyline.points = arrayPoints

            Log.d("LocationListener", "projection = " + mGoogleMap.projection)
            for (p in arrayPoints) {
                Log.d("LocationListener", "location = " + p.latitude + ", " + p.longitude)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        currentContext = this.requireActivity().applicationContext

        if (thisView != null) {
            Log.d("onCreateView()", "remove view()")
            val parent = thisView!!.parent as ViewGroup
            parent?.removeView(view)
        }
        try {
            Log.d("onCreateView()", "inflate View()")
            thisView = inflater.inflate(R.layout.map_view_fragment, container, false)
        } catch (e: InflateException) {
            /* map is already there, just return view as it is */
        }

        mGoogleMapView = thisView!!.findViewById<View>(R.id.mapView) as MapView
        mGoogleMapView.onCreate(savedInstanceState)

        mGoogleMapView.onResume()
        try{
            MapsInitializer.initialize(requireActivity().applicationContext)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        mGoogleMapView.getMapAsync(this)

        buttonLeft = thisView!!.findViewById(R.id.ChaseButton)
        buttonRight = thisView!!.findViewById(R.id.ClearButton)
        buttonObject = thisView!!.findViewById(R.id.AddObjectButton)
        buttonRemove = thisView!!.findViewById(R.id.RemoveObjectButton)

        _binding = MapViewFragmentBinding.inflate(inflater, container, false)

        // region chase & map draw button Click Listener
        buttonLeft.text = "추적 시작";
        buttonLeft.setOnClickListener(object : View.OnClickListener {
            @SuppressLint("MissingPermission")
            override fun onClick(v: View?) {
                // Toggle Chase Mode
                isChaseModeOn = !isChaseModeOn

                if (isChaseModeOn) {
                    buttonLeft.text = "추적 종료";

                    // PolyLine 옵션 설정
                    polylineOptions = PolylineOptions()
                    polylineOptions.color(Color.RED)
                    polylineOptions.width(10f)
                    mPolyline = mGoogleMap.addPolyline(polylineOptions)

                    mPolyline.tag = "A"
                    mPolyline.endCap = RoundCap()
                    mPolyline.width = 8f
                    mPolyline.color = Color.RED
                    mPolyline.jointType = JointType.ROUND

                    // 가장최근 위치정보 가져오기
                    var location: Location? = getLastKnownLocation()

                    // 먼저 GPS로 얻어온다.
                    if (location != null) {
                        val provider = location.provider
                        val longitude = location.longitude
                        val latitude = location.latitude
                        val altitude = location.altitude
                        Log.d(
                            "Current Location",
                            "위치정보 : $provider 위도 : $longitude 경도 : $latitude 고도  : $altitude"
                        )

                        // GPS Listner 추가
                        locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            1000,
                            1f,
                            gpsLocationListener
                        )
                    }

                    // GPS로 못 얻어온 경우 네트워크로 얻어온다.
                    else {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                        if (location != null) {
                            val provider = location.provider
                            val longitude = location.longitude
                            val latitude = location.latitude
                            val altitude = location.altitude

                            Log.d(
                                "Initialize Current Location",
                                "위치정보 : $provider 위도 : $longitude 경도 : $latitude 고도  : $altitude"
                            )
                        } else {
                            Log.d(
                                "Current Location", "Network, GPS Location is null"
                            )
                        }


//                      // 위치정보를 원하는 시간, 거리마다 갱신해준다.
                        locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            1000,
                            1f,
                            gpsLocationListener
                        )
                    }

                } else {
                    buttonLeft.text = "추적 시작";
                    // Chase Off, Save Paths

                    if (locationManager != null) {
                        locationManager.removeUpdates(gpsLocationListener)
                    }

                    if (arrayPoints != null) {
                        arrayPoints.clear()
                    }
                }
            }
        })

        buttonRight.text = "경로 삭제"
        buttonRight.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                mGoogleMap.clear()
            }
        })
        // endregion

        buttonObject.text = "객체 추가"
        buttonObject.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                    val latOffset = 0.005
                    val lonOffset = 0.008
                    val randomLatOffset = Random.nextDouble(-latOffset, latOffset) // from ~ until
                    val randomLonOffset = Random.nextDouble(-lonOffset, lonOffset)
                    val companyPos = LatLng(37.342189 + randomLatOffset, 127.108483 + randomLonOffset)

                    // To Do :: Power에 따른 hue 설정 추가
                    // hue : 250~275, 315~340
                    val random1 = Random.nextFloat() * (275f - 250f) + 250f
                    val random2 = Random.nextFloat() * (340f - 315f) + 315f
                    val hue = if (Random.nextFloat() > 0.5f) random1 else random2

                    // get -120~-20 random power
                    // val randomPower = (-120..-20).random().toDouble() + Math.random()

                    _binding!!.AngleText.setText("0")
                    var strAngle = _binding!!.AngleText.text.toString()
                    angle += 30.0
                    mapDrawer = MapDrawer(0, companyPos, hue, angle, mGoogleMap)
            }
        })

        buttonRemove.text = "객체 제거"
        buttonRemove.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if ( mapDrawer != null )
                {
                    mapDrawer.removeMarker()
                }
            }
        })

        return thisView
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }

    override fun onStart() {
        super.onStart()
        if ( mGoogleMapView != null)
            mGoogleMapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        if ( mGoogleMapView != null)
            mGoogleMapView.onStop()
    }

    override fun onResume() {
        super.onResume()
        mGoogleMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mGoogleMapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mGoogleMapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mGoogleMapView.onDestroy()
    }

    // Fragment에서 View Bindnig 사용할 경우 Fragment는 View 보다 오래 지속되어, 
    // Lifecycle로 인해 메모리 누수 발생할 수 있기 떄문
    
    // Fragment에서 Navigation component 또는 BackStack or detach 사용할 경우 
    // onDestroyView() 이후 Fragment view는 종료되지만, Fragment는 여전히 살아있어 메모리 누수 발생
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(p0: GoogleMap) {
        Log.d("onMapReady()", "Ready to map")
        mGoogleMap = p0;
        mGoogleMap.mapType = GoogleMap.MAP_TYPE_NORMAL // default 노말 생략 가능

        // Max 및 Min Zoom Level 설정
        mGoogleMap.setMaxZoomPreference(20F)
        mGoogleMap.setMinZoomPreference(3F)

        // Marker 생성
        val companyPos = LatLng(37.342189, 127.108483)
        val markerOptions = MarkerOptions()
        markerOptions
            .position(companyPos)
            .title("원하는 위치(위도, 경도)에 마커를 표시했습니다")
            .snippet("여기는 dabinsystems 입니다")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            .alpha(0.5f) // 알파는 좌표의 투명도

        mGoogleMap.getUiSettings().isZoomControlsEnabled()
        mGoogleMap.getUiSettings().isMyLocationButtonEnabled()

        mGoogleMap.addMarker(markerOptions)
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(companyPos, 15F)) //카메라 이동

        // Connect other events
        mGoogleMap.setOnMapClickListener(this)
        mGoogleMap.setOnMapLongClickListener(this)
        mGoogleMap.setOnCameraMoveListener(this)
        mGoogleMap.setOnCameraMoveStartedListener(this)

        mGoogleMap.setOnMarkerClickListener(OnMarkerClickListener { // 마커 클릭 이벤트 처리
            Log.d("Map", "Marker clicked: " + it.getTitle());

            false
        })

        mGoogleMap.setOnPolygonClickListener {
            Log.d("PolygonTouched" ,"polygon tag = " + it.tag.toString() + ", id = " + it.id)
        }

        mGoogleMap.setOnCircleClickListener {
            Log.d("CircleTouched" ,"circle tag = " + it.tag.toString() + ", id = " + it.id)
        }

    }

    override fun onMapClick(p0: LatLng) {
//        Toast.makeText(
//            this, "터치한 지도의 실제 위치입니다. " +
//                    "위도 : " + Math.round(p0?.latitude * 100000) / 100000.0 + ", 경도 : " + Math.round(
//                p0?.longitude * 100000
//            ) / 100000.0, Toast.LENGTH_SHORT
//        ).show()
    }

    override fun onMapLongClick(p0: LatLng) {
    }

    override fun onCameraMove() {
    }

    override fun onCameraMoveStarted(p0: Int) {
    }

    override fun onLocationChanged(location: Location) {
        var lat = location.latitude
        var lon = location.longitude

        Log.d("onLocationChanged", "Location Changed" + lat + "," + lon);

        lateinit var mCurrentMarker: Marker
        if (mCurrentMarker != null) {
            mCurrentMarker.remove()
        }
        var mCurrentLocation = location
        var markerOptions = MarkerOptions()
        markerOptions.position(LatLng(lat, lon))
        mCurrentMarker = mGoogleMap.addMarker(markerOptions)!!

        mGoogleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(mCurrentLocation.latitude, mCurrentLocation.longitude), 18f
            )
        )
        if (isChaseModeOn) {
            var startLatLng = LatLng(lat, lon)
            var endLatLng = LatLng(lat, lon)
            var options =
                PolylineOptions().add(startLatLng).add(endLatLng).width(15F).color(Color.BLACK)
                    .geodesic(true)
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, 18f))
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(): Location? {
        locationManager =
            currentContext.getSystemService(FragmentActivity.LOCATION_SERVICE) as LocationManager
        val providers: List<String> = locationManager.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val l: Location = locationManager.getLastKnownLocation(provider)
                ?: continue
            if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                // Found best last known location: %s", l);
                bestLocation = l
            }
        }
        return bestLocation
    }
}