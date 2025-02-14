package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.*
import uk.gov.justice.digital.hmpps.integrations.delius.Document.Companion.breachNoticeUrn
import uk.gov.justice.digital.hmpps.model.EnforceableContact
import uk.gov.justice.digital.hmpps.model.WarningDetails
import uk.gov.justice.digital.hmpps.model.WarningTypes
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional(readOnly = true)
class WarningService(
    private val rdRepository: ReferenceDataRepository,
    private val documentRepository: DocumentRepository,
    private val disposalRepository: DisposalRepository,
    private val contactRepository: ContactRepository,
) {
    fun getWarningTypes(): WarningTypes = WarningTypes(
        rdRepository.findByDatasetCodeAndSelectableTrue(Dataset.BREACH_NOTICE_TYPE).codedDescriptions()
    )

    fun getWarningDetails(crn: String, breachNoticeId: UUID): WarningDetails {
        val breachReasons = rdRepository.findByDatasetCodeAndSelectableTrue(Dataset.BREACH_REASON)
        val sentenceTypes = rdRepository.findByDatasetCodeAndSelectableTrue(Dataset.BREACH_SENTENCE_TYPE)
        val eventId = documentRepository.findEventIdFromDocument(breachNoticeUrn(breachNoticeId))
            ?: throw NotFoundException("BreachNotice", "id", breachNoticeId)
        val disposal = disposalRepository.getByEventId(eventId)
        val enforceableContacts = contactRepository.findByEventIdAndOutcomeEnforceableTrue(eventId)
        return WarningDetails(
            breachReasons.codedDescriptions(),
            sentenceTypes.sentenceTypes(),
            disposal.type.defaultSentenceTypeCode(),
            enforceableContacts.map(Contact::toEnforceableContact),
        )
    }
}

fun Contact.toEnforceableContact() = EnforceableContact(
    id,
    LocalDateTime.of(date, startTime.toLocalTime()),
    description,
    type.codedDescription(),
    outcome!!.codedDescription(),
    notes,
    requirement?.toModel(),
)

fun Requirement.toModel() = uk.gov.justice.digital.hmpps.model.Requirement(
    id,
    checkNotNull(mainCategory?.codedDescription()),
    subCategory?.codedDescription(),
)