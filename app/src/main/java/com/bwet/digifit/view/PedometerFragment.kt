package com.bwet.digifit.view


import android.animation.ObjectAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import com.bwet.digifit.R
import com.bwet.digifit.utils.DEBUG_TAG
import com.bwet.digifit.utils.PERMISSION_REQUEST_CODE_ACTIVITY_RECOGNITION
import com.bwet.digifit.model.Step
import com.bwet.digifit.utils.CalorieBurnedCalculator
import com.bwet.digifit.viewModel.StepViewModel
import kotlinx.android.synthetic.main.fragment_pedometer.*
import kotlinx.android.synthetic.main.fragment_pedometer.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import kotlin.math.sqrt

// this var is temporary, will be replaced with app setting later
// if true app will unregister accelerometer sensor to save battery
// only needed if STEP_DETECTOR sensor is not present on phone
internal var DISABLE_ACCELEROMETER_ON_BACKGROUND = true

class PedometerFragment : BaseFragment(), SensorEventListener {

    private lateinit var stepViewModel: StepViewModel

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
        arguments?.let {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_pedometer, container, false)

        checkPermission()

        stepViewModel = ViewModelProviders.of(this).get(StepViewModel::class.java)

        // get total saved steps count from database
        launch {
            steps = stepViewModel.getTotalSteps()
            view.step_count_txt.text = steps.toString()
            animateProgressBar(steps)
        }

        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager

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

        view.test_btn.setOnClickListener {
            val startTime = System.currentTimeMillis() - 7200 * 1000
            val endTime = System.currentTimeMillis()
            launch {
                stepViewModel.getStepCountByInterval(startTime, endTime, 3600, "%H")
                    .forEach { Log.d(DEBUG_TAG, "count ${it.count} intervalFormat ${it.intervalFormat}") }
            }
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            PedometerFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    // do nothing on accuracy changed
    override fun onAccuracyChanged(event: Sensor?, p1: Int) {}

    // step detect on
    override fun onSensorChanged(event: SensorEvent?) {

        val eventMillsDiff = (SystemClock.elapsedRealtimeNanos() - (event?.timestamp ?: SystemClock.elapsedRealtimeNanos())) / 1000000L

        currentEventTimeStamp = System.currentTimeMillis() + eventMillsDiff

        if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {

            // filter out fast step counts, decrease false positives
            if (currentEventTimeStamp - lastEventTimeStamp >= timeIntervalThreshold) {
                // if sensor is type step detector only 1.0 is returned, add to steps and show
                steps++
                step_count_txt.text = steps.toString()
                animateProgressBar(steps)
                // using GlobalScope instead this fragment scope so that coroutine is not interrupted prematurely
                // if the fragment is destroyed
                GlobalScope.launch(Dispatchers.IO) {stepViewModel.addStep(Step(currentEventTimeStamp))}

                calorie_txt.text = DecimalFormat("#.##").format(CalorieBurnedCalculator.calculateForSteps(steps)) + " cal"
            }


        } else if (event?.sensor?.type == Sensor.TYPE_LINEAR_ACCELERATION) {

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
        if (activity?.checkSelfPermission( android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION),
                PERMISSION_REQUEST_CODE_ACTIVITY_RECOGNITION
            )
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
                    AlertDialog.Builder(context!!)
                        .setMessage(R.string.activity_recognition)
                        .setPositiveButton("OK") { _, _ ->  checkPermission()}
                        .setNegativeButton("No") { _, _ -> }
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
