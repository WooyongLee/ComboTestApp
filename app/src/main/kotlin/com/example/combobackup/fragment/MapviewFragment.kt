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
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*


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

    lateinit var buttonLeft : Button
    lateinit var buttonRight : Button

    var arrayPoints = ArrayList<LatLng>()
    var polylineOptions = PolylineOptions()
    lateinit var mPolyline: Polyline

    private val binding get() = _binding!!

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

        _binding = MapViewFragmentBinding.inflate(inflater, container, false)

        // region chase & map draw button Click Listener
        buttonLeft.text = "?????? ??????";
        buttonLeft.setOnClickListener(object : View.OnClickListener {
            @SuppressLint("MissingPermission")
            override fun onClick(v: View?) {
                // Toggle Chase Mode
                isChaseModeOn = !isChaseModeOn

                if (isChaseModeOn) {
                    buttonLeft.text = "?????? ??????";

                    // PolyLine ?????? ??????
                    polylineOptions = PolylineOptions()
                    polylineOptions.color(Color.RED)
                    polylineOptions.width(10f)
                    mPolyline = mGoogleMap.addPolyline(polylineOptions)

                    mPolyline.tag = "A"
                    mPolyline.endCap = RoundCap()
                    mPolyline.width = 8f
                    mPolyline.color = Color.RED
                    mPolyline.jointType = JointType.ROUND

                    // ???????????? ???????????? ????????????
                    var location: Location? = getLastKnownLocation()

                    // ?????? GPS??? ????????????.
                    if (location != null) {
                        val provider = location.provider
                        val longitude = location.longitude
                        val latitude = location.latitude
                        val altitude = location.altitude
                        Log.d(
                            "Current Location",
                            "???????????? : $provider ?????? : $longitude ?????? : $latitude ??????  : $altitude"
                        )

                        // GPS Listner ??????
                        locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            1000,
                            1f,
                            gpsLocationListener
                        )
                    }

                    // GPS??? ??? ????????? ?????? ??????????????? ????????????.
                    else {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                        if (location != null) {
                            val provider = location.provider
                            val longitude = location.longitude
                            val latitude = location.latitude
                            val altitude = location.altitude

                            Log.d(
                                "Initialize Current Location",
                                "???????????? : $provider ?????? : $longitude ?????? : $latitude ??????  : $altitude"
                            )
                        } else {
                            Log.d(
                                "Current Location", "Network, GPS Location is null"
                            )
                        }


//                      // ??????????????? ????????? ??????, ???????????? ???????????????.
                        locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            1000,
                            1f,
                            gpsLocationListener
                        )
                    }

                } else {
                    buttonLeft.text = "?????? ??????";
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

        buttonRight.text = "?????? ??????"
        buttonRight.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                mGoogleMap.clear()
            }
        })
        // endregion

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

    // Fragment?????? View Bindnig ????????? ?????? Fragment??? View ?????? ?????? ????????????, 
    // Lifecycle??? ?????? ????????? ?????? ????????? ??? ?????? ??????
    
    // Fragment?????? Navigation component ?????? BackStack or detach ????????? ?????? 
    // onDestroyView() ?????? Fragment view??? ???????????????, Fragment??? ????????? ???????????? ????????? ?????? ??????
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(p0: GoogleMap) {
        Log.d("onMapReady()", "Ready to map")
        mGoogleMap = p0;
        mGoogleMap.mapType = GoogleMap.MAP_TYPE_NORMAL // default ?????? ?????? ??????

        // Max ??? Min Zoom Level ??????
        mGoogleMap.setMaxZoomPreference(20F)
        mGoogleMap.setMinZoomPreference(3F)

        // Marker ??????
        val companyPos = LatLng(37.342189, 127.108483)
        val markerOptions = MarkerOptions()
        markerOptions
            .position(companyPos)
            .title("????????? ??????(??????, ??????)??? ????????? ??????????????????")
            .snippet("????????? dabinsystems ?????????")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            .alpha(0.5f) // ????????? ????????? ?????????

        mGoogleMap.getUiSettings().isZoomControlsEnabled()
        mGoogleMap.getUiSettings().isMyLocationButtonEnabled()

        mGoogleMap.addMarker(markerOptions)
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(companyPos, 15F)) //????????? ??????

        // Connect other events
        mGoogleMap.setOnMapClickListener(this)
        mGoogleMap.setOnMapLongClickListener(this)
        mGoogleMap.setOnCameraMoveListener(this)
        mGoogleMap.setOnCameraMoveStartedListener(this)
    }

    override fun onMapClick(p0: LatLng) {
//        Toast.makeText(
//            this, "????????? ????????? ?????? ???????????????. " +
//                    "?????? : " + Math.round(p0?.latitude * 100000) / 100000.0 + ", ?????? : " + Math.round(
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