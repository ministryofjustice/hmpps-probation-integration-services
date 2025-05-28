package uk.gov.justice.digital.hmpps.api.model

import java.time.DateTimeException
import java.time.LocalDate

data class LenientDate(val date: LocalDate) {
    fun allVariations(): Set<LocalDate> = buildSet {
        addAll(aroundDateInSameMonth())
        addAll(otherValidMonths())
        swapMonthDay()?.let { add(it) }
    }

    private fun aroundDateInSameMonth() =
        setOf(date.minusDays(1), date.plusDays(1), date).filter { it.month == date.month }

    private fun otherValidMonths() =
        (1..12).filterNot { date.monthValue == it }.mapNotNull { setMonthDay(date, it) }

    private fun swapMonthDay() = try {
        LocalDate.of(date.year, date.dayOfMonth, date.monthValue)
    } catch (_: DateTimeException) {
        null
    }

    private fun setMonthDay(date: LocalDate, monthValue: Int) = try {
        LocalDate.of(date.year, monthValue, date.dayOfMonth)
    } catch (_: DateTimeException) {
        null
    }
}