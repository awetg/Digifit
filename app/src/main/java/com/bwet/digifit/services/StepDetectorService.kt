package com.bwet.digifit.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bwet.digifit.R
import com.bwet.digifit.model.AppDB
import com.bwet.digifit.model.Step
import com.bwet.digifit.utils.FOREGROUND_NOTIFICATION_CHANNEL_ID
import com.bwet.digifit.utils.STEP_COUNTER_NOTIFICATION_ID
import com.bwet.digifit.utils.User
import com.bwet.digifit.view.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class StepDetectorService : SensorEventListener, BaseService() {

    private lateinit var appDB: AppDB
    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private var steps = 0

    // last sensor event timestamp
    var lastEventTimeStamp = 0L

    // interval threshold for time between 2 sensor events
    val timeIntervalThreshold = 200

    // get sensor type of current sensor(sub class sensors) must be implemented by sub classes
    abstract fun getSensorType(): Int

    override fun onCreate() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        appDB = AppDB.getInstance(applicationContext)
        launch { steps = appDB.stepDao().getTotalSteps() }
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // register sensor listner for sensor type
        sensor = sensorManager.getDefaultSensor(getSensorType())
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)

        // start foreground service with ongoing notification
        val message = String.format(getString(R.string.notification_text_steps), steps, User.dailyStepGoal)
        startForeground(STEP_COUNTER_NOTIFICATION_ID, createNotification(message))

        return Service.START_STICKY
    }

    override fun onHandleIntent(p0: Intent?) {}

    private fun createNotification(message: String): Notification {

        // pending intent to ope MainActivity when notification is clicked
        val pendingIntent = Intent(this, MainActivity::class.java).let {
            PendingIntent.getActivity(this, 0 , it, 0)
        }

        return NotificationCompat.Builder(this, FOREGROUND_NOTIFICATION_CHANNEL_ID)
            .setContentText(message)
            .setProgress(User.dailyStepGoal, steps, false)
            .setSmallIcon(R.drawable.ic_directions_walk_black_24dp)
            .setContentIntent(pendingIntent)
            .setTicker(message)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET) // Devices with API < 26 will respect visibility and priority
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    private fun updateNotification() {
        val message = String.format(getString(R.string.notification_text_steps), steps, User.dailyStepGoal)
        NotificationManagerCompat.from(this ).notify(STEP_COUNTER_NOTIFICATION_ID, createNotification(message))
    }


    // on accuracy overridden here, sub classes don't need to implement if not necessary
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    // sub class step detectors will call this method when a step is detected
    protected fun onStepDetected(step: Step) {
        steps++
        lastEventTimeStamp = step.timeStampMills
        GlobalScope.launch(Dispatchers.IO) {appDB.stepDao().insert(step)}
        updateNotification()
    }
}