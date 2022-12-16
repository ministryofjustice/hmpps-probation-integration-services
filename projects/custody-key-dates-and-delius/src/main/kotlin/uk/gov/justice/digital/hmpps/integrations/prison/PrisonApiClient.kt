package uk.gov.justice.digital.hmpps.integrations.prison

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "prison-api", url = "\${integrations.prison-api.url}/api/bookings")
interface PrisonApiClient {
    @GetMapping(value = ["/{id}"])
    fun getBooking(
        @PathVariable("id") id: Long,
        @RequestParam basicInfo: Boolean = false,
        @RequestParam extraInfo: Boolean = true
    ): Booking

    @GetMapping(value = ["/offenderNo/{nomsId}"])
    fun getBookingFromNomsNumber(
        @PathVariable("nomsId") id: String,
        @RequestParam basicInfo: Boolean = false,
        @RequestParam extraInfo: Boolean = true
    ): Booking

    @GetMapping(value = ["/{id}/sentenceDetail"])
    fun getSentenceDetail(
        @PathVariable("id") id: Long
    ): SentenceDetail
}
