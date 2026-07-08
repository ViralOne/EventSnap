package com.eventsnap.android.core.ui.mobile.dev

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import kotlin.math.sqrt

private const val SHAKE_THRESHOLD_G = 2.7f // ~2.7g spike, robust to walking
private const val MIN_INTERVAL_MS = 1_000L // debounce so one shake fires once

@Composable
fun ShakeListener(onShake: () -> Unit) {
    val context = LocalContext.current
    val latestOnShake by rememberUpdatedState(onShake)

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensorManager == null || accelerometer == null) {
            return@DisposableEffect onDispose { }
        }
        var lastShakeAt = 0L
        val listener =
            object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val gX = event.values[0] / SensorManager.GRAVITY_EARTH
                    val gY = event.values[1] / SensorManager.GRAVITY_EARTH
                    val gZ = event.values[2] / SensorManager.GRAVITY_EARTH
                    val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)
                    if (gForce >= SHAKE_THRESHOLD_G) {
                        val now = System.currentTimeMillis()
                        if (now - lastShakeAt >= MIN_INTERVAL_MS) {
                            lastShakeAt = now
                            latestOnShake()
                        }
                    }
                }

                override fun onAccuracyChanged(
                    sensor: Sensor?,
                    accuracy: Int,
                ) = Unit
            }
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(listener) }
    }
}
