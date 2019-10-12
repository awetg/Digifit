package com.bwet.digifit.view


import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.lifecycle.Observer
import com.bwet.digifit.R
import com.bwet.digifit.utils.RuntimePermissionUtil
import com.bwet.digifit.utils.StepGraphUtil
import com.bwet.digifit.utils.TimeUtil
import com.bwet.digifit.utils.User
import com.google.android.material.tabs.TabLayout
import com.jjoe64.graphview.GridLabelRenderer
import kotlinx.android.synthetic.main.fragment_pedometer.*
import kotlinx.android.synthetic.main.fragment_pedometer.view.*
import kotlinx.android.synthetic.main.graph_card.view.*
import kotlinx.coroutines.launch
import java.text.DecimalFormat


class PedometerFragment : GraphBaseFragment() {

    private lateinit var graphUtil: StepGraphUtil

    companion object {

        @JvmStatic
        fun newInstance() = PedometerFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_pedometer, container, false)

        view.step_progressBar.secondaryProgress = User.dailyStepGoal
        view.step_progressBar.max = User.dailyStepGoal

        activity?.let { RuntimePermissionUtil.getInstance(it).requestActivityRecognition() }

        val todayStartAndEnd = TimeUtil.getStartAndEndOfToday()

        stepViewModel.getStepCountBetweenTimeLive(todayStartAndEnd.first.timeInMillis, todayStartAndEnd.second.timeInMillis).observe(this, Observer {
            animateProgressBar(it)
            view.step_count_txt.text = it.toString()
            view.step_percent_txt.text = "${DecimalFormat("##").format((it.toDouble() / User.dailyStepGoal) * 100)}%"
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

    // Animate progressbar from last progress value up to the newValue passed to function
    private fun animateProgressBar(newValue: Int) {
        val animator = ObjectAnimator.ofInt(step_progressBar, "progress", step_progressBar.progress , newValue)
        animator.duration = 5000
        animator.interpolator = DecelerateInterpolator()
        animator.start()
        step_progressBar.clearAnimation()
    }
}
