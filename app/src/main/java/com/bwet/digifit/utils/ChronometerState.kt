package com.bwet.digifit.utils

data class ChronometerState(val startTime: Long, val pauseOffset: Long,val elapsedTime: Long)

data class SessionState(val startTime: Long, val elapsedTime: Long, val activityType: String)