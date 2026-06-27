package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtil {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val displayFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    fun getCurrentIsoString(): String {
        return isoFormat.format(Date())
    }

    fun getIsoString30DaysAgo(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -30)
        return isoFormat.format(cal.time)
    }

    fun formatIsoToDisplay(isoString: String): String {
        return try {
            val date = isoFormat.parse(isoString)
            if (date != null) {
                displayFormat.format(date)
            } else {
                isoString
            }
        } catch (e: Exception) {
            isoString
        }
    }
}
