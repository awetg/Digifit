package com.bwet.digifit.model

import android.location.Location
import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.bwet.digifit.utils.gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

@Entity
data class ActivitySession(
    val startTimeMills: Long,
    val endTimeMills:Long,
    @TypeConverters(LocationConverter::class)
    val locationList:MutableList<MyLocation>,
    val distance:Double,
    var activityType:String,
    @PrimaryKey(autoGenerate = true) var id: Int = 0
)

class LocationConverter{
    @TypeConverter
    fun fromLocationList(locationList: MutableList<MyLocation>): String {
        return gson.toJson(locationList, object :TypeToken<MutableList<MyLocation>>(){}.type)
    }

    @TypeConverter
    fun fromString(locationJson: String): MutableList<MyLocation> {
        return gson.fromJson(locationJson, object :TypeToken<MutableList<MyLocation>>(){}.type)
    }
}