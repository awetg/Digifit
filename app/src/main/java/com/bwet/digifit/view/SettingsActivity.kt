package com.bwet.digifit.view

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.bwet.digifit.R
import com.bwet.digifit.utils.PREFERENCE_KEY_THEME
import com.bwet.digifit.utils.SETTING_PREFERENCE_FILE_KEY
import com.bwet.digifit.utils.Theme
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(settings_toolbar)
        supportActionBar?.let {
            it.title = "Settings"
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        val spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.theme_settings, android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        theme_spinner.adapter = spinnerAdapter
        theme_spinner.onItemSelectedListener = this
        val sharedPref = this.getSharedPreferences(SETTING_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        val selectedThem =sharedPref.getString(PREFERENCE_KEY_THEME, "Light")
        theme_spinner.setSelection(spinnerAdapter.getPosition(selectedThem))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, p3: Long) {
        val selectedTheme: String = parent?.getItemAtPosition(position).toString()
        Theme.setThem(selectedTheme)
        this.getSharedPreferences(SETTING_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
            .edit()
            .putString(PREFERENCE_KEY_THEME, selectedTheme)
            .apply()
    }
}
