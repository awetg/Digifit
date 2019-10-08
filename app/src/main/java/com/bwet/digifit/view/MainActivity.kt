package com.bwet.digifit.view

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bwet.digifit.R
import com.bwet.digifit.adapters.SectionsPagerAdapter
import com.bwet.digifit.services.HardwareStepDetectorService
import com.bwet.digifit.utils.*
import com.bwet.digifit.viewModel.StepViewModel
import kotlinx.android.synthetic.main.activity_main.*

// this var is temporary, will be replaced with app setting later
// if true app will unregister accelerometer sensor to save battery
// only needed if STEP_DETECTOR sensor is not present on phone
internal var DISABLE_ACCELEROMETER_ON_BACKGROUND = true

class MainActivity : AppCompatActivity(){

    private lateinit var stepViewModel: StepViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check selected theme and set it
        val settingsSharedPref = this.getSharedPreferences(SETTING_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        val selectedThem =settingsSharedPref.getString(PREFERENCE_KEY_THEME, getString(R.string.defaultTheme))
        Theme.setThem(selectedThem ?: getString(R.string.defaultTheme))

        setContentView(R.layout.activity_main)

        val sharedPrefUtil = SharedPreferenceUtil(this)

        if (sharedPrefUtil.userExist()) {

            sharedPrefUtil.refreshUserData()

            val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
            val viewPager: ViewPager = findViewById(R.id.view_pager)
            viewPager.adapter = sectionsPagerAdapter
            val tabs: TabLayout = findViewById(R.id.tabs)
            tabs.setupWithViewPager(viewPager)

            stepViewModel = ViewModelProviders.of(this).get(StepViewModel::class.java)
            stepViewModel.getTotalStepsLive().observe(this, Observer { step_count.text = it.toString() })

            val bitmap = BitmapFactory.decodeFile(FileUtil.getOrCreateProfileImageFile(this, "image/jpeg").path)
            main_user_image.setImageBitmap(bitmap ?: BitmapFactory.decodeResource(resources, R.drawable.ic_add_a_photo_themed_24dp))

            setting_icon.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }


            // create foreground service for step count notification channel for android oreo or higher
            createNotificationChannel(
                STEP_COUNT_FOREGROUND_NOTIFICATION_CHANNEL_ID,
                getString(R.string.foregroundNotificationChannelName),
                getString(R.string.foregroundNotificationChannelDescription),
                NotificationManager.IMPORTANCE_LOW
            )

            // create foreground service for activity tracker notification channel for android oreo or higher
            createNotificationChannel(
                ACTIVITY_FOREGROUND_NOTIFICATION_CHANNEL_ID,
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

    override fun onResume() {
        super.onResume()
        val bitmap = BitmapFactory.decodeFile(FileUtil.getOrCreateProfileImageFile(this, "image/jpeg").path)
        main_user_image.setImageBitmap(bitmap ?: BitmapFactory.decodeResource(resources, R.drawable.ic_add_a_photo_themed_24dp))
    }

}
