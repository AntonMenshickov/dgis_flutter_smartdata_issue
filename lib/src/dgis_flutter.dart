import 'package:dgis_flutter/dgis_flutter.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';

enum TypeView { androidView, platformView }

class GisMap extends StatefulWidget {
  final GisCameraPosition startCameraPosition;
  final GisMapController controller;
  final Function(GisMapMarker) onTapMarker;
  final TypeView typeView;

  const GisMap(
      {Key? key,
      required this.startCameraPosition,
      required this.controller,
      this.typeView = TypeView.platformView,
      required this.onTapMarker})
      : super(key: key);

  @override
  State<GisMap> createState() => _GisMapState();
}

class _GisMapState extends State<GisMap> {
  final _methodChannel = const MethodChannel("fgis");

  @override
  initState() {
    _methodChannel
        .setMethodCallHandler((MethodCall call) => _handleMethodCall(call));
    super.initState();
  }

  Future<dynamic> _handleMethodCall(MethodCall call) async {
    switch (call.method) {
      case 'ontap_marker':
        String id = call.arguments['id'];
        final list = widget.controller.listMarker;
        widget.onTapMarker(list.firstWhere((element) => element.id == id));
        break;
      default:
        throw MissingPluginException();
    }
  }

  @override
  Widget build(BuildContext context) {
    // This is used in the platform side to register the view.
    const String viewType = '<gis-view>';
    // Pass parameters to the platform side.
    Map<String, dynamic> creationParams = {
      'latitude': widget.startCameraPosition.latitude,
      'longitude': widget.startCameraPosition.longitude,
      'zoom': widget.startCameraPosition.zoom,
      'tilt': widget.startCameraPosition.tilt,
      'bearing': widget.startCameraPosition.bearing,
    };
    if (widget.typeView == TypeView.androidView) {
      return AndroidView(
        viewType: viewType,
        layoutDirection: TextDirection.ltr,
        creationParams: creationParams,
        creationParamsCodec: const StandardMessageCodec(),
      );
    } else {
      return PlatformViewLink(
        viewType: viewType,
        surfaceFactory: (context, controller) {
          return AndroidViewSurface(
            controller: controller as AndroidViewController,
            gestureRecognizers: const <Factory<OneSequenceGestureRecognizer>>{},
            hitTestBehavior: PlatformViewHitTestBehavior.opaque,
          );
        },
        onCreatePlatformView: (params) {
          return PlatformViewsService.initSurfaceAndroidView(
            id: params.id,
            viewType: viewType,
            layoutDirection: TextDirection.ltr,
            creationParams: creationParams,
            creationParamsCodec: const StandardMessageCodec(),
            onFocus: () {
              params.onFocusChanged(true);
            },
          )
            ..addOnPlatformViewCreatedListener(params.onPlatformViewCreated)
            ..create();
        },
      );
    }
  }
}
