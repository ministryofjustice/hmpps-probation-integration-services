package uk.gov.justice.digital.hmpps.integrations.approvedpremises

import org.springframework.web.service.annotation.GetExchange
import java.net.URI

interface ApprovedPremisesApiClient {
    @GetExchange
    fun getApplicationSubmittedDetails(uri: URI): EventDetails<ApplicationSubmitted>

    @GetExchange
    fun getApplicationAssessedDetails(uri: URI): EventDetails<ApplicationAssessed>

    @GetExchange
    fun getApplicationWithdrawnDetails(uri: URI): EventDetails<ApplicationWithdrawn>

    @GetExchange
    fun getBookingMadeDetails(uri: URI): EventDetails<BookingMade>

    @GetExchange
    fun getBookingChangedDetails(uri: URI): EventDetails<BookingChanged>

    @GetExchange
    fun getBookingCancelledDetails(uri: URI): EventDetails<BookingCancelled>

    @GetExchange
    fun getPersonNotArrivedDetails(uri: URI): EventDetails<PersonNotArrived>

    @GetExchange
    fun getPersonArrivedDetails(uri: URI): EventDetails<PersonArrived>

    @GetExchange
    fun getPersonDepartedDetails(uri: URI): EventDetails<PersonDeparted>
}
