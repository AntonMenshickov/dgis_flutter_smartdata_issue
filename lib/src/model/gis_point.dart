
class GisPoint {
  final double latitude;
  final double longitude;
  const GisPoint({required this.latitude, required this.longitude});

  Map<String, dynamic> toNativeMap() => {
    'latitude' : latitude,
    'longitude' : longitude
  };
}