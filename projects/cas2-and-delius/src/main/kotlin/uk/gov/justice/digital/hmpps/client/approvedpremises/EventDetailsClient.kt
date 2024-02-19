package uk.gov.justice.digital.hmpps.client.approvedpremises

import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.client.approvedpremises.model.ApplicationStatusUpdated
import uk.gov.justice.digital.hmpps.client.approvedpremises.model.ApplicationSubmitted
import uk.gov.justice.digital.hmpps.client.approvedpremises.model.EventDetails
import java.net.URI

interface EventDetailsClient {
    @GetExchange
    fun getApplicationSubmittedDetails(uri: URI): EventDetails<ApplicationSubmitted>

    @GetExchange
    fun getApplicationStatusUpdatedDetails(uri: URI): EventDetails<ApplicationStatusUpdated>
}
