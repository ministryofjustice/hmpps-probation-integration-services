package uk.gov.justice.digital.hmpps.integrations.prison

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange

interface PrisonApiClient {
    @GetExchange(value = "/{id}")
    fun getBooking(
        @PathVariable("id") id: Long,
        @RequestParam basicInfo: Boolean = false,
        @RequestParam extraInfo: Boolean = true
    ): Booking

    @GetExchange(value = "/offenderNo/{nomsId}")
    fun getBookingFromNomsNumber(
        @PathVariable("nomsId") id: String
    ): Booking

    @GetExchange(value = "/{id}/sentenceDetail")
    fun getSentenceDetail(
        @PathVariable("id") id: Long
    ): SentenceDetail
}
