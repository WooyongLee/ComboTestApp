package com.example.combobackup.map

import android.graphics.*
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil

class MapDrawer {

    // Marker를 Create 하면서 추가하는 Object들 구성

    // index
    var index = 0;

    // 방향을 나타내는 Marker
    lateinit var triangle : Polygon

    // 지점을 나타내는 Circle
    lateinit var circle : Circle

    // 색상 값
    var hue = 0.0f

    // Object 크기
    var size = 120

    // 기울기 각
    // 기본은 0도 이며, 위쪽을 향하게
    var angle : Double = 0.0

    // Triangle Length
    var LengthOfTriangle = 30

    // 객체의 위치(위-경도(
    var centerPosition: LatLng

    // color properties
    var saturation = 0.5f
    var value = 0.5f

    val defaultTriangleSize = 25.0
    val defaultCircleRadius = 20.0 // 지도 표시상 m

    val radiusOffset = 5.0

    // 원과 Polygon(triangle) 간의 이격 거리
    val shapeMargin = -1.0

    // 원의 중심과 Polygon 중심 간격
    val centerOffset = 0.433

    constructor(index : Int, position: LatLng, hue : Float, angle : Double, googleMap : GoogleMap)
    {
        this.index = index
        this.angle = angle
        this.centerPosition = position

        Log.d("MapDrawer", "index = $index, latitude = ${position.latitude}, longitude = ${position.longitude}, hue = $hue, angle = $angle")

        addTriangleMarker(googleMap, position, hue, angle)
        addCircleMarker(googleMap, position, hue)
    }

    // Object를 제거
    fun removeMarker()
    {
        triangle.remove()
        circle.remove()
    }

    // 삼각형 Marker를 추가, 회전 각도를 적용함
    private fun addTriangleMarker(googleMap: GoogleMap, position: LatLng, hue : Float, angle : Double) {
        val markerOptions = MarkerOptions()
            .position(position)
            .title("Triangle Marker")
            .icon(createTriangleIcon(hue))
            .rotation(angle.toFloat())

        // 추가 Dependencies ;     compile 'com.google.maps.android:android-maps-utils:0.6.2'
        val shapeSize = defaultCircleRadius + defaultTriangleSize * centerOffset + shapeMargin

        val pointOnCircle = SphericalUtil.computeOffset(this.centerPosition, shapeSize, angle)
        val triangleOptions = PolygonOptions()
            .add(SphericalUtil.computeOffset(pointOnCircle, defaultTriangleSize / 2, angle + 0.0)) // 위쪽 꼭지점
            .add(SphericalUtil.computeOffset(pointOnCircle, defaultTriangleSize / 2, angle + 120.0)) // 왼쪽 꼭지점
            .add(SphericalUtil.computeOffset(pointOnCircle, defaultTriangleSize / 2, angle + 240.0)) // 오른쪽 꼭지점
            .strokeWidth(0f)
            .strokeColor(Color.HSVToColor(floatArrayOf(hue, saturation, value)))
            .fillColor(Color.HSVToColor(floatArrayOf(hue, saturation, value)))
            .clickable(true)

        triangle = googleMap.addPolygon(triangleOptions)!!
        triangle.tag = index.toString()
        // triangle = googleMap.addMarker(markerOptions)!!
    }

    // 원형 Marker를 추가
    private fun addCircleMarker(googleMap: GoogleMap, position: LatLng, hue : Float) {
        val markerOptions = MarkerOptions()
            .position(position)
            .title("Circle Marker")
            .icon(BitmapDescriptorFactory.defaultMarker(hue))

        val circleOption = CircleOptions()
            .center(position)
            .radius(defaultCircleRadius) // meter
            .strokeColor(Color.HSVToColor(floatArrayOf(hue, saturation, value)))
            .strokeWidth(0.01f)
            .fillColor(Color.HSVToColor(floatArrayOf(hue, saturation, value)))
            .clickable(true)

        // googleMap.addMarker(markerOptions)
        circle = googleMap.addCircle(circleOption)
    }

    // 삼각형 아이콘을 그림
    private fun createTriangleIcon(hue : Float, zoomLv : Float = 13f): BitmapDescriptor {
        val size = (defaultTriangleSize * zoomLv).toInt()
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.HSVToColor(floatArrayOf(hue, saturation, value))
        paint.style = Paint.Style.FILL

        // 단말 센서의 angle 에 대해서 triangle의 위치 이동
        var xAngle = Math.sin(angle).toFloat() * (defaultCircleRadius + radiusOffset).toFloat()
        var yAngle = Math.cos(angle).toFloat() * (defaultCircleRadius + radiusOffset).toFloat()

        Log.d("createTriangleIcon()", "size = $size, xAngle = $xAngle, yAngle = $yAngle")

        val path = createTrianglePath()

        // region old (주석)
//        path.moveTo(size.toFloat() / 2 + xAngle, 0f + yAngle) // beginning of the next contour
//        path.lineTo(size.toFloat() / 2, size.toFloat() / 2) // triangle height
//        path.lineTo(0f, 0f)         // triangle width 1
//        path.lineTo(size.toFloat(), 0f) // triangle width 2
        // endregion

        canvas.drawPath(path, paint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun createTrianglePath(): Path {
        val path = Path()
        val vertices = arrayOfNulls<Point>(3)

        // 각 꼭지점의 위치 계산
        for (i in 0..2) {
            val heading = (i * 120 + angle) // 중앙으로 부터 0 - 120 - 240도 방향으로 Vertice 설정
            vertices[i] = Point(
                (centerPosition.latitude + LengthOfTriangle / 2 * Math.cos(Math.toRadians(heading))).toInt(),
                (centerPosition.longitude + LengthOfTriangle / 2 * Math.sin(Math.toRadians(heading))).toInt()
            )
        }

        // Path에 삼각형 그리기
        path.moveTo(vertices[0]!!.x.toFloat(), vertices[0]!!.y.toFloat())
        path.lineTo(vertices[1]!!.x.toFloat(), vertices[1]!!.y.toFloat())
        path.lineTo(vertices[2]!!.x.toFloat(), vertices[2]!!.y.toFloat())
        path.close()
        return path
    }

}