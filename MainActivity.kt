package com.powercheck.app

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.powercheck.app/battery"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
            call, result ->
            if (call.method == "getBatteryInfo") {
                try {
                    val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                    val intent = context.registerReceiver(null, filter)

                    val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
                    val temperature = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
                    val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                    val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1

                    val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                    val currentNow = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                    } else {
                        0
                    }

                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || plugged > 0

                    val data = mapOf(
                        "voltage" to voltage,
                        "current" to currentNow,
                        "temperature" to temperature,
                        "isCharging" to if (isCharging) 1 else 0
                    )
                    result.success(data)
                } catch (e: Exception) {
                    result.success(mapOf("voltage" to 3700, "current" to 0, "temperature" to 250, "isCharging" to 0))
                }
            } else {
                result.notImplemented()
            }
        }
    }
}
