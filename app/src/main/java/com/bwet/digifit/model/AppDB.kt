package com.bwet.digifit.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Step::class], version = 1, exportSchema = false)
abstract class AppDB : RoomDatabase() {
    abstract fun stepDao(): StepDao

    companion object {

        private var instance: AppDB? = null

        @Synchronized
        fun getInstance(context: Context): AppDB {
            if (instance == null) {
                instance =
                    Room.databaseBuilder(context.applicationContext, AppDB::class.java, "contact.db")
                        .build()
            }
            return instance!!
        }
    }
}