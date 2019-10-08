package com.bwet.digifit.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.bwet.digifit.R
import com.bwet.digifit.model.ActivitySession
import com.bwet.digifit.model.AppDB
import com.bwet.digifit.utils.*
import com.bwet.digifit.view.MainActivity
import kotlinx.coroutines.launch

class ActivityTrackerService : BaseService(), LocationListener {

    private lateinit var appDB: AppDB
    private lateinit var locationManager: LocationManager
    private var gpsProviderEnable: Boolean = false
    private var locationList: ArrayList<Location> = arrayListOf()
    private var distance = 0.0
    private var sessionOn = false

    override fun onCreate() {
        appDB = AppDB.getInstance(applicationContext)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsProviderEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        super.onCreate()
    }

    override fun onHandleIntent(intent: Intent?) {}

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(ACTIVITY_SESSIONS_NOTIFICATION_ID, createNotification("Tracking activity"))

        // if an intent was received it means that session should be saved to database and service must stop
        if (intent != null) {
            val pauseSession = intent.getBooleanExtra(ACTIVITY_SERVICE_INTENT_PAUSE_SESSION, false)

            if (pauseSession) {
                sessionOn = false
                locationManager.removeUpdates(this)
                Log.d("DBG", "pause")

            } else {
                val startTime = intent.getLongExtra(ACTIVITY_SERVICE_INTENT_START_TIME, -1L)
                val elapsedTime = intent.getLongExtra(ACTIVITY_SERVICE_INTENT_ELAPSED_TIME, -1L)
                val activityType = intent.getStringExtra(ACTIVITY_SERVICE_INTENT_SELECTED_ACTIVITY)
                if (startTime > 0 && elapsedTime >= 0 && activityType != null) {
                    Log.d("DBG", "start: $startTime, pause:$elapsedTime")
                    launch {
                        appDB.activitySessionDao()
                            .insert(ActivitySession(startTime, startTime + elapsedTime, locationList, distance, activityType))
                    }
                    stopSelf()
                }
            }
        }

        requestLocationUpdates()

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) updateRecevier(true)

        return Service.START_STICKY
    }

    private fun createNotification(message: String): Notification {

        // pending intent to ope MainActivity when notification is clicked
        val pendingIntent = Intent(this, MainActivity::class.java).let {
            PendingIntent.getActivity(this, 0 , it, 0)
        }

        return NotificationCompat.Builder(this, ACTIVITY_FOREGROUND_NOTIFICATION_CHANNEL_ID)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_directions_walk_black_24dp)
            .setContentIntent(pendingIntent)
            .setTicker(message)
            .setColorized(true)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .setVisibility(NotificationCompat.VISIBILITY_SECRET) // Devices with API < 26 will respect visibility and priority
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    override fun onLocationChanged(location: Location?) {
        location?.let {
            locationList.add(it)
            distance += locationList.last().distanceTo(location)
        }
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        requestLocationUpdates()
    }

    override fun onProviderEnabled(provider: String?) {
        if (provider == "gps") updateRecevier(true)
    }

    override fun onProviderDisabled(provider: String?) {
        if (provider == "gps") updateRecevier(false)
    }

    private fun requestLocationUpdates() {
        Log.d("DBG", "requesting updates")
        locationManager.removeUpdates(this)
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 5.0f, this)

//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 5.0f, this)

        } catch (e: SecurityException) {
            e.printStackTrace()
            Log.d(DEBUG_TAG, "security exception ${e.message}")
        }
    }

    fun updateRecevier(value: Boolean) {
        Log.d("DBG", "provider changed")
        val intent = Intent()
        intent.action = BROADCAST_ACTION_GPS_PROVIDER
        intent.putExtra(GPS_PROVIDER_ENABLED, value)
        sendBroadcast(intent)
    }
}