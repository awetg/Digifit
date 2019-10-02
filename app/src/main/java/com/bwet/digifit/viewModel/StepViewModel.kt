package com.bwet.digifit.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.bwet.digifit.model.AppDB
import com.bwet.digifit.model.Step
import com.bwet.digifit.model.StepCount

class StepViewModel(application: Application): AndroidViewModel(application) {

    private val db = AppDB.getInstance(getApplication())

    suspend fun getTotalSteps(): Int = db.stepDao().getTotalStep()

    fun addStep(step: Step) = db.stepDao().insert(step)

    /*  Returns StepCount with number of steps grouped by interval(interval in seconds) of steps between two timestamp (timestamp in mills)
    for interval formats refer https://www.sqlite.org/lang_datefunc.html
    strftime('%Y-%m-%d %H:%M:%S', datetime(timeStampMills/1000, 'unixepoch', 'localtime')) */
    suspend fun getStepCountByInterval(
        startTimeMills: Long, endTimeMills: Long, intervalSec: Long, intervalFormat: String
    ): List<StepCount> = db.stepDao().getStepBetweenTimeGroupedByInterval(startTimeMills, endTimeMills, intervalSec, intervalFormat)
}