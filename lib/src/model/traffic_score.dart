class TrafficScore {
  final int? value;

  const TrafficScore({this.value});

  factory TrafficScore.fromJson(Map json) => TrafficScore(
        value: json['value'],
      );

  Map<String, dynamic> toNativeMap() => {
        'value': value,
      };
}
