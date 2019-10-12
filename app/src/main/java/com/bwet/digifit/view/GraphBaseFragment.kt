package com.bwet.digifit.view

import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.bwet.digifit.R
import com.bwet.digifit.model.StepCount
import com.bwet.digifit.utils.RuntimePermissionUtil
import com.bwet.digifit.viewModel.StepViewModel
import kotlinx.coroutines.async
import java.util.*

abstract class GraphBaseFragment : BaseFragment() {

    lateinit var stepViewModel: StepViewModel

    companion object {
        private const val FAKE_DATA = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        stepViewModel = ViewModelProviders.of(this).get(StepViewModel::class.java)
        super.onCreate(savedInstanceState)
    }

    suspend fun getStepCountList(between: Pair<Calendar, Calendar>, secondsInterval: Long, intervalFormat: String): List<StepCount>? {
        return if (FAKE_DATA)
            null
        else async {
            stepViewModel.getStepCountByInterval(
                between.first.timeInMillis,
                between.second.timeInMillis,
                secondsInterval,
                intervalFormat
            )
        }.await()
    }
}