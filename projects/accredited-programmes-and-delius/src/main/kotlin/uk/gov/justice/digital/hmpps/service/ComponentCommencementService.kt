package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.datetime.toDeliusDate
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.entity.contact.ContactType
import uk.gov.justice.digital.hmpps.entity.sentence.component.SentenceComponent
import uk.gov.justice.digital.hmpps.integration.EntityType
import uk.gov.justice.digital.hmpps.integration.ReferralStart
import uk.gov.justice.digital.hmpps.repository.*
import uk.gov.justice.digital.hmpps.service.ComponentCommencementService.Result.ResultName.ComponentAlreadyCommenced
import uk.gov.justice.digital.hmpps.service.ComponentCommencementService.Result.ResultName.ComponentCommenced
import java.time.ZonedDateTime

@Service
@Transactional
class ComponentCommencementService(
    private val requirementRepository: RequirementRepository,
    private val licenceConditionRepository: LicenceConditionRepository,
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository,
) {
    fun commence(crn: String, detail: ReferralStart): Result {
        val startedAt = detail.licReqStartedAt.atZone(EuropeLondon)
        val component = detail.getComponent(crn)
        return if (component.commencementDate != null) {
            Result(ComponentAlreadyCommenced, component.telemetry(crn, startedAt))
        } else {
            component
                .apply { setCommencement(startedAt) }
                .createCommencementContact(startedAt)
                .let { Result(ComponentCommenced, it.telemetry(crn, startedAt)) }
        }
    }

    private fun ReferralStart.getComponent(crn: String) = when (sourcedFromEntityType) {
        EntityType.REQUIREMENT -> requirementRepository.findByIdOrNotFound(licReqId.toLong())
        EntityType.LICENCE_CONDITION -> licenceConditionRepository.findByIdOrNotFound(licReqId.toLong())
    }.also { require(crn == it.disposal.event.person.crn) { "CRN and component do not match" } }

    private fun SentenceComponent.setCommencement(startedAt: ZonedDateTime) {
        val date = startedAt.toLocalDate()
        commencementDate = date.atStartOfDay(EuropeLondon)
        notes = listOfNotNull(
            notes,
            "Actual Start Date set to ${date.toDeliusDate()} following notification from the Accredited Programmes – Intervention Service"
        ).joinToString(System.lineSeparator() + System.lineSeparator())
    }

    private fun SentenceComponent.createCommencementContact(startedAt: ZonedDateTime) = also {
        val manager = checkNotNull(manager) { "$type with id=$id has no manager" }
        contactRepository.save(
            Contact(
                person = disposal.event.person.asPersonCrn(),
                event = disposal.event,
                component = this,
                date = startedAt.toLocalDate(),
                provider = manager.team.provider,
                team = manager.team,
                staff = manager.staff,
                type = contactTypeRepository.getByCode(ContactType.ORDER_COMPONENT_COMMENCED),
            )
        )
    }

    private fun SentenceComponent.telemetry(crn: String, startedAt: ZonedDateTime) = mapOf(
        "crn" to crn,
        "type" to this::class.simpleName!!,
        "componentId" to id.toString(),
        "eventStartDate" to startedAt.toLocalDate().toString(),
        "commencementDate" to commencementDate?.withZoneSameInstant(EuropeLondon)?.toLocalDate()?.toString(),
    )

    data class Result(val name: ResultName, val data: Map<String, String?>) {
        enum class ResultName {
            ComponentCommenced,
            ComponentAlreadyCommenced,
        }
    }
}
