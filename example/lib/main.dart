import 'dart:math';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:afsdk/afsdk.dart';
import 'dart:ui' as ui;

import 'package:intl/intl.dart';
import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _afsdkPlugin = Afsdk();
  String debugData = "Logs will be here";

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            Padding(
              padding: const EdgeInsets.all(16.0),
              child: Container(
                height: 250,
                width: double.infinity,
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.all(Radius.circular(10)),
                  color: Colors.black12,
                ),
                child: Padding(
                  padding: const EdgeInsets.all(12.0),
                  child: Text(
                    debugData,
                    style: TextStyle(color: Colors.black, fontSize: 18.0),
                    textAlign: TextAlign.start,
                  ),
                ),
              ),
            ),
            ElevatedButton(
              onPressed: () {
                createTextBitmap(printText1(), 384, 1230, false).then(
                  (image) {
                    _afsdkPlugin.print(image).then((_) {
                      debugPrint("Print 1111 success");
                    }).onError(
                      (error, stackTrace) {
                        debugPrint("Print 1111 failed :: $error");
                        debugPrint(stackTrace.toString());
                      },
                    );
                  },
                ).onError(
                  (error, stackTrace) {},
                );
              },
              child: Text("Print"),
            ),
            SizedBox(height: 8),
            ElevatedButton(
              onPressed: () {
                _afsdkPlugin.beepBuzzer().then((_) {
                  debugPrint("Buzzer 1111 success");
                }).onError(
                  (error, stackTrace) {
                    debugPrint("Buzzer 1111 failed :: $error");
                    debugPrint(stackTrace.toString());
                  },
                );
              },
              child: Text("Buzzer"),
            ),
            SizedBox(height: 8),
            ElevatedButton(
              onPressed: () {
                requestCameraPermission();
              },
              child: Text("Scan Code"),
            ),
            SizedBox(height: 8),
            ElevatedButton(
              onPressed: () {
                _afsdkPlugin.readCard().then((result) {
                  setState(() {
                    debugData = result.toString();
                  });
                  debugPrint("Read Card 1111 success");
                }).onError(
                  (error, stackTrace) {
                    debugPrint("Read Card 1111 failed :: $error");
                    debugPrint(stackTrace.toString());
                  },
                );
              },
              child: Text("Read Card"),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> requestCameraPermission() async {
    var status = await Permission.camera.status;

    if (!status.isGranted) {
      status = await Permission.camera.request();
    }

    if (status.isGranted) {
      print("✅ Camera permission granted");
      _afsdkPlugin.scanCode().then((result) {
        setState(() {
          debugData = result.toString();
        });
        debugPrint("Scan Code 1111 success");
      }).onError(
        (error, stackTrace) {
          debugPrint("Scan Code 1111 failed :: $error");
          debugPrint(stackTrace.toString());
        },
      );
    } else if (status.isPermanentlyDenied) {
      print("❌ Permission permanently denied. Show dialog to open settings.");
      // openAppSettings(); // Optionally prompt to open settings
    } else {
      print("❌ Camera permission denied");
    }
  }

  String printText() {
    DateTime currentDate = DateTime.now();
    String dateTime = DateFormat("yyyy-MM-dd HH:mm:ss").format(currentDate);
    String refId = DateFormat("yyyyMMddHHmmss").format(currentDate) +
        generateRandomNumber(4);

    StringBuffer sbMsg = StringBuffer();
    sbMsg.writeln("                          TEST");
    sbMsg.writeln("------------------------------------------------------");
    sbMsg.writeln("\t\t\t$dateTime");
    sbMsg.writeln("------------------------------------------------------");
    sbMsg.writeln("                          SALE\n");
    sbMsg.writeln(
        "MERCHANT NAME:\n                               001420183990573");
    sbMsg.writeln(
        "TERMINAL NO:\n                                              00026715");
    sbMsg.writeln(
        "OPERATOR NO:\n                                              12345678");
    sbMsg.writeln("ISSUER:\n                              01020001");
    sbMsg.writeln("CARD NO:\n                      9558803602109503920");
    sbMsg.writeln("ACQUIRER:\n                              03050011");
    sbMsg
        .writeln("TXN. TYPE:\n                                           SALE");
    sbMsg.writeln(
        "EXP. DATE:\n                                                2023/08");
    sbMsg.writeln("REF NO:\n                                     $refId");
    sbMsg.writeln("------------------------------------------------------");
    sbMsg.writeln("AMOUNT: RMB:2.55");
    sbMsg.writeln("------------------------------------------------------");
    sbMsg.writeln("CARDHOLDER SIGNATURE\n");
    debugPrint("PrinterServiceActivity: printMsg: ${sbMsg.toString()}");
    return sbMsg.toString();
  }

  String printText1() {
    DateTime currentDate = DateTime.now();
    String dateTime = DateFormat("yyyy-MM-dd HH:mm:ss").format(currentDate);
    String refId = DateFormat("yyyyMMddHHmmss").format(currentDate) +
        generateRandomNumber(4);

    StringBuffer sbMsg = StringBuffer();
    sbMsg.writeln("                          TEST");
    sbMsg.writeln("------------------------------------------------------");
    sbMsg.writeln("\t\t\t$dateTime");
    sbMsg.writeln("------------------------------------------------------");
    sbMsg.writeln("                          SALE\n");
    sbMsg.writeln("MERCHANT NAME:\t\t001420183990573");
    sbMsg.writeln("TERMINAL NO:\t\t00026715");
    sbMsg.writeln("OPERATOR NO:\t\t12345678");
    sbMsg.writeln("ISSUER:\t\t01020001");
    sbMsg.writeln("CARD NO:\t\t9558803602109503920");
    sbMsg.writeln("ACQUIRER:\t\t03050011");
    sbMsg.writeln("TXN. TYPE:\t\tSALE");
    sbMsg.writeln("EXP. DATE:\t\t2023/08");
    sbMsg.writeln("REF NO:\t\t$refId");
    sbMsg.writeln("------------------------------------------------------");
    sbMsg.writeln("AMOUNT: RMB:2.55");
    sbMsg.writeln("------------------------------------------------------");
    sbMsg.writeln("CARDHOLDER SIGNATURE\n");
    debugPrint("PrinterServiceActivity: printMsg: ${sbMsg.toString()}");
    return sbMsg.toString();
  }

  String generateRandomNumber(int length) {
    Random random = Random();
    return List.generate(length, (index) => random.nextInt(10).toString())
        .join();
  }

  Future<Uint8List> createTextBitmap(
      String text, int width, int height, bool isBold) async {
    final recorder = ui.PictureRecorder();
    final canvas = Canvas(
        recorder, Rect.fromLTWH(0, 0, width.toDouble(), height.toDouble()));

    // Set background color
    final paint = Paint()..color = Colors.white;
    canvas.drawRect(
        Rect.fromLTWH(0, 0, width.toDouble(), height.toDouble()), paint);

    // Configure text style
    final textStyle = TextStyle(
      color: Colors.black,
      fontSize: 24,
      fontWeight: isBold ? FontWeight.bold : FontWeight.normal,
    );

    // Create paragraph builder
    final paragraphStyle = ui.ParagraphStyle(textAlign: TextAlign.left);
    final paragraphBuilder = ui.ParagraphBuilder(paragraphStyle)
      ..pushStyle(ui.TextStyle(
          color: Colors.black,
          fontSize: 24,
          fontWeight: isBold ? ui.FontWeight.bold : ui.FontWeight.normal))
      ..addText(text);

    final paragraph = paragraphBuilder.build();
    paragraph.layout(ui.ParagraphConstraints(width: width.toDouble()));

    // Draw text
    canvas.drawParagraph(paragraph, Offset(10, height / 10));

    final picture = recorder.endRecording();
    final img = await picture.toImage(width, height);
    final byteData = await img.toByteData(format: ui.ImageByteFormat.png);
    return byteData!.buffer.asUint8List();
  }
}
