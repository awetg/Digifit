package com.bwet.digifit.services

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.os.SystemClock
import com.bwet.digifit.model.Step
import kotlin.math.sqrt

class AccelerometerStepDetectorService : StepDetectorService() {

    // all below variables are linear acceleration step count variables
    private var accThreshold = 1.5F
    private var thresholdArray = FloatArray(4)
    private var thresholdCount = 0
    private var lastPeakTimeStamp = 0L

    private var lastAccVectorSum = 0F
    private var peakValue = 0F
    private var valleyValue = 0F
    private var accIncreasing = false
    private var lastAccIncreasing = false
    private var accIncreaseCount = 0
    private var lastAccIncreaseCount = 0

    override fun getSensorType(): Int = Sensor.TYPE_LINEAR_ACCELERATION

    override fun onSensorChanged(event: SensorEvent?) {

        val eventMillsDiff = (SystemClock.elapsedRealtimeNanos() - (event?.timestamp ?: SystemClock.elapsedRealtimeNanos())) / 1000000L
        val currentEventTimeStamp = System.currentTimeMillis() + eventMillsDiff

        if (event?.sensor?.type == Sensor.TYPE_LINEAR_ACCELERATION) {

            val (X, Y, Z) = event.values
            val accVectorSum = sqrt(X*X + Y*Y + Z*Z)

            if (lastAccVectorSum == 0F) {
                lastAccVectorSum = accVectorSum

            } else {

                if (peakDetected(accVectorSum, lastAccVectorSum)) {

                    // if peak detected within time intervalFormat threshold and
                    // the difference between peak and valley is greater than acceleration threshold, detect step
                    if (currentEventTimeStamp - lastPeakTimeStamp >= timeIntervalThreshold) {

                        if(peakValue - valleyValue >= accThreshold) {
                            // step detected increase step count
                            onStepDetected(Step(currentEventTimeStamp))
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