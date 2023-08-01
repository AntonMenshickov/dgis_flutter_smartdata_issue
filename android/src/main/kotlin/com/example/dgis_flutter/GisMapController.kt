package com.example.dgis_flutter

import android.graphics.BitmapFactory
import io.flutter.Log
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import ru.dgis.sdk.Context
import ru.dgis.sdk.Duration
import ru.dgis.sdk.coordinates.*
import ru.dgis.sdk.geometry.ComplexGeometry
import ru.dgis.sdk.geometry.GeoPointWithElevation
import ru.dgis.sdk.geometry.PointGeometry
import ru.dgis.sdk.map.*
import ru.dgis.sdk.routing.*
import java.io.ByteArrayInputStream

class GisMapController(gv: MapView, ctx: Context) {

    private var gisView = gv
    private var sdkContext = ctx
    private var listIconObjects: List<SimpleMapObject>? = null
    private var polylineObject: SimpleMapObject? = null


    fun setCameraPosition(call: MethodCall, result: MethodChannel.Result) {
        val args: Map<String, Any?> = call.arguments as Map<String, Any?>
        val cameraPosition = CameraPosition(
            GeoPoint(
                latitude = Latitude(value = args["latitude"] as Double),
                longitude = Longitude(value = args["longitude"] as Double)
            ),
            zoom = Zoom(value = (args["zoom"] as Double).toFloat()),
            bearing = Bearing(value = args["bearing"] as Double),
            tilt = Tilt(value = (args["tilt"] as Double).toFloat())
        )
        gisView.getMapAsync { map ->
            map.camera.move(
                cameraPosition,
                Duration.ofMilliseconds((args["duration"] as Int).toLong()),
                CameraAnimationType.LINEAR
            )
                .onResult {
                    Log.d("APP", "Перелёт камеры завершён.")
                    result.success("OK")
                }
        }
    }

    fun getCameraPosition(result: MethodChannel.Result) {
        lateinit var cameraPosition: CameraPosition;
        gisView.getMapAsync { map ->
            cameraPosition = map.camera.position;
            val data = mapOf(
                "latitude" to cameraPosition.point.latitude.value,
                "longitude" to cameraPosition.point.longitude.value,
                "bearing" to cameraPosition.bearing.value,
                "tilt" to cameraPosition.tilt.value,
                "zoom" to cameraPosition.zoom.value,
            )
            result.success(data);
        }
    }

    fun updateMarkers(arguments: Any, mapObjectManager: MapObjectManager) {
        val args = arguments as Map<String, Any>;
        val markers = args["markers"] as List<Map<String, Any>>
        val objects: MutableList<SimpleMapObject> = ArrayList();
        for (i in markers) {
            val arrayInputStream = ByteArrayInputStream(i["icon"] as ByteArray?)
            val bitmap = BitmapFactory.decodeStream(arrayInputStream)
            val icon = imageFromBitmap(sdkContext, bitmap)
            val marker = Marker(
                MarkerOptions(
                    position = GeoPointWithElevation(
                        latitude = i["latitude"] as Double,
                        longitude = i["longitude"] as Double,
                    ),
                    icon = icon,
                    zIndex = ZIndex(i["zIndex"] as Int),
                    userData = i["id"],
                )
            )
            objects.add(marker)
        }
        if (listIconObjects != null) {
            mapObjectManager.removeObjects(listIconObjects!!)
        }
        mapObjectManager.addObjects(objects.toList());
        listIconObjects = objects.toList()

    }

    fun setPolyline(
        arguments: Any,
        mapObjectManager: MapObjectManager,
        result: MethodChannel.Result
    ) {
        val args = arguments as Map<String, Any>
        val p = args["points"] as List<Map<String, Any>>
        val points: MutableList<GeoPoint> = mutableListOf()
        for (element in p) {
            points.add(
                GeoPoint(
                    latitude = (element["latitude"] as Double),
                    longitude = (element["longitude"] as Double),
                )
            )
        }
        // Создание линии
        val polyline = Polyline(
            PolylineOptions(
                points = points,
                width = 4.lpx,
                color = Color(0, 0, 255)
            )
        )
        if (polylineObject != null) {
            mapObjectManager.removeObject(polylineObject!!)
        }
        mapObjectManager.addObject(polyline)
        polylineObject = polyline
        val geometry = ComplexGeometry(points.map { PointGeometry(it) })
        gisView.getMapAsync { map ->
            val position = calcPosition(map.camera, geometry, padding = Padding(top = 50, bottom = 50, left = 50, right = 50))
            map.camera.move(position, Duration.ofMilliseconds(200),
                CameraAnimationType.LINEAR)
        }
        result.success("OK")
    }

    fun removePolyline(mapObjectManager: MapObjectManager, result: MethodChannel.Result) {
        if (polylineObject != null) {
            mapObjectManager.removeObject(polylineObject!!)
        }
        result.success("OK")
    }
}

