package com.powercheck.app

import android.content.BroadcastReceiver
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
                    val batteryStatus: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val receiver = object : BroadcastReceiver() {
                            override fun onReceive(context: Context?, intent: Intent?) {}
                        }
                        val intent = registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
                        unregisterReceiver(receiver)
                        intent
                    } else {
                        registerReceiver(null, filter)
                    }

                    val voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
                    val temperature = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
                    
                    val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                    val current = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                    } else {
                        0
                    }

                    val data = mapOf(
                        "voltage" to voltage,
                        "current" to current,
                        "temperature" to temperature
                    )
                    result.success(data)
                } catch (e: Exception) {
                    result.success(mapOf("voltage" to 3700, "current" to 0, "temperature" to 250))
                }
            } else {
                result.notImplemented()
            }
        }
    }
}
