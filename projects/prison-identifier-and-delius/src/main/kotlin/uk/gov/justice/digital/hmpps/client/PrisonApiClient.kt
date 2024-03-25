package uk.gov.justice.digital.hmpps.client

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import java.time.LocalDate

interface PrisonApiClient {
    @GetExchange(value = "/api/prisoners/{nomsNumber}")
    fun getPrisoners(@PathVariable nomsNumber: String): List<Prisoner>

    @GetExchange(value = "/api/bookings/offenderNo/{nomsNumber}")
    fun getBooking(
        @PathVariable nomsNumber: String,
        @RequestParam basicInfo: Boolean = true,
        @RequestParam extraInfo: Boolean = false
    ): Booking

    @GetExchange(value = "/api/offender-sentences/booking/{bookingId}/sentenceTerms")
    fun getSentenceTerms(@PathVariable bookingId: Long): List<SentenceSummary>
}

data class Prisoner(
    val offenderNo: String,
    val pncNumber: String?,
    val croNumber: String?,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
)

data class Booking(
    val bookingId: Long,
    val bookingNo: String,
    val offenderNo: String,
    val activeFlag: Boolean,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
)

data class SentenceSummary(
    val startDate: LocalDate?,
    val consecutiveTo: Long?,
)