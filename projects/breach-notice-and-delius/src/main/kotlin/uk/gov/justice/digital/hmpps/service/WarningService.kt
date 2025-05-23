package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.*
import uk.gov.justice.digital.hmpps.integrations.delius.Document.Companion.breachNoticeUrn
import uk.gov.justice.digital.hmpps.model.EnforceableContact
import uk.gov.justice.digital.hmpps.model.WarningDetails
import uk.gov.justice.digital.hmpps.model.WarningTypesResponse
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId.systemDefault
import java.util.*

@Service
@Transactional(readOnly = true)
class WarningService(
    private val rdRepository: ReferenceDataRepository,
    private val documentRepository: DocumentRepository,
    private val disposalRepository: DisposalRepository,
    private val contactRepository: ContactRepository,
    private val requirementRepository: RequirementRepository
) {
    fun getWarningTypes(crn: String, breachNoticeId: UUID): WarningTypesResponse {
        val disposal = documentRepository.findEventIdFromDocument(breachNoticeUrn(breachNoticeId))
            ?.let { crn.disposalForEvent(it) }
        return WarningTypesResponse(
            warningTypes = rdRepository
                .findByDatasetCodeAndSelectableTrue(Dataset.BREACH_NOTICE_TYPE).codedDescriptions(),
            sentenceTypes = rdRepository
                .findByDatasetCodeAndSelectableTrue(Dataset.BREACH_SENTENCE_TYPE).sentenceTypes(),
            defaultSentenceTypeCode = disposal!!.type.defaultSentenceTypeCode()
        )
    }

    fun getWarningDetails(crn: String, breachNoticeId: UUID): WarningDetails {
        val breachReasons = rdRepository.findByDatasetCodeAndSelectableTrue(Dataset.BREACH_REASON)
        val disposal = documentRepository.findEventIdFromDocument(breachNoticeUrn(breachNoticeId))
            ?.let { crn.disposalForEvent(it) }
        val enforceableContacts =
            contactRepository.findEnforceableContacts(disposal!!.event.id)

        return WarningDetails(
            breachReasons = breachReasons.codedDescriptions(),
            enforceableContacts = enforceableContacts.map(Contact::toEnforceableContact),
            unpaidWorkRequirements = enforceableContacts.filter { it.unpaidWorkAppointments?.isNotEmpty() == true }
                .let {
                    requirementRepository.getUnpaidWorkRequirementsByDisposal(disposal.id)
                        .map { it.toModel() }
                }
        )
    }

    private fun String.disposalForEvent(eventId: Long): Disposal =
        requireNotNull(disposalRepository.getByEventId(eventId)) { "Event with id $eventId is not sentenced" }
            .takeIf { this == it.event.person.crn }
            ?: throw NotFoundException("Breach Notice not found")
}

fun Contact.toEnforceableContact() = EnforceableContact(
    id,
    LocalDateTime.of(date, startTime?.withZoneSameInstant(systemDefault())?.toLocalTime() ?: LocalTime.MIN),
    description,
    type.codedDescription(),
    outcome!!.codedDescription(),
    notes,
    requirement?.toModel() ?: pssRequirement?.toModel(),
)

fun Requirement.toModel() = uk.gov.justice.digital.hmpps.model.Requirement(
    id,
    checkNotNull(mainCategory?.codedDescription()),
    subCategory?.codedDescription(),
)

fun PssRequirement.toModel() = uk.gov.justice.digital.hmpps.model.Requirement(
    id,
    checkNotNull(mainCategory?.codedDescription()),
    subCategory?.codedDescription(),
)