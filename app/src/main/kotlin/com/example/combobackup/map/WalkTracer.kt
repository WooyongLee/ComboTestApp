package com.example.combobackup.map

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import com.google.android.gms.maps.GoogleMap
import java.util.*
import kotlin.concurrent.timer

// 이동에 따라 위치를 체크하여 지도위에 그려주는 모듈
class WalkTracer {

    // lateinit var lm : LocationManager

    var gpsLocationListener = LocationListener() {
        fun onLocationChanged(location : Location)
        {
            var provider = location.provider // 위치정보
            var longitude = location.longitude; // 위도
            var latitude = location.latitude; // 경도
            var altitude = location.altitude // 고도

            // draw current position

        }
    }


    constructor(mGoogleMap : GoogleMap)
    {
        // lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE)

        timer(period = 1000, initialDelay = 1000)
        {
            // if is GoogleMap Available?

            // Get Current Position

            // Set Point and Draw on Map


        }
    }

    // 추후 종료에 대한 부분 고려할 것
}