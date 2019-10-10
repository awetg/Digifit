package com.bwet.digifit.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.bwet.digifit.R
import com.bwet.digifit.model.ActivitySession
import com.bwet.digifit.model.AppDB
import com.bwet.digifit.model.MyLocation
import com.bwet.digifit.utils.*
import com.bwet.digifit.view.MainActivity
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

class ActivityTrackerService : BaseService(), LocationListener {

    private lateinit var appDB: AppDB
    private lateinit var locationManager: LocationManager
    private var gpsProviderEnable: Boolean = false
    private var locationList: ArrayList<Location> = arrayListOf()
    private var sessionOn = false
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        appDB = AppDB.getInstance(applicationContext)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsProviderEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        sharedPreferences = this.getSharedPreferences(SETTING_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        sharedPreferences.booleanLiveData(STOP_SERVICE_FLAG_KEY, false).observeForever { stop ->
            Log.d("DBG", "stop $stop")
            if (stop) {
                saveSession()
                stopSelf()
            } else {
                requestLocationUpdates()
            }
        }

        sharedPreferences.booleanLiveData(PUASE_SERVICE_FLAG_KEY, false).observeForever { pause ->
            Log.d("DBG", "pause $pause")
            if (pause) pauseSession()
        }
        super.onCreate()
    }

    override fun onHandleIntent(intent: Intent?) {}

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startForeground(ACTIVITY_SESSIONS_NOTIFICATION_ID, createNotification("Tracking activity"))

        requestLocationUpdates()

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) updateReceiver(true)

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
        Toast.makeText(this, "onupdate provider:${location?.provider} lat: ${location?.latitude}", Toast.LENGTH_SHORT).show()
        location?.let {locationList.add(it) }
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        requestLocationUpdates()
    }

    override fun onProviderEnabled(provider: String?) {
        if (provider == "gps") updateReceiver(true)
    }

    override fun onProviderDisabled(provider: String?) {
        if (provider == "gps") updateReceiver(false)
    }

    private fun requestLocationUpdates() {
        locationManager.removeUpdates(this)
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5.0f, this)

//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 5.0f, this)

        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun updateReceiver(value: Boolean) {
        val intent = Intent()
        intent.action = BROADCAST_ACTION_GPS_PROVIDER
        intent.putExtra(GPS_PROVIDER_ENABLED, value)
        sendBroadcast(intent)
    }

    private fun calculateDistance(): Double {
        var distance = 0.0

        if (locationList.isNotEmpty()) {
            locationList.reduce { current, next ->
                distance += current.distanceTo(next)
                next
            }
        }
        return distance
    }

    private fun saveSession() {
        val sessionState = SharedPreferenceUtil(this).getSessionState()
        launch {
            locationList.forEach {
            }
            val myLocationList = locationList.map { MyLocation(it.latitude, it.longitude, it.altitude, it.speed) }
            appDB.activitySessionDao()
                .insert(ActivitySession(sessionState.startTime, sessionState.startTime + sessionState.elapsedTime , myLocationList.toMutableList(), calculateDistance(), sessionState.activityType))
        }
    }

    private fun pauseSession() {
        sessionOn = false
        locationManager.removeUpdates(this)
    }
}