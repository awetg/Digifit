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
const val SERVICE_PERFERENCE_KEY_START_TIME = "intentStartTime"
const val SERVICE_PERFERENCE_KEY_ELAPSED_TIME = "intentPauseOffset"
const val SERVICE_PERFERENCE_KEY_SELECTED_ACTIVITY = "typeOfActivity"
const val STOP_SERVICE_FLAG_KEY = "stopLivedata"
const val PUASE_SERVICE_FLAG_KEY = "PAUSELivedata"

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


const val BROADCAST_ACTION_GPS_PROVIDER = "broadcastAvtionGpsEnabled"
const val BROADCAST_ACTION_LOCATION_EMPTY = "broadcatsActionLocaionListEmpt"
const val GPS_PROVIDER_ENABLED = "gpsEnabled"

const val BROADCAST_ACTION_STOP_PAUSE_SERVICE = "stopAction"
const val INTENT_KEY_STOP_SERVICE = "stopService"
const val INTENT_KEY_PAUSE_SERVICE = "pauseService"

