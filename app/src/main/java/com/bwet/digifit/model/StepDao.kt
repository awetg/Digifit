package com.bwet.digifit.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface StepDao {

    @Insert( onConflict = REPLACE)
    fun insert(step: Step)

    // returns total number of steps inserted to database as LiveData
    @Query("SELECT COUNT(*) FROM step")
    fun getTotalStepsLive(): LiveData<Int>

    // returns total number of steps inserted to database
    @Query("SELECT COUNT(*) FROM step")
    suspend fun getTotalSteps(): Int

    // returns step between two time stamps (timestamp in mills)
    @Query("SELECT * FROM Step WHERE timeStampMills BETWEEN :startTimeMills AND :endTimeMills")
    suspend fun getStepBetweenTime(startTimeMills: Long, endTimeMills: Long): List<Step>

/*  Returns StepCount with number of steps grouped by interval(interval in seconds) of steps between two timestamp (timestamp in mills)
    for interval formats refer https://www.sqlite.org/lang_datefunc.html
    strftime('%Y-%m-%d %H:%M:%S', datetime(timeStampMills/1000, 'unixepoch', 'localtime')) */
    @Query("SELECT COUNT(timeStampMills) as count, strftime(:intervalFormat, datetime(timeStampMills/1000, 'unixepoch', 'localtime')) as intervalFormat FROM step WHERE timeStampMills  BETWEEN :startTimeMills AND :endTimeMills GROUP BY round(timeStampMills / (1000 * :intervalSec))" )
    suspend fun getStepBetweenTimeGroupedByInterval(startTimeMills: Long, endTimeMills: Long, intervalSec: Long, intervalFormat: String): List<StepCount>
}
