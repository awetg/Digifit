package com.bwet.digifit.utils

import androidx.appcompat.app.AppCompatDelegate

object Theme {

    private val themes = mapOf(
        "Light" to AppCompatDelegate.MODE_NIGHT_NO,
        "Dark" to AppCompatDelegate.MODE_NIGHT_YES,
        "Battery Mode" to AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY,
        "Follow System" to AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    )

    fun getThemes()= themes.keys

    fun setThem(theme: String) = AppCompatDelegate.setDefaultNightMode(themes.get(theme)!!)
}