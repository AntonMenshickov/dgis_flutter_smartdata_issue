import 'dart:typed_data';

class GisMapMarker {
  final double latitude;
  final double longitude;
  final Uint8List icon;
  final int zIndex;
  final String id;

  GisMapMarker(
      {required this.latitude,
      required this.longitude,
      required this.icon,
      required this.id,
      required this.zIndex});

  Map<String, dynamic> toJson() => {
        'latitude': latitude,
        'longitude': longitude,
        'icon': icon,
        'id': id,
        'zIndex': zIndex
      };
}

class RoutePosition {
  final double startLatitude;
  final double startLongitude;
  final double finishLatitude;
  final double finishLongitude;

  RoutePosition(
      {required this.finishLatitude,
      required this.finishLongitude,
      required this.startLatitude,
      required this.startLongitude});

  Map<String, dynamic> toJson()=>{
    "startLatitude" : startLatitude,
    "startLongitude" : startLongitude,
    "finishLatitude" : finishLatitude,
    "finishLongitude" : finishLongitude
  };
}
