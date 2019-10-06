package com.bwet.digifit.utils


import android.util.Log
import com.bwet.digifit.model.StepCount
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.util.*

class StepGraphUtil (private val graph: GraphView, private val seriesColor: Int){

    companion object {
        private const val USE_BARCHART = true
        private const val BARCHART_SPACING = 30 // space between bars if we are using BarGraphSeries in percentage value

        private val calendarWeekDays = mapOf(
            Calendar.SUNDAY to "Sun",
            Calendar.MONDAY to "Mon",
            Calendar.TUESDAY to "Tue",
            Calendar.WEDNESDAY to "Wed",
            Calendar.THURSDAY to "Thu",
            Calendar.FRIDAY to "Fri",
            Calendar.SATURDAY to "Sat"
        )

    }

    // set the graph with today's data
    fun setDayGraph(stepCountList: List<StepCount>? = null) {
        setSeries(stepCountList ?: generateTodayFakeData())
        graph.gridLabelRenderer.numHorizontalLabels = 7
        graph.gridLabelRenderer.labelFormatter = DefaultLabelFormatter()
        graph.viewport.setMinX(0.0)
        graph.viewport.setMaxX(24.0)
    }

    // set the graph with this week's data
    fun setWeekGraph(stepCountList: List<StepCount>? = null) {
        setSeries(stepCountList ?: generateWeekFakeData())
        graph.gridLabelRenderer.numHorizontalLabels = 7
        graph.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                return if (isValueX) {
                    val day = value.toInt()
                    calendarWeekDays[(day + 1)] ?: super.formatLabel(value, isValueX)
                } else {
                    super.formatLabel(value, isValueX)
                }
            }
        }
        graph.viewport.setMinX(0.0)
        graph.viewport.setMaxX(6.0)
    }

    // set the graph with this month's data
    fun setMonthGraph(stepCountList: List<StepCount>? = null) {
        setSeries(stepCountList ?: generateMonthFakeData())
        graph.gridLabelRenderer.numHorizontalLabels = 5
        val monthName = TimeUtil.getShortNameOfThisMonth(null)
        graph.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                return if (isValueX) {
                    val day = if (value < 1) 1 else value.toInt()
                    if (day > 25 || day < 5) "" else "$monthName $day"
                } else {
                    super.formatLabel(value, isValueX)
                }
            }
        }
        graph.viewport.setMinX(0.0)
        graph.viewport.setMaxX(TimeUtil.getLastDayOfThisMonth().toDouble())
    }

    private fun setSeries(stepCountList: List<StepCount>){
//        stepCountList.forEach { Log.d("DBG", "count: ${it.count} interval: ${it.intervalFormat}") }
        val series = if (USE_BARCHART) {
            BarGraphSeries<DataPoint>(Array(stepCountList.size){stepCountList[it].let { DataPoint(it.intervalFormat.toDouble(), it.count.toDouble()) }}).also {
                it.spacing = BARCHART_SPACING
                it.color = seriesColor
            }
        } else {
            LineGraphSeries<DataPoint>(Array(stepCountList.size){stepCountList[it].let { DataPoint(it.intervalFormat.toDouble(), it.count.toDouble()) }})
        }
        val minY = series.lowestValueY
        graph.viewport.setMinY(if (minY < 10) 0.0 else minY/2)
        graph.viewport.setMaxY(series.highestValueY)
        graph.removeAllSeries()
        graph.addSeries(series)
    }


    /* ---- Fake data generators --- */

    private fun generateTodayFakeData(): List<StepCount> {
        return (0..24).map { StepCount((0..100).random(), it.toString().padStart(2,'0')) }.sortedBy { it.intervalFormat.toInt() }
    }

    private fun generateWeekFakeData(): List<StepCount> {
        return (0..6).map { day -> StepCount((50..200).random(), day.toString().padStart(2, '0'))}.sortedBy { it.intervalFormat.toInt() }

    }

    private fun generateMonthFakeData(): List<StepCount> {
        return (1..30).map { StepCount((0..100).random(), it.toString().padStart(2,'0')) }.sortedBy { it.intervalFormat.toInt() }
    }

}