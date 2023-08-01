class GisCameraPosition {
  final double latitude;
  final double longitude;
  final double zoom;
  final double tilt;
  final double bearing;

  const GisCameraPosition(
      {required this.latitude,
      required this.longitude,
      this.bearing = 0.0,
      this.tilt = 0.0,
      this.zoom = 0.0});

  factory GisCameraPosition.fromJson(Map json) => GisCameraPosition(
        latitude: json['latitude'] ?? 0.0,
        longitude: json['longitude'] ?? 0.0,
        bearing: json['bearing'] ?? 0.0,
        tilt: json['tilt'] ?? 0.0,
        zoom: json['zoom'] ?? 0.0,
      );

  Map<String, dynamic> toNativeMap()=> {
    'latitude': latitude,
    'longitude': longitude,
    'bearing': bearing,
    'tilt': tilt,
    'zoom': zoom,
  } ;

  GisCameraPosition copyWith({
    double? latitude,
    double? longitude,
    double? zoom,
    double? tilt,
    double? bearing,
  }) =>
      GisCameraPosition(
          latitude: latitude ?? this.latitude,
          longitude: longitude ?? this.longitude,
          bearing: bearing ?? this.bearing,
          tilt: tilt ?? this.tilt,
          zoom: zoom ?? this.zoom);
}
