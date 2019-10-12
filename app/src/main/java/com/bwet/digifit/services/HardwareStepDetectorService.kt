package com.bwet.digifit.services

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.os.SystemClock
import com.bwet.digifit.model.Step

class HardwareStepDetectorService : StepDetectorService() {

    override fun getSensorType(): Int = Sensor.TYPE_STEP_DETECTOR

    override fun onSensorChanged(event: SensorEvent?) {

        val eventMillsDiff = (SystemClock.elapsedRealtimeNanos() - (event?.timestamp ?: SystemClock.elapsedRealtimeNanos())) / 1000000L
        val currentEventTimeStamp = System.currentTimeMillis() + eventMillsDiff

        if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
            if (currentEventTimeStamp - lastEventTimeStamp >= timeIntervalThreshold)
                onStepDetected(Step(currentEventTimeStamp))
        }
    }

}