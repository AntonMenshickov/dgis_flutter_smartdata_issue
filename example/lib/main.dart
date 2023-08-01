import 'dart:developer';
import 'dart:typed_data';
import 'dart:ui';
import 'package:dgis_flutter/dgis_flutter.dart';
import 'package:dgis_flutter_example/assets_constant.dart';
import 'package:flutter/material.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      home: GisScreen(),
    );
  }
}

class GisScreen extends StatefulWidget {
  const GisScreen({Key? key}) : super(key: key);

  @override
  State<GisScreen> createState() => _GisScreenState();
}

class _GisScreenState extends State<GisScreen> {
  final GisMapController controller = GisMapController();

  late final Future<List<GisMapMarker>> icons;
  List<GisMapMarker> list = [];

  @override
  void initState() {
    icons =
        Future.wait([getPngFromAsset(context, AssetPath.iconsPointGrey, 60)])
            .then((value) => [
                  GisMapMarker(
                      icon: value[0],
                      latitude: 52.29778,
                      longitude: 104.29639,
                      zIndex: 0,
                      id: "123456")
                ]);
    super.initState();
  }

  Future<Uint8List> getPngFromAsset(
    BuildContext context,
    String path,
    int width,
  ) async {
    ByteData data = await DefaultAssetBundle.of(context).load(path);
    Codec codec = await instantiateImageCodec(
      data.buffer.asUint8List(),
      targetWidth: width,
    );
    FrameInfo fi = await codec.getNextFrame();
    return (await fi.image.toByteData(format: ImageByteFormat.png))!
        .buffer
        .asUint8List();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      floatingActionButton: Row(
        mainAxisAlignment: MainAxisAlignment.end,
        children: [
          FloatingActionButton(
            child: const Icon(Icons.zoom_in_outlined),
            onPressed: () async {
              final status = await controller.increaseZoom(duration: 200);
              log(status);
            },
          ),
          FloatingActionButton(
            child: const Icon(Icons.zoom_out_outlined),
            onPressed: () async {
              final status = await controller.reduceZoom(duration: 200);
              log(status);
            },
          ),
          FloatingActionButton(
            child: const Icon(Icons.add),
            onPressed: () async {
              final status = await controller.setRoute(RoutePosition(
                  finishLatitude: 55.752425,
                  finishLongitude: 37.613983,
                  startLatitude: 55.759909,
                  startLongitude: 37.618806));
              log(status);
            },
          ),
          FloatingActionButton(
            child: const Icon(Icons.remove),
            onPressed: () async {
              final status = await controller.removeRoute();
              log(status);
            },
          ),
        ],
      ),
      body: Center(
          child: FutureBuilder<List<GisMapMarker>>(
        future: icons,
        builder: (context, snapshot) {
          if (!snapshot.hasData) return const SizedBox();
          list = snapshot.data!;
          return GisMap(
            typeView: TypeView.platformView,
            controller: controller,
            onTapMarker: (marker) {
              // ignore: avoid_print
              print(marker.id);
            },
            startCameraPosition: const GisCameraPosition(
              latitude: 52.29778,
              longitude: 104.29639,
              bearing: 85.0,
              tilt: 25.0,
              zoom: 14.0,
            ),
          );
        },
      )),
    );
  }
}
