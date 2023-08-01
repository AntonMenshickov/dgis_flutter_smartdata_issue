package com.example.dgis_flutter

import android.Manifest
import android.content.Context
import io.flutter.Log
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import ru.dgis.sdk.DGis
import ru.dgis.sdk.coordinates.Bearing
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.coordinates.Latitude
import ru.dgis.sdk.coordinates.Longitude
import ru.dgis.sdk.map.*
import ru.dgis.sdk.positioning.registerPlatformMagneticSource
import android.content.pm.PackageManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import ru.dgis.sdk.navigation.NavigationManager
import ru.dgis.sdk.positioning.registerPlatformLocationSource
import ru.dgis.sdk.routing.*
import ru.dgis.sdk.traffic.TrafficControl

internal class NativeView(
    context: Context,
    id: Int,
    creationParams: Map<String, Any?>?,
    messenger: BinaryMessenger
) :
    PlatformView, MethodChannel.MethodCallHandler {
    private var methodChannel: MethodChannel
    private var mapObjectManager: MapObjectManager? = null
    private var gisView: MapView
    private var controller: GisMapController
    private var routeEditor: RouteEditor


    private var sdkContext: ru.dgis.sdk.Context = DGis.initialize(
        context,
    )

    override fun getView(): MapView {
        return gisView
    }

    override fun dispose() {
    }

    init {
        // Запрос разрешений, и регистрация сервисов локации
        registerServices(context)
        setupPermissions(context)

        val mapOptions = MapOptions()
        val startPoint = GeoPoint(
            latitude = Latitude(creationParams?.get("latitude") as Double),
            longitude = Longitude(creationParams["longitude"] as Double),
        )
        mapOptions.position = CameraPosition(
            point = startPoint,
            zoom = Zoom((creationParams["zoom"] as Double).toFloat()),
            tilt = Tilt((creationParams["tilt"] as Double).toFloat()),
            bearing = Bearing((creationParams["bearing"] as Double)),
        )
        // Создаем канал для общения..
        methodChannel = MethodChannel(messenger, "fgis")
        methodChannel.setMethodCallHandler(this)
        gisView = MapView(context, mapOptions)
//        GisMapSession.setMapView(gisView)
        routeEditor = RouteEditor(sdkContext)
        controller = GisMapController(gisView, sdkContext)
        gisView.getMapAsync { map ->
            val minMaxZoom = CameraZoomRestrictions(
                minZoom = Zoom(value = 3.0f),
                maxZoom = Zoom(value = 20.0f)
            )
            map.camera.zoomRestrictions = minMaxZoom
            if (mapObjectManager == null) {
                mapObjectManager = MapObjectManager(map)
            }
            gisView.setTheme("day")
            gisView.setTouchEventsObserver(object : TouchEventsObserver {
                override fun onTap(point: ScreenPoint) {
                    map.getRenderedObjects(point, ScreenDistance(1f))
                        .onResult { renderedObjectInfos ->
                            for (renderedObjectInfo in renderedObjectInfos) {
                                if (renderedObjectInfo.item.item.userData != null) {
                                    val args = mapOf(
                                        "id" to renderedObjectInfo.item.item.userData
                                    )
                                    Log.d(
                                        "DGIS",
                                        "Нажатие на маркер"
                                    )
                                    methodChannel.invokeMethod(
                                        "ontap_marker",
                                        args
                                    )
                                }
                            }
                        }
                    super.onTap(point)
                }
            })
            val source = MyLocationMapObjectSource(
                sdkContext,
                MyLocationDirectionBehaviour.FOLLOW_SATELLITE_HEADING,
                createSmoothMyLocationController()
            )
            map.addSource(source)
            val trafficSource = TrafficSource(sdkContext)
            map.addSource(trafficSource)
            val trafficControl = TrafficControl(context, null, 0)
            val  view = LinearLayout(context)

            view.setPadding(15, 0, 0, 15)
            view.gravity = Gravity.BOTTOM
            view.addView(trafficControl)
            this.gisView.addView(view);



            val navigationManager = NavigationManager(sdkContext)
            navigationManager.start()
        }
    }


    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "getCameraPosition" -> {
                controller.getCameraPosition(result = result)
            }
            "setCameraPosition" -> {
                controller.setCameraPosition(call = call, result = result)
            }
            "updateMarkers" -> {
                val args = call.arguments
                if (mapObjectManager == null) {
                    gisView.getMapAsync { map ->
                        mapObjectManager = MapObjectManager(map)
                        controller.updateMarkers(
                            arguments = args,
                            mapObjectManager = mapObjectManager!!
                        )
                    }
                } else {
                    controller.updateMarkers(
                        arguments = args,
                        mapObjectManager = mapObjectManager!!
                    )
                }
            }
            "setRoute" -> {
                setRoute(arguments = call.arguments, result = result)
            }
            "removeRoute" -> {
                removeRoute(result = result)
            }
            "setPolyline" -> {
                if (mapObjectManager == null) {
                    gisView.getMapAsync { map ->
                        mapObjectManager = MapObjectManager(map)
                        controller.setPolyline(
                            arguments = call.arguments,
                            mapObjectManager = mapObjectManager!!,
                            result = result
                        )
                        result.success("OK")
                    }
                } else {
                    controller.setPolyline(
                        arguments = call.arguments,
                        mapObjectManager = mapObjectManager!!,
                        result = result
                    )
                    result.success("OK")
                }

            }
            "removePolyline" -> {
                if (mapObjectManager == null) {
                    gisView.getMapAsync { map ->
                        mapObjectManager = MapObjectManager(map)
                        controller.removePolyline(mapObjectManager!!, result = result)
                    }
                } else {
                    controller.removePolyline(mapObjectManager!!, result = result)
                }

            }
        }
    }

    private fun registerServices(applicationContext: Context) {
        val compassSource = CustomCompassManager(applicationContext)
        registerPlatformMagneticSource(sdkContext, compassSource)

        val locationSource = CustomLocationManager(applicationContext)
        registerPlatformLocationSource(sdkContext, locationSource)
    }

    private fun setupPermissions(context: Context) {
        val permission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun setRoute(arguments: Any, result: MethodChannel.Result) {
        arguments as Map<String, Any>
        val routeEditorSource = RouteEditorSource(sdkContext, routeEditor)
        val startPoint = RouteSearchPoint(
            coordinates = GeoPoint(
                latitude = arguments["startLatitude"] as Double,
                longitude = arguments["startLongitude"] as Double
            )
        )
        val finishPoint = RouteSearchPoint(
            coordinates = GeoPoint(
                latitude = arguments["finishLatitude"] as Double,
                longitude = arguments["finishLongitude"] as Double
            )
        )
        routeEditor.setRouteParams(
            RouteEditorRouteParams(
                startPoint = startPoint,
                finishPoint = finishPoint,
                routeSearchOptions = RouteSearchOptions(
                    CarRouteSearchOptions(

                    )
                )
            )
        )
        gisView.getMapAsync { map ->
            for (s in map.sources) {
                if (s is RouteEditorSource) {
                    map.removeSource(s)
                }
            }
            map.addSource(routeEditorSource)
            result.success("OK")
        }
    }

    private fun removeRoute(result: MethodChannel.Result) {
        gisView.getMapAsync { map ->
            for (s in map.sources) {
                if (s is RouteEditorSource) {
                    map.removeSource(s)
                }
            }
            result.success("OK")
        }
    }

}
