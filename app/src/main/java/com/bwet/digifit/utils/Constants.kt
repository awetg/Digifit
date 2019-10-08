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

const val CHRONOMETER_PREFERENCE_FILE_KEY = "chronometer"
const val PREFERENCE_KEY_START_TIME = "startTime"
const val PREFERENCE_KEY_PAUSE_OFFSET = "pauseOffset"
const val PREFERENCE_KEY_ELAPSED_TIME = "preferenceElapsedTime"
const val ACTIVITY_SERVICE_INTENT_START_TIME = "intentStartTime"
const val ACTIVITY_SERVICE_INTENT_ELAPSED_TIME = "intentPauseOffset"
const val ACTIVITY_SERVICE_INTENT_SELECTED_ACTIVITY = "typeOfActivity"
const val ACTIVITY_SERVICE_INTENT_PAUSE_SESSION = "pauseSession"

// notification constants
const val STEP_COUNT_FOREGROUND_NOTIFICATION_CHANNEL_ID = "stepCountChannelId"
const val STEP_COUNTER_NOTIFICATION_ID = 1010

const val ACTIVITY_FOREGROUND_NOTIFICATION_CHANNEL_ID = "activitySessionChannelId"
const val ACTIVITY_SESSIONS_NOTIFICATION_ID = 2020

// Intent constants
const val EDIT_PROFILE_INTENT_KEY = "Edit"
const val ACTIVITY_TRACKER_DETAIL_KEY = "TrackerDetail"

// object constancts
val gson = Gson()


const val BROADCAST_ACTION_GPS_PROVIDER = "com.bwet.digifit.view.ActivityTrackerFragment.broadcastgpsprovider"
const val GPS_PROVIDER_ENABLED = "gpsEnabled"

