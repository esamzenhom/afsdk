import 'afsdk_platform_interface.dart';
import 'dart:typed_data';

class Afsdk {
  Future<String?> getPlatformVersion() {
    return AfsdkPlatform.instance.getPlatformVersion();
  }

  Future<void> print(Uint8List image) {
    return AfsdkPlatform.instance.print(image);
  }

  Future<void> beepBuzzer() {
    return AfsdkPlatform.instance.beepBuzzer();
  }

  Future<Map<String, dynamic>?> readCard() {
    return AfsdkPlatform.instance.readCard();
  }
}
