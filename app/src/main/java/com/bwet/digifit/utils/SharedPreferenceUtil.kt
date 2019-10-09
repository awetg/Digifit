package com.bwet.digifit.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
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

    fun saveInt(prefName: String, keyName: String, value: Int) {
        context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
            .edit()
            .putInt(keyName, value)
            .apply()
        refreshUserData()
    }

    fun saveBoolean(prefName: String, keyName: String, value: Boolean) {
        context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(keyName, value)
            .apply()
        refreshUserData()
    }


    fun saveChronometerSate(chronometerState: ChronometerState) {
        context.getSharedPreferences(CHRONOMETER_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
            .edit()
            .putLong(PREFERENCE_KEY_START_TIME, chronometerState.startTime)
            .putLong(PREFERENCE_KEY_PAUSE_OFFSET, chronometerState.pauseOffset)
            .putLong(PREFERENCE_KEY_ELAPSED_TIME, chronometerState.elapsedTime)
            .apply()
    }

    fun getChronometerStae(): ChronometerState {
        val sp = context.getSharedPreferences(CHRONOMETER_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        val startTime = sp.getLong(PREFERENCE_KEY_START_TIME, 0L)
        val pauseOffset = sp.getLong(PREFERENCE_KEY_PAUSE_OFFSET, 0L)
        val elapsedTime = sp.getLong(PREFERENCE_KEY_ELAPSED_TIME, 0L)
        return ChronometerState(startTime, pauseOffset, elapsedTime)
    }

    fun saveSessionSate(sessionState: SessionState) {
        context.getSharedPreferences(SETTING_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
            .edit()
            .putLong(SERVICE_PERFERENCE_KEY_START_TIME, sessionState.startTime)
            .putLong(SERVICE_PERFERENCE_KEY_ELAPSED_TIME, sessionState.elapsedTime)
            .putString(SERVICE_PERFERENCE_KEY_SELECTED_ACTIVITY, sessionState.activityType)
            .apply()
    }

    fun getSessionState(): SessionState {
        val sp = context.getSharedPreferences(SETTING_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        val startTime = sp.getLong(SERVICE_PERFERENCE_KEY_START_TIME, 0)
        val elapsedTime = sp.getLong(SERVICE_PERFERENCE_KEY_ELAPSED_TIME, 0)
        val activityType = sp.getString(SERVICE_PERFERENCE_KEY_SELECTED_ACTIVITY, "Running")
        return SessionState(startTime, elapsedTime, activityType?: "Running")
    }
}





abstract class SharedPreferenceLiveData<T>(val sharedPrefs: SharedPreferences,
                                           val key: String,
                                           val defValue: T) : LiveData<T>() {

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key == this.key) {
            value = getValueFromPreferences(key, defValue)
        }
    }

    abstract fun getValueFromPreferences(key: String, defValue: T): T

    override fun onActive() {
        super.onActive()
        value = getValueFromPreferences(key, defValue)
        sharedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onInactive() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        super.onInactive()
    }

    class SharedPreferenceBooleanLiveData(sharedPrefs: SharedPreferences, key: String, defValue: Boolean) :
        SharedPreferenceLiveData<Boolean>(sharedPrefs, key, defValue) {
        override fun getValueFromPreferences(key: String, defValue: Boolean): Boolean = sharedPrefs.getBoolean(key, defValue)
    }

}

fun SharedPreferences.booleanLiveData(key: String, defValue: Boolean): SharedPreferenceLiveData<Boolean> {
    return SharedPreferenceLiveData.SharedPreferenceBooleanLiveData(this, key, defValue)
}

