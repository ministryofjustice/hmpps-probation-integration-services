package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.config.security.nullIfNotFound
import uk.gov.justice.digital.hmpps.integrations.cvl.CvlClient
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import java.net.URI

@Component
class LicenceActivatedHandler(
    private val cvlClient: CvlClient,
    private val lca: LicenceConditionApplier
) {
    fun licenceActivated(domainEvent: HmppsDomainEvent): List<ActionResult> {
        val properties = listOfNotNull(
            domainEvent.personReference.findCrn()?.let { "crn" to it },
            domainEvent.detailUrl?.let { "url" to it }).toMap()
        return try {
            val (crn, url) = validateEvent(domainEvent)
            val activatedLicence = nullIfNotFound { cvlClient.getActivatedLicence(url) }
                ?: return listOf(ActionResult.Ignored("Licence not found", properties))
            lca.applyLicenceConditions(crn, activatedLicence, domainEvent.occurredAt)
        } catch (e: Exception) {
            listOf(ActionResult.Failure(e, properties))
        }
    }

    private fun validateEvent(domainEvent: HmppsDomainEvent): Pair<String, URI> {
        val crn = domainEvent.personReference.findCrn() ?: throw IllegalArgumentException("No CRN Provided")
        val url = domainEvent.detailUrl ?: throw IllegalArgumentException("No Detail Url Provided")
        return crn to URI.create(url)
    }
}
