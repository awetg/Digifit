package com.bwet.digifit.view


import android.animation.ObjectAnimator
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bwet.digifit.R
import com.bwet.digifit.model.StepCount
import com.bwet.digifit.utils.RuntimePermissionUtil
import com.bwet.digifit.utils.StepGraphUtil
import com.bwet.digifit.utils.TimeUtil
import com.bwet.digifit.viewModel.StepViewModel
import com.google.android.material.tabs.TabLayout
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GridLabelRenderer
import kotlinx.android.synthetic.main.fragment_pedometer.*
import kotlinx.android.synthetic.main.fragment_pedometer.view.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*


class PedometerFragment : BaseFragment() {

    private lateinit var stepViewModel: StepViewModel
    private lateinit var graphUtil: StepGraphUtil

    companion object {
        private const val FAKE_DATA = false

        @JvmStatic
        fun newInstance() = PedometerFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_pedometer, container, false)

        activity?.let { RuntimePermissionUtil.getInstance(it).requestActivityRecognition() }

        stepViewModel = ViewModelProviders.of(this).get(StepViewModel::class.java)
        stepViewModel.getTotalStepsLive().observe(this, Observer {
            view.step_count_txt.text = it.toString()
            animateProgressBar(it)
        })

        view.grap_title_txt.text = TimeUtil.getTodayGraphText()

        graphUtil = StepGraphUtil(view.step_graph,context?.getColor(R.color.colorPrimary) ?: Color.GREEN)

        view.step_graph.viewport.isXAxisBoundsManual = true
        view.step_graph.viewport.isYAxisBoundsManual = true
        view.step_graph.gridLabelRenderer.gridStyle = GridLabelRenderer.GridStyle.HORIZONTAL
        launch {
            graphUtil.setDayGraph(getStepCountList(TimeUtil.getStartAndEndOfToday(), TimeUtil.secondsInHour, "%H"))
        }


        view.step_grap_tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position) {
                    0 -> {
                        launch {
                            graphUtil.setDayGraph(getStepCountList(TimeUtil.getStartAndEndOfToday(), TimeUtil.secondsInHour, "%H"))
                            view.grap_title_txt.text = TimeUtil.getTodayGraphText()
                        }
                    }
                    1 -> {
                        launch {
                            graphUtil.setWeekGraph(getStepCountList(TimeUtil.getFirstAndLastDaysOfThisWeek(), TimeUtil.secondsInDay, "%w"))
                            view.grap_title_txt.text = TimeUtil.getWeekGraphText()
                        }
                    }
                    2 -> {
                        launch {
                            graphUtil.setMonthGraph(getStepCountList(TimeUtil.getFirstAndLastDaysOfThisMonth(), TimeUtil.secondsInDay, "%d"))
                            view.grap_title_txt.text = TimeUtil.getMonthGraphText()
                        }
                    }
                }
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })

        return view
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            RuntimePermissionUtil.PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //permission granted
                } else {
                    activity?.let {
                        val runtimePermissionUtil = RuntimePermissionUtil.getInstance(it)
                        runtimePermissionUtil.showDialogAndAsk(
                            getString(R.string.activity_recognition),
                            DialogInterface.OnClickListener{_, _ -> runtimePermissionUtil.requestActivityRecognition()}
                        )
                    }
                }
            }
        }
    }

    // Animate progressbar from last progress value up to the newValue passed to function
    private fun animateProgressBar(newValue: Int) {
        val animator = ObjectAnimator.ofInt(step_progressBar, "progress", step_progressBar.progress , newValue)
        animator.duration = 5000
        animator.interpolator = DecelerateInterpolator()
        animator.start()
        step_progressBar.clearAnimation()
    }

    private suspend fun getStepCountList(between: Pair<Calendar, Calendar>, secondsInterval: Long, intervalFormat: String): List<StepCount>? {
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
