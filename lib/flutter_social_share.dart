import 'dart:async';

import 'package:flutter/services.dart';

class FlutterSocialShare {
  static const MethodChannel _channel =
      const MethodChannel('flutter_social_share');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  ///share to facebook
  static Future<String> shareToFacebook({String url = '', String msg = ''}) async {
    final Map<String, Object> arguments = Map<String, dynamic>();
    arguments.putIfAbsent('msg', () => msg);
    arguments.putIfAbsent('url', () => url);
    dynamic result;
    try {
      result = await _channel.invokeMethod('shareFacebook', arguments);
    } catch (e) {
      return "false";
    }
    return result;
  }

  static Future<String> sharePhotoToFacebook({String url = '', String title = '', String description = ''}) async {
    final Map<String, Object> arguments = Map<String, dynamic>();
    arguments.putIfAbsent('uri', () => url);
    arguments.putIfAbsent('title', () => title);
    arguments.putIfAbsent('description', () => description);
    dynamic result;
    try {
      result = await _channel.invokeMethod('shareFacebookPhoto', arguments);
    } catch (e) {
      return "false";
    }
    return result;
  }

  static Future<String> shareVideoToFacebook({String url = '', String title = '', String description = ''}) async {
    final Map<String, Object> arguments = Map<String, dynamic>();
    arguments.putIfAbsent('uri', () => url);
    arguments.putIfAbsent('title', () => title);
    arguments.putIfAbsent('description', () => description);
    dynamic result;
    try {
      result = await _channel.invokeMethod('shareFacebookVideo', arguments);
    } catch (e) {
      return "false";
    }
    return result;
  }

  ///use system share ui
  static Future<String> shareToSystem({String msg}) async {
    Map<String, Object> arguments = Map<String, dynamic>();
    arguments.putIfAbsent('msg', () => msg);
    dynamic result;
    try {
      result = await _channel.invokeMethod('system', {'msg': msg});
    } catch (e) {
      return "false";
    }
    return result;
  }
}
