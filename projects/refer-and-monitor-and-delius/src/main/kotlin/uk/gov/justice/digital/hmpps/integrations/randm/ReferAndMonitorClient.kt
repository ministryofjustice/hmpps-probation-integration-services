package uk.gov.justice.digital.hmpps.integrations.randm

import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.messaging.SentReferral
import java.net.URI

interface ReferAndMonitorClient {

    @GetExchange
    fun getReferral(uri: URI): SentReferral?

    @GetExchange
    fun getSession(uri: URI): ReferralSession?

    @GetExchange
    fun getSupplierAssessment(uri: URI): SupplierAssessment?
}
