package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.courtcase.CourtCaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode.UPDATE_CONTACT
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.CaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.CaseNoteType.Companion.DEFAULT_CODE
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.PersonManagerRepository
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Service
class DeliusIntegrationService(
    auditedInteractionService: AuditedInteractionService,
    private val caseNoteRepository: CaseNoteRepository,
    private val caseNoteTypeRepository: CaseNoteTypeRepository,
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository
) : AuditableService(auditedInteractionService) {
    @Transactional
    fun mergeCourtCaseNote(crn: String, caseNote: CourtCaseNote, occurredAt: ZonedDateTime) = audit(UPDATE_CONTACT) {
        val person = personRepository.findByCrnAndSoftDeletedIsFalse(crn)
            ?: throw NotFoundException("Person", "crn", crn)
        val externalReference = caseNote.reference

        val existing =
            caseNoteRepository.findByExternalReferenceAndOffenderIdAndSoftDeletedIsFalse(externalReference, person.id)

        val entity =
            if (existing == null) {
                caseNote.newEntity(occurredAt, person.id)
            } else {
                existing.updateFrom(
                    caseNote,
                    occurredAt
                )
            }
        if (entity != null) {
            caseNoteRepository.save(entity)
        }
    }

    private fun CaseNote.updateFrom(caseNote: CourtCaseNote, occurredAt: ZonedDateTime): CaseNote? {
        val last = lastModifiedDateTime.truncatedTo(ChronoUnit.SECONDS)
        val current = occurredAt.truncatedTo(ChronoUnit.SECONDS)
        return if (last.isBefore(current)) {
            notes = caseNote.notes
            date = occurredAt.toLocalDate()
            startTime = occurredAt
            this
        } else {
            log.warn("Court Case Note update ignored because it was out of sequence ${caseNote.reference}")
            null
        }
    }

    private fun CourtCaseNote.newEntity(occurredAt: ZonedDateTime, personId: Long): CaseNote {
        val caseNoteType = caseNoteTypeRepository.findByCode(DEFAULT_CODE) ?: throw NotFoundException(
            "ContactType",
            "code",
            DEFAULT_CODE
        )
        val comDetails = personManagerRepository.findActiveManager(personId) ?: throw NotFoundException(
            "PersonManager",
            "personId",
            personId
        )

        return CaseNote(
            offenderId = personId,
            type = caseNoteType,
            notes = notes,
            date = occurredAt.toLocalDate(),
            startTime = occurredAt,
            isSensitive = caseNoteType.isSensitive,
            probationAreaId = comDetails.provider.id,
            teamId = comDetails.team!!.id,
            staffId = comDetails.staff!!.id,
            externalReference = reference
        )
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DeliusIntegrationService::class.java)
    }
}
