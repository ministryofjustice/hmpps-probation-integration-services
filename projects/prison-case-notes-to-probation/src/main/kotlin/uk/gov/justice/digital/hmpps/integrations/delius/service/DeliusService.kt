package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.AuditedInteraction
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteType
import uk.gov.justice.digital.hmpps.integrations.delius.model.DeliusCaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteNomisTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.OffenderRepository
import java.time.temporal.ChronoUnit
import javax.validation.Valid

@Service
class DeliusService(
    private val caseNoteRepository: CaseNoteRepository,
    private val nomisTypeRepository: CaseNoteNomisTypeRepository,
    private val caseNoteTypeRepository: CaseNoteTypeRepository,
    private val offenderRepository: OffenderRepository,
    private val assignmentService: AssignmentService,
    private val auditedInteractionService: AuditedInteractionService,
    private val relatedService: CaseNoteRelatedService
) {
    @Transactional
    fun mergeCaseNote(@Valid caseNote: DeliusCaseNote) {
        val existing = caseNoteRepository.findByNomisId(caseNote.header.noteId)

        val entity = if (existing == null) caseNote.newEntity() else existing.updateFrom(caseNote)
        if (entity != null) {
            caseNoteRepository.save(entity)

            auditedInteractionService.createAuditedInteraction(
                BusinessInteractionCode.CASE_NOTES_MERGE,
                AuditedInteraction.Parameters("contactId" to entity.id.toString())
            )
        }
    }

    private fun CaseNote.updateFrom(caseNote: DeliusCaseNote): CaseNote? {
        val last = lastModifiedDateTime.truncatedTo(ChronoUnit.SECONDS)
        val current = caseNote.body.systemTimestamp.truncatedTo(ChronoUnit.SECONDS)
        return if (last.isBefore(current)) {
            copy(
                notes = caseNote.body.notes(notes.length),
                date = caseNote.body.contactTimeStamp,
                startTime = caseNote.body.contactTimeStamp,
                lastModifiedDateTime = caseNote.body.systemTimestamp
            )
        } else {
            log.warn("Case Note update ignored because it was out of sequence ${caseNote.header}")
            null
        }
    }

    private fun DeliusCaseNote.newEntity(): CaseNote? {
        val caseNoteType = nomisTypeRepository.findById(body.typeLookup())
            .map { it.type }
            .orElseGet {
                caseNoteTypeRepository.findByCode(CaseNoteType.DEFAULT_CODE)
                    ?: throw NotFoundException("Case note type ${body.typeLookup()} not found and no default type is set")
            }

        val offender = offenderRepository.findByNomsIdAndSoftDeletedIsFalse(header.nomisId) ?: return null

        val relatedIds = relatedService.findRelatedCaseNoteIds(offender.id, body.typeLookup())

        val assignment = assignmentService.findAssignment(body.establishmentCode, body.staffName)

        return CaseNote(
            offenderId = offender.id,
            eventId = relatedIds.eventId,
            nsiId = relatedIds.nsiId,
            type = caseNoteType,
            nomisId = header.noteId,
            notes = body.notes(),
            date = body.contactTimeStamp,
            startTime = body.contactTimeStamp,
            isSensitive = caseNoteType.isSensitive,
            probationAreaId = assignment.first,
            teamId = assignment.second,
            staffId = assignment.third,
            staffEmployeeId = assignment.third
        )
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DeliusService::class.java)
    }
}
