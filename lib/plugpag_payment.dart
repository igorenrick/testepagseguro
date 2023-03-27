import 'dart:async';

import 'package:flutter/services.dart';

class PlugPagPayment {
  static String status = "";
  static bool makingPayment = false;

  static final StreamController<String> _statusController =
      StreamController<String>.broadcast();

  static final StreamController<bool> _makingPaymentController =
      StreamController<bool>.broadcast();

  static Stream<String> get statusStream => _statusController.stream;

  static Stream<bool> get makingPaymentStream =>
      _makingPaymentController.stream;

  static const MethodChannel _channel = MethodChannel(
    "plugpag_payment",
  );

  static Future<String> abortPayment() async {
    String success;
    try {
      success = await _channel.invokeMethod("abortPayment");
      makingPayment = false;
      _makingPaymentController.sink.add(makingPayment);
      _statusController.sink.add(success);
    } on PlatformException catch (e) {
      status = e.message!;
      _statusController.sink.add(status);
      return e.message!;
    }
    return success;
  }

  static Future<void> makePayment(
    String type,
    double amount,
    String reference,
  ) async {
    makingPayment = true;
    _makingPaymentController.sink.add(makingPayment);

    _channel.setMethodCallHandler((MethodCall call) async {
      if (call.method == "statusUpdate") {
        status = call.arguments;
        _statusController.sink.add(status);
        if (status.contains("TRANSAÇÃO AUTORIZADA") ||
            status.contains("PAGAMENTO FINALIZADO") ||
            status == "0,") {
          makingPayment = false;
          _makingPaymentController.sink.add(makingPayment);
        }
      }
    });

    String success;

    try {
      await _channel.invokeMethod("makePayment", {
        "type": type,
        "amount": amount,
        "reference": reference,
        "activationCode": "403938",
      });
    } on PlatformException catch (e) {
      status = e.message!;
      _statusController.sink.add(status);
      makingPayment = false;
      _makingPaymentController.sink.add(makingPayment);
    }
  }
}
