package com.bwet.digifit.utils

import com.google.gson.Gson

const val DEBUG_TAG = "DBG"

const val PERMISSION_REQUEST_CODE_ACTIVITY_RECOGNITION = 1111

// Shared preference constants
const val SETTING_PREFERENCE_FILE_KEY = "SettingPreference"
const val PREFERENCE_KEY_THEME = "Theme"
const val USER_PREFERENCE_FILE_KEY = "UserPreference"
const val PREFERENCE_KEY_NAME = "Name"
const val PREFERENCE_KEY_WEIGHT = "Weight"
const val PREFERENCE_KEY_HEIGHT = "Height"
const val PREFERENCE_KEY_DAILY_GOAL = "dailyGoal"

// notification constants
const val FOREGROUND_NOTIFICATION_CHANNEL_ID = "PersistentNotificationID"
const val STEP_COUNTER_NOTIFICATION_ID = 1010

// Intent constants
const val EDIT_PROFILE_INTENT_KEY = "Edit"
const val ACTIVITY_TRACKER_DETAIL_KEY = "TrackerDetail"

// object constancts
val gson = Gson()

