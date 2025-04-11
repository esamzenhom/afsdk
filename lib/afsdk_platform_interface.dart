import 'dart:typed_data';

import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'afsdk_method_channel.dart';

abstract class AfsdkPlatform extends PlatformInterface {
  /// Constructs a AfsdkPlatform.
  AfsdkPlatform() : super(token: _token);

  static final Object _token = Object();

  static AfsdkPlatform _instance = MethodChannelAfsdk();

  /// The default instance of [AfsdkPlatform] to use.
  ///
  /// Defaults to [MethodChannelAfsdk].
  static AfsdkPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [AfsdkPlatform] when
  /// they register themselves.
  static set instance(AfsdkPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<void> print(Uint8List image) {
    throw UnimplementedError('print() has not been implemented.');
  }

  Future<void> beepBuzzer() {
    throw UnimplementedError('beepBuzzer() has not been implemented.');
  }

  Future<Map<String, dynamic>?> scanCode() {
    throw UnimplementedError('scanCode() has not been implemented.');
  }

  Future<Map<String, dynamic>?> readCard() {
    throw UnimplementedError('readCard() has not been implemented.');
  }
}
