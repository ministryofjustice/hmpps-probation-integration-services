package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.config.security.nullIfNotFound
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.integrations.cvl.ActivatedLicence
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent

@Component
class LicenceActivatedHandler(
    private val detailService: DomainEventDetailService,
    private val lca: LicenceConditionApplier
) {
    fun licenceActivated(domainEvent: HmppsDomainEvent): List<ActionResult> {
        val properties = listOfNotNull(
            domainEvent.personReference.findCrn()?.let { "crn" to it },
            domainEvent.detailUrl?.let { "url" to it }).toMap()
        return try {
            val crn = requireNotNull(domainEvent.personReference.findCrn()) { "No CRN Provided" }
            val activatedLicence: ActivatedLicence = nullIfNotFound { detailService.getDetail(domainEvent) }
                ?: return listOf(ActionResult.Ignored("Licence not found", properties))
            lca.applyLicenceConditions(crn, activatedLicence, domainEvent.occurredAt)
        } catch (e: Exception) {
            listOf(ActionResult.Failure(e, properties))
        }
    }
}
