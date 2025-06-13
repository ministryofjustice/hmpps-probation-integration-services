package uk.gov.justice.digital.hmpps.client

import com.fasterxml.jackson.annotation.JsonAlias
import org.springframework.web.service.annotation.GetExchange
import java.time.LocalDate

interface BankHolidayClient {
    @GetExchange(url = "/bank-holidays.json")
    fun getBankHolidays(): Holidays
}

data class Holidays(
    @JsonAlias("england-and-wales")
    val englandAndWales: BankHolidays,
)

data class BankHolidays(
    val events: List<Event>
)

data class Event(
    val title: String,
    val date: LocalDate,
    val notes: String,
    val bunting: Boolean
)
