package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.cvl.CvlClient
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import java.net.URI

@Component
class LicenceActivatedHandler(
    private val cvlClient: CvlClient,
    private val lca: LicenceConditionApplier,
) {
    fun licenceActivated(domainEvent: HmppsDomainEvent): List<ActionResult> =
        try {
            val (crn, url) = validateEvent(domainEvent)
            val activatedLicence =
                cvlClient.getActivatedLicence(url)
                    ?: throw NotFoundException("Activated Licence", "detailUrl", url)
            lca.applyLicenceConditions(crn, activatedLicence, domainEvent.occurredAt)
        } catch (e: Exception) {
            listOf(ActionResult.Failure(e))
        }

    private fun validateEvent(domainEvent: HmppsDomainEvent): Pair<String, URI> {
        val crn = domainEvent.personReference.findCrn() ?: throw IllegalArgumentException("No CRN Provided")
        val url = domainEvent.detailUrl ?: throw IllegalArgumentException("No Detail Url Provided")
        return crn to URI.create(url)
    }
}
