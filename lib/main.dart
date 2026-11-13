import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:async';

void main() {
  runApp(const PowerCheckApp());
}

class PowerCheckApp extends StatelessWidget {
  const PowerCheckApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'PowerCheck',
      theme: ThemeData(primarySwatch: Colors.blue),
      home: const PowerCheckHome(),
    );
  }
}

class PowerCheckHome extends StatefulWidget {
  const PowerCheckHome({super.key});

  @override
  State<PowerCheckHome> createState() => _PowerCheckHomeState();
}

class _PowerCheckHomeState extends State<PowerCheckHome> {
  static const platform = MethodChannel('com.powercheck.app/battery');

  String _voltage = "جاري القراءة...";
  String _current = "جاري القراءة...";
  String _temperature = "جاري القراءة...";
  String _wattage = "0.0 واط";

  Timer? _timer;

  @override
  void initState() {
    super.initState();
    _startPeriodicUpdates();
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  void _startPeriodicUpdates() {
    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      _fetchBatteryData();
    });
  }

  Future<void> _fetchBatteryData() async {
    try {
      final Map<dynamic, dynamic>? result = await platform.invokeMethod('getBatteryInfo');
      if (result != null) {
        double voltageV = (result['voltage'] ?? 0.0) / 1000.0;
        double currentA = (result['current'] ?? 0.0) / 1000000.0;
        double tempC = (result['temperature'] ?? 0.0) / 10.0;
        double wattage = voltageV * currentA.abs();

        setState(() {
          _voltage = "${voltageV.toStringAsFixed(2)} فولت";
          _current = "${(currentA * 1000).toStringAsFixed(0)} ملي أمبير";
          _temperature = "${tempC.toStringAsFixed(1)} °C";
          _wattage = "${wattage.toStringAsFixed(2)} واط";
        });
      }
    } catch (e) {
      setState(() {
        _voltage = "غير مدعوم";
        _current = "0 ملي أمبير";
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('PowerCheck - فاحص الطاقة'),
        backgroundColor: Colors.blueAccent,
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Card(
              color: Colors.amber,
              child: Padding(
                padding: EdgeInsets.all(12.0),
                child: Text(
                  'قم بتوصيل الشاحن أو الباور بانك لبدء قراءة القيم الحية.',
                  style: TextStyle(fontSize: 14, fontWeight: FontWeight.bold),
                ),
              ),
            ),
            const SizedBox(height: 20),
            _buildMetricCard("القدرة اللحظية (Wattage)", _wattage, Colors.green, Icons.bolt),
            _buildMetricCard("الجهد الفعلي (Voltage)", _voltage, Colors.blue, Icons.electric_bolt),
            _buildMetricCard("التيار الفعلي (Current)", _current, Colors.orange, Icons.speed),
            _buildMetricCard("حرارة البطارية", _temperature, Colors.red, Icons.thermostat),
          ],
        ),
      ),
    );
  }

  Widget _buildMetricCard(String title, String value, Color color, IconData icon) {
    return Card(
      elevation: 4,
      margin: const EdgeInsets.symmetric(vertical: 8),
      child: ListTile(
        leading: Icon(icon, size: 40, color: color),
        title: Text(title, style: const TextStyle(fontSize: 16, color: Colors.grey)),
        subtitle: Text(value, style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: color)),
      ),
    );
  }
}
