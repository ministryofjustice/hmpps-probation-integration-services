package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.entity.sentence.component.SentenceComponent
import uk.gov.justice.digital.hmpps.integration.StatusInfo
import uk.gov.justice.digital.hmpps.integration.StatusInfo.EntityType
import uk.gov.justice.digital.hmpps.integration.StatusInfo.Status
import uk.gov.justice.digital.hmpps.repository.*
import java.time.ZonedDateTime
import java.util.*

@Transactional
@Service
class StatusChangeService(
    private val licenceConditionRepository: LicenceConditionRepository,
    private val requirementRepository: RequirementRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository,
    private val componentTerminationService: ComponentTerminationService
) {
    fun statusChanged(messageId: UUID, crn: String, occurredAt: ZonedDateTime, info: StatusInfo) {
        val component = info.getComponent(crn)
        contactRepository.save(info.asStatusChangeContact(messageId, component, occurredAt))
        if (info.newStatus == Status.PROGRAMME_COMPLETE) {
            componentTerminationService.terminate(component, occurredAt)
        }
    }

    private fun StatusInfo.getComponent(crn: String): SentenceComponent = when (sourcedFromEntityType) {
        EntityType.LICENCE_CONDITION -> licenceConditionRepository.findByIdOrNotFound(sourcedFromEntityId)
        EntityType.REQUIREMENT -> requirementRepository.findByIdOrNotFound(sourcedFromEntityId)
    }.also { require(crn == it.disposal.event.person.crn) { "CRN and component do not match" } }

    private fun StatusInfo.asStatusChangeContact(
        messageId: UUID,
        component: SentenceComponent,
        occurredAt: ZonedDateTime
    ): Contact {
        val event = component.disposal.event
        val manager = requireNotNull(event.person.manager) { "Person manager not found" }
        return Contact(
            person = event.person.asPersonCrn(),
            event = event,
            component = component,
            date = occurredAt.toLocalDate(),
            startTime = occurredAt,
            notes = notes,
            sensitive = false,
            provider = manager.team.provider,
            team = manager.team,
            staff = manager.staff,
            type = contactTypeRepository.getByCode(newStatus.contactTypeCode),
            externalReference = "urn:uk:gov:hmpps:accredited-programmes-service:$messageId"
        )
    }
}
