package uk.gov.justice.digital.hmpps.sevice.model

import java.time.DateTimeException
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs

data object DateMatcher {

    fun variations(date: LocalDate): List<LocalDate> = buildList {
        swapMonthDay(date)?.also(::add)
        addAll(aroundDateInSameMonth(date))
        addAll(everyOtherValidMonth(date))
    }

    private fun aroundDateInSameMonth(date: LocalDate) =
        listOf(date.minusDays(1), date.minusDays(-1), date).filter { it.month == date.month }

    private fun everyOtherValidMonth(date: LocalDate): List<LocalDate> =
        (1..12).filterNot { date.monthValue == it }.mapNotNull { setMonthDay(date, it) }

    private fun swapMonthDay(date: LocalDate): LocalDate? = try {
        LocalDate.of(date.year, date.dayOfMonth, date.monthValue)
    } catch (e: DateTimeException) {
        null
    }

    private fun setMonthDay(date: LocalDate, monthValue: Int): LocalDate? = try {
        LocalDate.of(date.year, monthValue, date.dayOfMonth)
    } catch (e: DateTimeException) {
        null
    }
}

fun LocalDate.withinDays(date: LocalDate, days: Int = 7): Boolean = abs(ChronoUnit.DAYS.between(this, date)) <= days
