import 'dart:async';

import 'package:flutter/services.dart';

class AndroidInAppUpdate {
  static const MethodChannel _channel =
  const MethodChannel('android_in_app_update');

  static Future<UpdateStatus> get updateStatus async {
    final int status = await _channel.invokeMethod('checkUpdateStatus');
    switch (status) {
      case 0:
        return UpdateStatus.UNKNOWN;
      case 1:
        return UpdateStatus.RUNNING;
      case 2:
        return UpdateStatus.DOWNLOADED;
      case 3:
        return UpdateStatus.AVAILABLE;
      default:
        return UpdateStatus.NOT_AVAILABLE;
    }
  }

  static Future<UpdateResult> startFlexibleUpdate() async {
    final int responce = await _channel.invokeMethod('startFlexibleUpdate');
    switch (responce) {
      case -1:
        return UpdateResult.OK;
      case 0:
        return UpdateResult.CANCELED;
      default:
        return UpdateResult.ERROR;
    }
  }

  static Future<UpdateResult> startImmediateUpdate() async {
    final int responce = await _channel.invokeMethod('startImmediateUpdate');
    switch (responce) {
      case -1:
        return UpdateResult.OK;
      case 0:
        return UpdateResult.CANCELED;
      default:
        return UpdateResult.ERROR;
    }
  }

  static Future<UpdateResult> installFlexibleUpdate() async {
    final int responce = await _channel.invokeMethod('installFlexibleUpdate');
    switch (responce) {
      case -1:
        return UpdateResult.OK;
      case 0:
        return UpdateResult.CANCELED;
      default:
        return UpdateResult.ERROR;
    }
  }
}

enum UpdateStatus {
  UNKNOWN,
  RUNNING,
  DOWNLOADED,
  AVAILABLE,
  NOT_AVAILABLE
}

enum UpdateResult {
  OK,
  CANCELED,
  ERROR,
}