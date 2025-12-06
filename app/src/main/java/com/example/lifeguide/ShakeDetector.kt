package com.example.lifeguide

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(private val context: Context, private val onShake: () -> Unit) : SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    private val SHAKE_THRESHOLD_GRAVITY = 2.7f
    private val SHAKE_SLOP_TIME_MS = 500
    private val SHAKE_COUNT_RESET_TIME_MS = 3000

    private var mShakeTimestamp: Long = 0
    private var mShakeCount: Int = 0

    init {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    fun start() {
        if (accelerometer != null) {
            sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (accelerometer == null) return

        val x = event?.values?.get(0) ?: 0f
        val y = event?.values?.get(1) ?: 0f
        val z = event?.values?.get(2) ?: 0f

        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        // gForce will be close to 1 when there is no movement.
        val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            val now = System.currentTimeMillis()
            // ignore shakes too close to each other (500ms)
            if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                return
            }

            // reset the shake count after 3 seconds of no shakes
            if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                mShakeCount = 0
            }

            mShakeTimestamp = now
            mShakeCount++

            if (mShakeCount >= 3) { // Detect 3 shakes
                onShake()
                mShakeCount = 0 // Reset count after detection
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }
}
