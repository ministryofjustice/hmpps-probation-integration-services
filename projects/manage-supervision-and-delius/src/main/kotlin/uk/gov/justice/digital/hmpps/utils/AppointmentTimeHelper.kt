package uk.gov.justice.digital.hmpps.utils

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object AppointmentTimeHelper {
    fun startAndEnd(startDate: ZonedDateTime, endDate: ZonedDateTime): String {
        val startTime = replace(startDate.format(DateTimeFormatter.ofPattern("h:mm a")))
        val endTime = replace(endDate.format(DateTimeFormatter.ofPattern("h:mm a")))
        return "$startTime to $endTime"
    }

    private fun replace(time: String) = time.replace(" ", "").replace(":00", "").lowercase()
}
