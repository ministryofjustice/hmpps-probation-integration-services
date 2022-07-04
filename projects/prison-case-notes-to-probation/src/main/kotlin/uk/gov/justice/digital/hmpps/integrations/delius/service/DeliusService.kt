package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exceptions.CaseNoteTypeNotFoundException
import uk.gov.justice.digital.hmpps.exceptions.OffenderNotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.audit.AuditedInteraction
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteType
import uk.gov.justice.digital.hmpps.integrations.delius.model.DeliusCaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteNomisTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.CaseNoteTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.OffenderRepository
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

        val entity = existing?.copy(
            notes = existing.notes + System.lineSeparator() + caseNote.body.notesToAppend(),
            date = caseNote.body.contactTimeStamp,
            startTime = caseNote.body.contactTimeStamp,
        ) ?: caseNote.newEntity()
        caseNoteRepository.save(entity)

        auditedInteractionService.createAuditedInteraction(
            BusinessInteractionCode.CASE_NOTES_MERGE,
            AuditedInteraction.Parameters("contactId" to entity.id.toString())
        )
    }

    private fun DeliusCaseNote.newEntity(): CaseNote {
        val caseNoteType = nomisTypeRepository.findById(body.typeLookup())
            .map { it.type }
            .orElseGet {
                caseNoteTypeRepository.findByCode(CaseNoteType.DEFAULT_CODE)
                    ?: throw CaseNoteTypeNotFoundException(body.typeLookup())
            }

        val offender = offenderRepository.findByNomsId(header.nomisId)
            ?: throw OffenderNotFoundException(header.nomisId)

        val relatedIds = relatedService.findRelatedCaseNoteIds(offender.id)

        val assignment = assignmentService.findAssignment(body.establishmentCode, body.staffName)

        return CaseNote(
            offenderId = offender.id,
            eventId = relatedIds.eventId,
            type = caseNoteType,
            nomisId = header.noteId,
            notes = body.notesToAppend(),
            date = body.contactTimeStamp,
            startTime = body.contactTimeStamp,
            isSensitive = caseNoteType.isSensitive,
            probationAreaId = assignment.first,
            teamId = assignment.second,
            staffId = assignment.third,
            staffEmployeeId = assignment.third
        )
    }
}
