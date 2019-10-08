package com.bwet.digifit.model

import android.location.Location
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.bwet.digifit.utils.gson
import com.google.gson.reflect.TypeToken

@Entity
data class ActivitySession(
    val startTimeMills: Long,
    val endTimeMills:Long,
    @TypeConverters(LocationConverter::class)
    val locationList:ArrayList<Location>,
    val distance:Double,
    var activityType:String,
    @PrimaryKey(autoGenerate = true) var id: Int = 0
)

class LocationConverter{
    @TypeConverter
    fun fromLocationList(locationList: ArrayList<Location>): String {
        return gson.toJson(locationList, object :TypeToken<ArrayList<Location>>(){}.type)
    }

    @TypeConverter
    fun fromString(locationJson: String): ArrayList<Location> {
        return gson.fromJson(locationJson, object :TypeToken<ArrayList<Location>>(){}.type)
    }
}