package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.*
import uk.gov.justice.digital.hmpps.integrations.delius.Document.Companion.breachNoticeUrn
import uk.gov.justice.digital.hmpps.model.EnforceableContact
import uk.gov.justice.digital.hmpps.model.SentenceType
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
    private val linkedListRepository: LinkedListRepository,
    private val documentRepository: DocumentRepository,
    private val disposalRepository: DisposalRepository,
    private val contactRepository: ContactRepository,
) {
    fun getWarningTypes(crn: String, breachNoticeId: UUID): WarningTypesResponse {
        val disposal = crn.disposalForEvent(documentRepository.eventId(breachNoticeUrn(breachNoticeId)))
        val sentenceTypeRefData = rdRepository.findByDatasetCodeAndSelectableTrue(Dataset.BREACH_SENTENCE_TYPE)
        val linkedConditions = linkedListRepository.findByData1IdIn(sentenceTypeRefData.map { it.id })
            .associate { it.id.data1 to it.data2.description }
        val sentenceTypes = sentenceTypeRefData
            .map { SentenceType(it.code, it.description, linkedConditions[it.id] ?: "") }
            .sortedBy { it.description }

        return WarningTypesResponse(
            warningTypes = rdRepository
                .findByDatasetCodeAndSelectableTrue(Dataset.BREACH_NOTICE_TYPE).codedDescriptions(),
            sentenceTypes = sentenceTypes,
            defaultSentenceTypeCode = disposal.type.defaultSentenceTypeCode()
        )
    }

    fun getWarningDetails(crn: String, breachNoticeId: UUID): WarningDetails {
        val breachReasons = rdRepository.findByDatasetCodeAndSelectableTrue(Dataset.BREACH_REASON)
        val disposal = crn.disposalForEvent(documentRepository.eventId(breachNoticeUrn(breachNoticeId)))
        val enforceableContacts = contactRepository.findEnforceableContacts(disposal.event.id)
        return WarningDetails(
            breachReasons = breachReasons.codedDescriptions(),
            enforceableContacts = enforceableContacts.map(Contact::toEnforceableContact),
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