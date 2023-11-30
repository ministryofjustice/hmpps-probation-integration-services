package uk.gov.justice.digital.hmpps.integrations.approvedpremesis

import org.springframework.web.service.annotation.GetExchange
import java.net.URI

interface Cas3ApiClient {
    @GetExchange
    fun getApplicationSubmittedDetails(uri: URI): EventDetails<ApplicationSubmitted>

    @GetExchange
    fun getBookingCancelledDetails(uri: URI): EventDetails<BookingCancelled>

    @GetExchange
    fun getBookingConfirmedDetails(uri: URI): EventDetails<BookingConfirmed>

    @GetExchange
    fun getBookingProvisionallyMade(uri: URI): EventDetails<BookingProvisional>

    @GetExchange
    fun getPersonArrived(uri: URI): EventDetails<PersonArrived>

    @GetExchange
    fun getPersonDeparted(uri: URI): EventDetails<PersonDeparted>
}
