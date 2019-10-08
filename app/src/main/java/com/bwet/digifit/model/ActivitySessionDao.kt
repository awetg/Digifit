package com.bwet.digifit.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface ActivitySessionDao {

    @Insert( onConflict = REPLACE)
    fun insert(session: ActivitySession)

    @Query("SELECT * FROM activitysession")
    fun getAllSessions(): LiveData<List<ActivitySession>>

    @Query("SELECT * FROM activitysession WHERE id= :id")
    suspend fun getSessionById(id: Int): ActivitySession
}