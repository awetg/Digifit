package com.bwet.digifit.view

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.bwet.digifit.R
import com.bwet.digifit.adapters.SectionsPagerAdapter
import com.bwet.digifit.services.HardwareStepDetectorService
import com.bwet.digifit.utils.*
import kotlinx.android.synthetic.main.activity_main.*

// this var is temporary, will be replaced with app setting later
// if true app will unregister accelerometer sensor to save battery
// only needed if STEP_DETECTOR sensor is not present on phone
internal var DISABLE_ACCELEROMETER_ON_BACKGROUND = true

class MainActivity : AppCompatActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check selected theme and set it
        val settingsSharedPref = this.getSharedPreferences(SETTING_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        val selectedThem =settingsSharedPref.getString(PREFERENCE_KEY_THEME, "Follow System")
        Theme.setThem(selectedThem?: "Follow System")

        setContentView(R.layout.activity_main)

        // get user data
        val userSharedPref = this.getSharedPreferences(USER_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        val name = userSharedPref.getString(PREFERENCE_KEY_NAME, null)
        val weight = userSharedPref.getInt(PREFERENCE_KEY_WEIGHT, 0)
        val height = userSharedPref.getInt(PREFERENCE_KEY_HEIGHT, 0)
        if (name != null && weight != 0 && height != 0) {
            User.name = name
            User.weight = weight
            User.height = height

            val sectionsPagerAdapter =
                SectionsPagerAdapter(this, supportFragmentManager)
            val viewPager: ViewPager = findViewById(R.id.view_pager)
            viewPager.adapter = sectionsPagerAdapter
            val tabs: TabLayout = findViewById(R.id.tabs)
            tabs.setupWithViewPager(viewPager)

            setting_icon.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }


            // create foreground service notification channel for android oreo or higher
            createNotificationChannel(
                FOREGROUND_NOTIFICATION_CHANNEL_ID,
                getString(R.string.foregroundNotificationChannelName),
                getString(R.string.foregroundNotificationChannelDescription),
                NotificationManager.IMPORTANCE_LOW
            )

            ContextCompat.startForegroundService(applicationContext, Intent(this, HardwareStepDetectorService::class.java))

        } else {
            startActivity(Intent(this, ProfileSetupActivity::class.java))
        }

    }


    // Creates notification channel with given params
    private fun createNotificationChannel(channelId: String, channelName: String, channelDescription: String, priority: Int) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName,priority)
            channel.description = channelDescription
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}
