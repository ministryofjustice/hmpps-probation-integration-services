package uk.gov.justice.digital.hmpps.utils

import java.time.DateTimeException
import java.time.LocalDate

object SearchHelpers {
    fun allLenientDateVariations(date: LocalDate): List<LocalDate> {
        return swapMonthDay(date) + everyOtherValidMonth(date) + aroundDateInSameMonth(date)
    }

    fun formatPncNumber(pncNumber: String): String {
        return pncNumber.split("/").takeIf { it.size == 2 }?.let {
            try {
                it[0] + "/" + "%07d".format(it[1].substring(0, it[1].length - 1).toInt()) + it[1].last().toString()
            } catch (e: Exception) {
                pncNumber
            }
        } ?: pncNumber
    }

    private fun aroundDateInSameMonth(date: LocalDate) =
        listOf(date.minusDays(1), date.minusDays(-1), date).filter { it.month == date.month }

    private fun everyOtherValidMonth(date: LocalDate): List<LocalDate> =
        (1..12).filterNot { date.monthValue == it }.mapNotNull { setMonthDay(date, it) }

    private fun swapMonthDay(date: LocalDate): List<LocalDate> = try {
        listOf(LocalDate.of(date.year, date.dayOfMonth, date.monthValue))
    } catch (e: DateTimeException) {
        listOf()
    }

    private fun setMonthDay(date: LocalDate, monthValue: Int): LocalDate? = try {
        LocalDate.of(date.year, monthValue, date.dayOfMonth)
    } catch (e: DateTimeException) {
        null
    }
}