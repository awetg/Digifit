package com.bwet.digifit.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bwet.digifit.R
import com.bwet.digifit.utils.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.number_picker_dialog.view.*

class SettingsActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var sharedPreferenceUtil: SharedPreferenceUtil
    private var keyName = PREFERENCE_KEY_WEIGHT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(settings_toolbar)
        supportActionBar?.let {
            it.title = "Settings"
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        val themeSpenerAdapter = ArrayAdapter.createFromResource(this, R.array.theme_settings, android.R.layout.simple_spinner_item)
        themeSpenerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        theme_spinner.adapter = themeSpenerAdapter
        theme_spinner.onItemSelectedListener = this
        sharedPreferenceUtil = SharedPreferenceUtil(this)
        theme_spinner.setSelection(themeSpenerAdapter.getPosition(sharedPreferenceUtil.getSavedTheme()))

        setting_weight_txt_spinner.text = User.weight.toString()
        setting_height_txt_spinner.text = User.height.toString()

        setting_weight_txt_spinner.setOnClickListener {
            keyName = PREFERENCE_KEY_WEIGHT
           showPicker("Weight", "Choose your weight in kg", 40, 200, setting_weight_txt_spinner)
        }
        setting_height_txt_spinner.setOnClickListener {
            keyName = PREFERENCE_KEY_HEIGHT
            showPicker("Height", "Choose your height in cm", 100, 200, setting_height_txt_spinner)
        }

        setting_editAll_txt.setOnClickListener { startActivity(Intent(this, ProfileSetupActivity::class.java).apply {
            putExtra(EDIT_PROFILE_INTENT_KEY, true)
        })
        }

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
        sharedPreferenceUtil.saveTheme(selectedTheme)
    }

    private fun showPicker(title: String, message: String, numberMin: Int, numberMax: Int, textView: TextView) {
        val alterDialog = AlertDialog.Builder(this)
        val view = this.layoutInflater.inflate(R.layout.number_picker_dialog, null)
        alterDialog.setView(view)
        view.dialog_number_picker.minValue = numberMin
        view.dialog_number_picker.maxValue = numberMax
        view.dialog_number_picker.value = textView.text.toString().toInt()
        alterDialog
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                textView.text = view.dialog_number_picker.value.toString()
                sharedPreferenceUtil.saveInt(keyName, view.dialog_number_picker.value)
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .setCancelable(false)
            .show()
    }

    override fun onResume() {
        super.onResume()
        setting_weight_txt_spinner.text = User.weight.toString()
        setting_height_txt_spinner.text = User.height.toString()
    }
}
