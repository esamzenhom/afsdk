import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'afsdk_platform_interface.dart';

/// An implementation of [AfsdkPlatform] that uses method channels.
class MethodChannelAfsdk extends AfsdkPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('afsdk');

  @override
  Future<String?> getPlatformVersion() async {
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<void> print(Uint8List image) async {
    final version =
        await methodChannel.invokeMethod<void>('printImage', {"image": image});
    return version;
  }

  @override
  Future<void> beepBuzzer() async {
    final version = await methodChannel.invokeMethod<void>('beepBuzzer');
    return version;
  }

  @override
  Future<Map<String, dynamic>?> scanCode() async {
    final data =
        await methodChannel.invokeMapMethod<String, dynamic>('scanCode');
    return data;
  }

  @override
  Future<Map<String, dynamic>?> readCard() async {
    final data =
        await methodChannel.invokeMapMethod<String, dynamic>('readCard');
    return data;
  }
}
