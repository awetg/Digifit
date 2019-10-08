package com.bwet.digifit.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object TimeUtil {

    const val secondsInHour = 3600L
    const val secondsInDay = 86400L
    const val secondsIn5Days = 86400L

    fun getFirsDayOfThisWeek(): Calendar {
        val cal = getClearCalendar()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        return cal
    }

    fun getFirstAndLastDaysOfThisWeek(): Pair<Calendar, Calendar> {
        val first = getFirsDayOfThisWeek()
        val last = getFirsDayOfThisWeek()
        last.add(Calendar.WEEK_OF_YEAR, 1)
        last.add(Calendar.DAY_OF_WEEK, -1)
        return Pair(first, last)
    }

    fun getFirstAndLastDaysOfThisMonth(): Pair<Calendar, Calendar> {
        val first = getClearCalendar()
        first.set(Calendar.DAY_OF_MONTH, 1)
        val last = getClearCalendar()
        last.set(Calendar.DAY_OF_MONTH, 1)
        last.set(Calendar.DAY_OF_MONTH, last.getActualMaximum(Calendar.DAY_OF_MONTH))
        return Pair(first, last)
    }

    fun getStartAndEndOfToday(): Pair<Calendar, Calendar> {
        val start = getClearCalendar()
        val end = getClearCalendar()
        end.add(Calendar.DAY_OF_MONTH, 1)
        return Pair(start, end)

    }

    fun getLastDayOfThisMonth(): Int = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)

    fun getShortNameOfThisMonth(cal: Calendar?): String = SimpleDateFormat("MMM").format(Date(cal?.timeInMillis ?: System.currentTimeMillis()))

    fun getTodayGraphText(): String = SimpleDateFormat("E, MMM d").format(Date(System.currentTimeMillis()))

    fun getWeekGraphText(): String {
        val (start, end) = getFirstAndLastDaysOfThisWeek()
        val sdf = SimpleDateFormat("MMM d")
        return sdf.format(Date(start.timeInMillis)) + " - " + sdf.format(Date(end.timeInMillis))
    }

    fun getMonthGraphText(): String {
        val (start, _) = getFirstAndLastDaysOfThisMonth()
        val sdf = SimpleDateFormat("MMMM YYYY")
        return sdf.format(Date(start.timeInMillis))
    }

    fun getActivityTimeAndDuration(startTimeMills: Long, endTimeMills: Long): String {
        val timeDiff = endTimeMills - startTimeMills
        val diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(timeDiff)
        val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff)
        val diffInHours = TimeUnit.MILLISECONDS.toHours(timeDiff)
        val duration: String =
            if (diffInHours > 0)
                "$diffInHours hr"
            else if (diffInMinutes > 0 || diffInMinutes < 61)
                "$diffInMinutes min"
            else "$diffInSeconds sec"
        val time = SimpleDateFormat("HH:mm MMM d").format(Date(startTimeMills))
        return "$time - $duration"
    }

    private fun getClearCalendar(): Calendar {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)
        return cal
    }
}