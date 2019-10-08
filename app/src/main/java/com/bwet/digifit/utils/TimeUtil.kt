package com.bwet.digifit.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtil {

    const val secondsInHour = 3600L
    const val secondsInDay = 86400L
    const val secondsIn5Days = 86400L

    private fun getFirsDayOfThisWeek(): Calendar {
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

    fun getDate(startTimeMills: Long): String {
       return SimpleDateFormat("MMM d").format(Date(startTimeMills))
    }

    fun getDuration(startTimeMills: Long, endTimeMills: Long): String {
        val timeDiff = endTimeMills - startTimeMills
        val diffInSeconds = (timeDiff/1000)%60//TimeUnit.MILLISECONDS.toSeconds(timeDiff)
        val diffInMinutes = (timeDiff/60000)%60//TimeUnit.MILLISECONDS.toMinutes(timeDiff)
        val diffInHours = (timeDiff/3600000)%60//TimeUnit.MILLISECONDS.toHours(timeDiff)
        val duration: String =
            if (diffInHours > 0)
                "$diffInHours hrs $diffInMinutes mins $diffInSeconds secs"
            else if (diffInMinutes > 0 && diffInMinutes < 60)
                "$diffInMinutes mins $diffInSeconds secs "
            else
                "$diffInSeconds secs"
        return duration
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