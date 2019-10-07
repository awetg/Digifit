package com.bwet.digifit.utils

import android.content.Context
import android.util.Log
import com.bwet.digifit.R

class SharedPreferenceUtil (private val context: Context){

    fun userExist(): Boolean {
        val sp = context.getSharedPreferences(USER_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        return sp?.getString(PREFERENCE_KEY_NAME, null) != null
    }

    fun refreshUserData() {
        val sp = context.getSharedPreferences(USER_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        val name = sp.getString(PREFERENCE_KEY_NAME, null)
        val weight = sp.getInt(PREFERENCE_KEY_WEIGHT, 0)
        val height = sp.getInt(PREFERENCE_KEY_HEIGHT, 0)
        val dailyGoal = sp.getInt(PREFERENCE_KEY_DAILY_GOAL, 0)
        User.name = name!!
        User.weight = weight
        User.height = height
        User.dailyStepGoal = dailyGoal
    }

    fun saveProfile(name: String, weight: Int, height: Int, dailyGoal: Int) {
        context.getSharedPreferences(USER_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
            .edit()
            .putString(PREFERENCE_KEY_NAME, name)
            .putInt(PREFERENCE_KEY_WEIGHT, weight)
            .putInt(PREFERENCE_KEY_HEIGHT, height)
            .putInt(PREFERENCE_KEY_DAILY_GOAL, dailyGoal)
            .apply()

        User.name = name
        User.weight = weight
        User.height = height
        User.dailyStepGoal = dailyGoal
    }

    fun getSavedTheme(): String {
        return context.getSharedPreferences(SETTING_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
            .getString(PREFERENCE_KEY_THEME, context.getString(R.string.defaultTheme))!!
    }

    fun saveTheme(selectedTheme: String) {
        context.getSharedPreferences(SETTING_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
            .edit()
            .putString(PREFERENCE_KEY_THEME, selectedTheme)
            .apply()
    }

    fun saveInt(keyName: String, value: Int) {
        context.getSharedPreferences(USER_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
            .edit()
            .putInt(keyName, value)
            .apply()
        refreshUserData()
    }


}