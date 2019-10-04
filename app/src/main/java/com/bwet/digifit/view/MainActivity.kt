package com.bwet.digifit.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import com.bwet.digifit.R
import com.bwet.digifit.ui.main.SectionsPagerAdapter
import com.bwet.digifit.utils.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check selected theme and set it
        val settingsSharedPref = this.getSharedPreferences(SETTING_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        val selectedThem =settingsSharedPref.getString(PREFERENCE_KEY_THEME, "Follow System")
        Theme.setThem(selectedThem?: "Follow System")

        setContentView(R.layout.activity_main)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        setting_icon.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }

        // get user data
        val userSharedPref = this.getSharedPreferences(USER_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        val name = userSharedPref.getString(PREFERENCE_KEY_NAME, null)
        val weight = userSharedPref.getInt(PREFERENCE_KEY_WEIGHT, 0)
        val height = userSharedPref.getInt(PREFERENCE_KEY_HEIGHT, 0)
        if (name != null && weight != 0 && height != 0) {
            User.name = name
            User.weight = weight
            User.height = height
        } else {
            startActivity(Intent(this, ProfileSetupActivity::class.java))
        }
    }

}
