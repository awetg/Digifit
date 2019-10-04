package com.bwet.digifit.view

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bwet.digifit.R
import com.bwet.digifit.utils.*
import kotlinx.android.synthetic.main.activity_profile_setup.*
import kotlinx.android.synthetic.main.number_picker_dialog.view.*

class ProfileSetupActivity : AppCompatActivity() {

    private var weight: Int = R.string.weightDefaultSelection
    private var height: Int = R.string.heightDefaultSelection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        weight_txt.setOnClickListener {
            weight = showPicker("Weight", "Choose your weight in kg", 30, 200, weight, weight_txt)
        }
        height_txt.setOnClickListener {
            height = showPicker("Height", "Choose your height in cm", 70, 220, height, height_txt)
        }

        profile_next_btn.setOnClickListener {
            if (name_edittxt.text.toString().isNotEmpty()) {

                this.getSharedPreferences(USER_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                    .edit()
                    .putString(PREFERENCE_KEY_NAME, name_edittxt.text.toString())
                    .putInt(PREFERENCE_KEY_WEIGHT, weight)
                    .putInt(PREFERENCE_KEY_HEIGHT, height)
                    .apply()
                startActivity(Intent(this, BoardingActivity::class.java))

            } else {
                AlertDialog.Builder(this)
                    .setMessage("You must enter a name to continue.")
                    .setPositiveButton("OK") { _, _ -> }
                    .setCancelable(false)
                    .show()
            }
        }
    }

    private fun showPicker(title: String, message: String, numberMin: Int, numberMax: Int, default: Int, textView: TextView): Int {
        var selectedValue = default
        val alterDialog = AlertDialog.Builder(this)
        val view = this.layoutInflater.inflate(R.layout.number_picker_dialog, null)
        alterDialog.setView(view)
        view.dialog_number_picker.minValue = numberMin
        view.dialog_number_picker.maxValue = numberMax
        view.dialog_number_picker.value = default
        alterDialog
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                selectedValue = view.dialog_number_picker.value
                textView.text = selectedValue.toString()
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .setCancelable(false)
            .show()
        return selectedValue
    }
}
