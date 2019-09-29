package com.bwet.digifit

import android.animation.ObjectAnimator
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AlertDialog
import com.bwet.digifit.Utils.PERMISSION_REQUEST_CODE_ACTIVITY_RECOGNITION
import kotlinx.android.synthetic.main.activity_pedometer.*
import kotlin.math.sqrt

// this var is temporary, will be replaced with app setting later
// if true app will unregister accelerometer sensor to save battery
// only needed if STEP_DETECTOR sensor is not present on phone
internal var DISABLE_ACCELEROMETER_ON_BACKGROUND = true

class PedometerActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private var steps = 0
    private var currentEventTimeStamp = 0L
    private var lastEventTimeStamp = 0L

    // all below variables are linear acceleration step count variables
    private var accThreshold = 1.5F
    private var thresholdArray = FloatArray(4)
    private var thresholdCount = 0
    private val timeIntervalThreshold = 200
    private var lastPeakTimeStamp = 0L

    private var lastAccVectorSum = 0F
    private var peakValue = 0F
    private var valleyValue = 0F
    private var accIncreasing = false
    private var lastAccIncreasing = false
    private var accIncreaseCount = 0
    private var lastAccIncreaseCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedometer)
        step_count_txt.text = steps.toString()

        checkPermission()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // use STEP_DETECTOR if present or use LINEAR_ACCELERATION as fallback
        // NOTE: using accelerometer for long time drains battery fast
        // using STEP_DETECTOR for long time have no noticeable battery effect
        sensor = if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        } else {
            sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        }

        // SENSOR_DELAY_FASTEST fastest sampling 0ms according to android Docs
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)
    }

    // do nothing on accuracy changed
    override fun onAccuracyChanged(event: Sensor?, p1: Int) {}

    // step detect on
    override fun onSensorChanged(event: SensorEvent?) {

        currentEventTimeStamp = System.currentTimeMillis() + (event?.timestamp ?: System.nanoTime() - SystemClock.elapsedRealtimeNanos()) / 1000000L

        if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {

            // filter out fast step counts, decrease false positives
            if (currentEventTimeStamp - lastEventTimeStamp >= timeIntervalThreshold) {
                // if sensor is type step detector only 1.0 is returned, add to steps and show
                steps++
                step_count_txt.text = steps.toString()
                animateProgressBar(steps)
            }


        } else if (event?.sensor?.type == Sensor.TYPE_LINEAR_ACCELERATION) {

            val (X, Y, Z) = event.values
            val accVectorSum = sqrt(X*X + Y*Y + Z*Z)

            if (lastAccVectorSum == 0F) {
                lastAccVectorSum = accVectorSum

            } else {

                if (peakDetected(accVectorSum, lastAccVectorSum)) {

                    // if peak detected within time interval threshold and
                    // the difference between peak and valley is greater than acceleration threshold, detect step
                    if (currentEventTimeStamp - lastPeakTimeStamp >= timeIntervalThreshold) {

                        if(peakValue - valleyValue >= accThreshold) {
                            // step detected increase step count
                            steps++
                            step_count_txt.text = steps.toString()
                            animateProgressBar(steps)
                        }
                        if (peakValue - valleyValue >= 1.0F) {
                            // tune threshold by averaging and updating last 4 threshold values
                            accThreshold = tuneThreshold(peakValue - valleyValue)
                        }
                    }
                    lastPeakTimeStamp = currentEventTimeStamp
                }
            }

            lastAccVectorSum = accVectorSum

        }

        lastEventTimeStamp = currentEventTimeStamp

    }

    override fun onResume() {
        super.onResume()
        // LINEAR_ACCELERATION is used as fallback if STEP_DETECTOR is not preset
        // using accelerometer sensor drains battery, user can have the setting to disable count on background
        if (DISABLE_ACCELEROMETER_ON_BACKGROUND) {
            sensorManager.registerListener(this, sensor,SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    override fun onPause() {
        super.onPause()
        // LINEAR_ACCELERATION is used as fallback if STEP_DETECTOR is not preset
        // using accelerometer sensor drains battery, user can have the setting to disable count on background
        if (DISABLE_ACCELEROMETER_ON_BACKGROUND) {
            sensorManager.unregisterListener(this, sensor)
        }
    }

    private fun checkPermission() {
        if (checkSelfPermission( android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION),
                PERMISSION_REQUEST_CODE_ACTIVITY_RECOGNITION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE_ACTIVITY_RECOGNITION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //permission granted
                } else {
                    AlertDialog.Builder(this)
                        .setMessage(R.string.activity_recognition)
                        .setPositiveButton("OK") { _, _ ->  checkPermission()}
                        .setNegativeButton("No") { _, _ -> finish()}
                        .setCancelable(false)
                        .show()
                }
            }
        }
    }

    // Animate progressbar from last progress value up to the newValue passed to function
    private fun animateProgressBar(newValue: Int) {
        val animator = ObjectAnimator.ofInt(step_progressBar, "progress", step_progressBar.progress , newValue)
        animator.duration = 5000
        animator.interpolator = DecelerateInterpolator()
        animator.start()
        step_progressBar.clearAnimation()
    }

    // Detect if there is a peak in linear acceleration values
    // returns Boolean
    private fun peakDetected(currentAccSum: Float, lastAccSum: Float): Boolean {

        lastAccIncreasing = accIncreasing

        // if new acceleration sum is greater than last acc is increasing
        if (currentAccSum > lastAccSum) {
            accIncreasing = true
            accIncreaseCount++

        } else {
            // if new acc is less than last acc, then acc is decreasing
            lastAccIncreaseCount = accIncreaseCount
            accIncreaseCount = 0
            accIncreasing = false
        }

        // if current acc is decreasing and last acc was increasing then there is peak
        return if (!accIncreasing && lastAccIncreasing && (lastAccIncreaseCount >= 2 || lastAccSum >= 3)) {
            peakValue = lastAccSum
            true

        } else if (!accIncreasing && !lastAccIncreasing) {
            valleyValue = lastAccSum
            false

        } else {
            false
        }
    }

    // tune acceleration threshold values
    // returns Float (new threshold value)
    private fun tuneThreshold(value: Float): Float {
        var threshold = accThreshold
        if (thresholdCount < thresholdArray.size) {
            thresholdArray[thresholdCount] = value
            thresholdCount++
        } else {
            val avg = thresholdArray.average()
            // tune this value and initial threshold values for more accurate step counting
            threshold = when {
                avg >= 7 -> 3F
                avg >= 5 -> 2.4F
                avg >= 3 -> 1.8F
                avg >= 1 -> 1.5F
                else -> 1F
            }
            // thresholdArray size is 4
            // remove first element and copy the rest of thresholdArray element
            thresholdArray = thresholdArray.sliceArray(1..3)
        }
        return threshold
    }
}
