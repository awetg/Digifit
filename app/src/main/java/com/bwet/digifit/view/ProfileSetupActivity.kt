package com.bwet.digifit.view

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.bwet.digifit.R
import com.bwet.digifit.utils.*
import kotlinx.android.synthetic.main.activity_profile_setup.*
import kotlinx.android.synthetic.main.number_picker_dialog.view.*

class ProfileSetupActivity : AppCompatActivity() {

    private val imagePickOptions = arrayOf("Take Photo", "Choose From Gallery", "Cancel")

    private val REQUEST_IMAGE_CAPTURE = 99
    private val REQUEST_IMAGE_PICK = 88

    private var EDITING_PROFILE = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        EDITING_PROFILE = intent.getBooleanExtra(EDIT_PROFILE_INTENT_KEY, false)

        val bitmap = BitmapFactory.decodeFile(FileUtil.getOrCreateProfileImageFile(this, "image/jpeg").path)
        profile_user_image.setImageBitmap(bitmap ?: BitmapFactory.decodeResource(resources, R.drawable.ic_add_a_photo_themed_24dp))

        profile_name_edittxt.setText(User.name)
        profile_weight_txt_spinner.text = if (User.weight == 0) getString(R.string.weightDefaultSelection )else User.weight.toString()
        profile_height_txt_spinner.text = if (User.height == 0) getString(R.string.heightDefaultSelection ) else User.height.toString()
        profile_daily_goal_edittxt.setText(if (User.dailyStepGoal == 0) getString(R.string.dailyGoalDefaultValue) else User.dailyStepGoal.toString() )


        profile_user_image.setOnClickListener { showImagePicker() }

        profile_weight_txt_spinner.setOnClickListener {
            showPicker("Weight", "Choose your tempWeight in kg", 40, 200, profile_weight_txt_spinner)
        }
        profile_height_txt_spinner.setOnClickListener {
            showPicker("Height", "Choose your tempHeight in cm", 100, 200, profile_height_txt_spinner)
        }

        profile_save_btn.setOnClickListener {
            if (profile_name_edittxt.text.toString().isNotEmpty() && profile_daily_goal_edittxt.text.toString().isNotEmpty()) {
                SharedPreferenceUtil(this).saveProfile(
                    profile_name_edittxt.text.toString(),
                    profile_weight_txt_spinner.text.toString().toInt(),
                    profile_height_txt_spinner.text.toString().toInt(),
                    profile_daily_goal_edittxt.text.toString().toInt()
                )
                if (EDITING_PROFILE) finish() else startActivity(Intent(this, BoardingActivity::class.java))

            } else {
                val (first, second) = if ( profile_name_edittxt.text.toString().isEmpty()) Pair("name", "") else Pair("daily goal", "Proposed daily goal is 10,000 steps a day.")
                val message = String.format(getString(R.string.emptyInputMessage), first, second)
                AlertDialog.Builder(this)
                    .setMessage(message)
                    .setPositiveButton("OK") { _, _ -> }
                    .setCancelable(false)
                    .show()
            }
        }
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
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .setCancelable(false)
            .show()
    }

    private fun showImagePicker() {
        AlertDialog.Builder(this)
            .setTitle("Add Photo")
            .setItems(imagePickOptions) {_, item ->
                when(imagePickOptions[item]) {
                    "Take Photo" -> {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        intent.resolveActivity(packageManager)?.let {
                            val file = FileUtil.getOrCreateProfileImageFile(this, "image/jpeg")
                            val fileUri = FileProvider.getUriForFile(this, "com.bwet.digifit.fileprovider", file)
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
                            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                        }
                    }
                    "Choose From Gallery" -> {
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.type = "image/*"
                        startActivityForResult(intent, REQUEST_IMAGE_PICK)
                    }
                }
            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                REQUEST_IMAGE_PICK -> {
                    data?.data?.let {
                        val file = FileUtil.getOrCreateProfileImageFile(this, contentResolver.getType(it) ?: "image/jpeg")
                        FileUtil.copyStreamToFile(contentResolver.openInputStream(it)!!, file)
                        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(it))
                        profile_user_image.setImageBitmap(bitmap)
                    }
                }
                REQUEST_IMAGE_CAPTURE -> {profile_user_image.setImageBitmap(BitmapFactory.decodeFile(FileUtil.getOrCreateProfileImageFile(this, "image/jpeg").path))}
            }
        }
    }
}
