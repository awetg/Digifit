package com.bwet.digifit.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.bwet.digifit.model.ActivitySession
import com.bwet.digifit.model.AppDB

class ActivitySessionViewModel (application: Application): AndroidViewModel(application) {

    private val db = AppDB.getInstance(getApplication())

    fun insertActivitySession(session: ActivitySession) = db.activitySessionDao().insert(session)

    fun getAllSessions(): LiveData<List<ActivitySession>> = db.activitySessionDao().getAllSessions()

    fun getSessionById(id: Int): ActivitySession = db.activitySessionDao().getSessionById(id)
}