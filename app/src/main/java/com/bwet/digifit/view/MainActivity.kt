package com.bwet.digifit.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import com.bwet.digifit.R
import com.bwet.digifit.ui.main.SectionsPagerAdapter
import com.bwet.digifit.utils.PREFERENCE_KEY_THEME
import com.bwet.digifit.utils.SETTING_PREFERENCE_FILE_KEY
import com.bwet.digifit.utils.Theme
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Check selected theme and set it
        val sharedPref = this.getSharedPreferences(SETTING_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        val selectedThem =sharedPref.getString(PREFERENCE_KEY_THEME, "Light")
        Theme.setThem(selectedThem?: "Light")

        setContentView(R.layout.activity_main)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        setting_icon.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
    }

}
