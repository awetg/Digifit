package com.bwet.digifit.view

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer

import com.bwet.digifit.R
import com.bwet.digifit.model.StepCount
import com.bwet.digifit.utils.CalorieAndDistanceCalculator
import com.bwet.digifit.utils.StepGraphUtil
import com.bwet.digifit.utils.TimeUtil
import com.google.android.material.tabs.TabLayout
import com.jjoe64.graphview.GridLabelRenderer
import kotlinx.android.synthetic.main.daily_count_layout.view.*
import kotlinx.android.synthetic.main.graph_card.view.*
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class CaloriesFragment : GraphBaseFragment() {

    private lateinit var graphUtil: StepGraphUtil
    companion object {
        fun newInstance() = CaloriesFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_calories, container, false)

        view.grap_title_txt.text = TimeUtil.getTodayGraphText()
        graphUtil = StepGraphUtil(view.step_graph,context?.getColor(R.color.colorPrimary) ?: Color.GREEN)

        view.dail_count_unit_txt.text = getString(R.string.calorieUnit)
        view.dail_count_description_txt.text = getString(R.string.txtCaloriesBurned)

        view.step_graph.viewport.isXAxisBoundsManual = true
        view.step_graph.viewport.isYAxisBoundsManual = true
        view.step_graph.gridLabelRenderer.gridStyle = GridLabelRenderer.GridStyle.HORIZONTAL

        val todayStartAndEnd = TimeUtil.getStartAndEndOfToday()

        stepViewModel.getStepCountBetweenTimeLive(todayStartAndEnd.first.timeInMillis, todayStartAndEnd.second.timeInMillis).observe(this, Observer {
            view.daily_count_txt.text = DecimalFormat(".##").format(CalorieAndDistanceCalculator.calculateForSteps(it))
        })


        launch {
            graphUtil.setDayGraph(
                getStepCountList(TimeUtil.getStartAndEndOfToday(), TimeUtil.secondsInHour, "%H")
                    ?.map { StepCount(CalorieAndDistanceCalculator.calculateForSteps(it.count).toInt(), it.intervalFormat) }
            )
        }

        view.step_grap_tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position) {
                    0 -> {
                        launch {
                            graphUtil.setDayGraph(
                                getStepCountList(TimeUtil.getStartAndEndOfToday(), TimeUtil.secondsInHour, "%H")
                                    ?.map { StepCount(CalorieAndDistanceCalculator.calculateForSteps(it.count).toInt(), it.intervalFormat) }
                            )
                            view.grap_title_txt.text = TimeUtil.getTodayGraphText()
                        }
                    }
                    1 -> {
                        launch {
                            graphUtil.setWeekGraph(
                                getStepCountList(TimeUtil.getFirstAndLastDaysOfThisWeek(), TimeUtil.secondsInDay, "%w")
                                    ?.map { StepCount(CalorieAndDistanceCalculator.calculateForSteps(it.count).toInt(), it.intervalFormat) }
                            )
                            view.grap_title_txt.text = TimeUtil.getWeekGraphText()
                        }
                    }
                    2 -> {
                        launch {
                            graphUtil.setMonthGraph(
                                getStepCountList(TimeUtil.getFirstAndLastDaysOfThisMonth(), TimeUtil.secondsInDay, "%d")
                                    ?.map { StepCount(CalorieAndDistanceCalculator.calculateForSteps(it.count).toInt(), it.intervalFormat) }
                            )
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
}
