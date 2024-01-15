package uk.gov.justice.digital.hmpps.client.approvedpremises

import org.springframework.web.service.annotation.GetExchange
import java.net.URI

interface EventDetailsClient {
    @GetExchange
    fun getApplicationSubmittedDetails(uri: URI): EventDetails<ApplicationSubmitted>
}
