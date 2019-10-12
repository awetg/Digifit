package com.bwet.digifit.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Step::class, ActivitySession::class], version = 1, exportSchema = false)
@TypeConverters(LocationConverter::class)
abstract class AppDB : RoomDatabase() {
    abstract fun stepDao(): StepDao
    abstract fun activitySessionDao(): ActivitySessionDao

    companion object {

        private var instance: AppDB? = null

        @Synchronized
        fun getInstance(context: Context): AppDB {
            if (instance == null) {
                instance =
                    Room.databaseBuilder(context.applicationContext, AppDB::class.java, "digifit.db")
                        .build()
            }
            return instance!!
        }
    }
}